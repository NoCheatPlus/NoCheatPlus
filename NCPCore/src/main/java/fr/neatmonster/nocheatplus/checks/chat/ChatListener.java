package fr.neatmonster.nocheatplus.checks.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree;

/**
 * Central location to listen to events that are relevant for the chat checks.
 * 
 * @see ChatEvent
 */
public class ChatListener extends CheckListener implements INotifyReload, JoinLeaveListener {

    // Checks.

    /** Captcha handler. */
    private final Captcha captcha		= addCheck(new Captcha());

    /** The color check. */
    private final Color    color    	= addCheck(new Color());    

    /** Commands repetition check. */
    private final Commands commands 	= addCheck(new Commands()); 

    /** Logins check (global) */
    private final Logins logins 		= addCheck(new Logins());

    /** Chat message check. */
    private final Text text 			= addCheck(new Text());

    /** Relogging check. */
    private final Relog relog 			= addCheck(new Relog());

    // Auxiliary stuff.

    /** Commands to be ignored completely. */
    private final SimpleCharPrefixTree commandExclusions = new SimpleCharPrefixTree();

    /** Commands to be handled as chat. */
    private final SimpleCharPrefixTree chatCommands = new SimpleCharPrefixTree();

    /** Commands not to be executed in-game.  */
    private final SimpleCharPrefixTree consoleOnlyCommands = new SimpleCharPrefixTree(); 

    public ChatListener() {
        super(CheckType.CHAT);
        ConfigFile config = ConfigManager.getConfigFile();
        initFilters(config);
        // (text inits in constructor.)
    }

    private void initFilters(ConfigFile config) {
        CommandUtil.feedCommands(consoleOnlyCommands, config, ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_CMDS, true);
        CommandUtil.feedCommands(chatCommands, config, ConfPaths.CHAT_COMMANDS_HANDLEASCHAT, true);
        CommandUtil.feedCommands(commandExclusions, config, ConfPaths.CHAT_COMMANDS_EXCLUSIONS, true);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        // Tell TickTask to update cached permissions.
        TickTask.requestPermissionUpdate(event.getPlayer().getName(), CheckType.CHAT);
    }

    /**
     * We listen to PlayerChat events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {

        final Player player = event.getPlayer();
        final boolean alreadyCancelled = event.isCancelled();

        // Tell TickTask to update cached permissions.
        // (Might omit this if already cancelled.)
        // TODO: Implement to only update on "timeout" or checks being activated at all.
        TickTask.requestPermissionUpdate(player.getName(), CheckType.CHAT);

        // First the color check.
        if (!alreadyCancelled && color.isEnabled(player)) {
            event.setMessage(color.check(player, event.getMessage(), false));
        }

        // Then the no chat check.
        // TODO: isMainThread: Could consider event.isAsync ?
        if (textChecks(player, event.getMessage(), false, alreadyCancelled)) {
            event.setCancelled(true);
        }

    }

    /**
     * We listen to PlayerCommandPreprocess events because commands can be used for spamming too.
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();

        // Tell TickTask to update cached permissions.
        TickTask.requestPermissionUpdate(player.getName(), CheckType.CHAT);

        final ChatConfig cc = ChatConfig.getConfig(player);

        // Checks that replace parts of the message (color).
        if (color.isEnabled(player)) {
            event.setMessage(color.check(player, event.getMessage(), true));
        }

        // Left-trim is necessary because the server accepts leading spaces with commands.
        final String message = event.getMessage();
        final String lcMessage = StringUtil.leftTrim(message).toLowerCase();
        // TODO: Remove bukkit: etc.

        final String[] split = lcMessage.split(" ", 2);
        final String alias = split[0].substring(1);
        final Command command = CommandUtil.getCommand(alias);

        final List<String> messageVars = new ArrayList<String>(); // Could as well use an array and allow null on input of SimpleCharPrefixTree.
        messageVars.add(lcMessage);
        String checkMessage = message; // Message to run chat checks on.
        if (command != null) {
            messageVars.add("/" + command.getLabel().toLowerCase() + (split.length > 1 ? (" " + split[1]) : ""));
        }
        if (alias.indexOf(":") != -1) {
            final int index = message.indexOf(":") + 1;
            if (index < message.length()) {
                checkMessage = message.substring(index);
                messageVars.add(checkMessage.toLowerCase());
            }
        }
        // Prevent commands from being used by players (e.g. /op /deop /reload).
        if (cc.consoleOnlyCheck && consoleOnlyCommands.hasAnyPrefixWords(messageVars)) {
            if (command == null || command.testPermission(player)) {
                player.sendMessage(cc.consoleOnlyMessage);
            }
            event.setCancelled(true);
            return;
        }

        // Handle as chat or command.
        if (chatCommands.hasAnyPrefixWords(messageVars)) {
            // Treat as chat.
            // TODO: Consider requesting permission updates on these, for consistency.
            // TODO: Cut off the command ?.
            if (textChecks(player, checkMessage, true, false)) {
                event.setCancelled(true);
            }
        }
        else if (!commandExclusions.hasAnyPrefixWords(messageVars)) {
            // Treat as command.
            if (commands.isEnabled(player) && commands.check(player, checkMessage, captcha)) {
                event.setCancelled(true);
            }
        }

    }

    private boolean textChecks(final Player player, final String message, final boolean isMainThread, final boolean alreadyCancelled) {
        return text.isEnabled(player) && text.check(player, message, captcha, isMainThread, alreadyCancelled);
    }

    /**
     * We listen to this type of events to prevent spambots from login to the server.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.NORMAL)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        if (event.getResult() != Result.ALLOWED) return;
        final Player player = event.getPlayer();
        final ChatConfig cc = ChatConfig.getConfig(player);
        final ChatData data = ChatData.getData(player);

        // Tell TickTask to update cached permissions.
        TickTask.requestPermissionUpdate(player.getName(), CheckType.CHAT);
        // Force permission update.
        TickTask.updatePermissions(); // TODO: This updates ALL... something more efficient ?

        // Reset captcha of player if needed.
        synchronized(data) {
            captcha.resetCaptcha(cc, data);
        }
        // Fast relog check.
        if (relog.isEnabled(player) && relog.unsafeLoginCheck(player, cc, data)) {
            event.disallow(Result.KICK_OTHER, cc.relogKickMessage);
        }
        else if (logins.isEnabled(player) && logins.check(player, cc, data)) {
            event.disallow(Result.KICK_OTHER, cc.loginsKickMessage);
        }
    }

    @Override
    public void onReload() {
        // Read some things from the global config file.
        ConfigFile config = ConfigManager.getConfigFile();
        initFilters(config);
        text.onReload();
        logins.onReload();
    }

    @Override
    public void playerJoins(final Player player) { 
        final ChatConfig cc = ChatConfig.getConfig(player);
        final ChatData data = ChatData.getData(player);
        synchronized (data) {
            if (captcha.isEnabled(player) && captcha.shouldCheckCaptcha(cc, data)) {
                // shouldCheckCaptcha: only if really enabled.
                // TODO: Later: add check for cc.captchaOnLogin or so (before shouldCheckCaptcha).
                // TODO: maybe schedule this to come after other plugins messages.
                captcha.sendNewCaptcha(player, cc, data);
            }
        }
    }

    @Override
    public void playerLeaves(final Player player) {
    }

}
