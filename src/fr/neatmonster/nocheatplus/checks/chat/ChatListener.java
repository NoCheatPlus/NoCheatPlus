package fr.neatmonster.nocheatplus.checks.chat;

import java.lang.management.ManagementFactory;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;

/**
 * Central location to listen to events that are
 * relevant for the chat checks
 * 
 */
public class ChatListener extends CheckListener {

    private final NoPwnageCheck      noPwnageCheck;
    private final ArrivalsLimitCheck arrivalsLimitCheck;
    private final ColorCheck         colorCheck;

    public ChatListener() {
        super("chat");

        noPwnageCheck = new NoPwnageCheck();
        arrivalsLimitCheck = new ArrivalsLimitCheck();
        colorCheck = new ColorCheck();

    }

    /**
     * We listen to PlayerChat events for obvious reasons
     * 
     * @param event
     *            The PlayerChat event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void chat(final PlayerChatEvent event) {

        boolean cancelled = false;

        final NCPPlayer player = NCPPlayer.getPlayer(event.getPlayer());
        final ChatConfig cc = (ChatConfig) getConfig(player);
        final ChatData data = (ChatData) getData(player);

        // Remember the original message
        data.message = event.getMessage();

        // Remember if it is a command or not
        if (event instanceof PlayerCommandPreprocessEvent)
            data.isCommand = true;
        else
            data.isCommand = false;

        // Now do the actual checks

        // First the nopwnage check
        if (cc.noPwnageCheck && !player.hasPermission(Permissions.CHAT_NOPWNAGE))
            if (noPwnageCheck.check(player, event)) {
                player.getBukkitPlayer().kickPlayer(Check.removeColors(cc.noPwnageMessagesKick));
                return;
            } else
                cancelled = event.isCancelled();

        // Second the color check
        if (!cancelled && cc.colorCheck && !player.hasPermission(Permissions.CHAT_COLOR))
            cancelled = colorCheck.check(player);

        // If one of the checks requested the event to be cancelled, do it
        if (cancelled)
            event.setCancelled(cancelled);
        else
            // In case one of the events modified the message, make sure that
            // the new message gets used
            event.setMessage(data.message);
    }

    /**
     * We listen to PlayerCommandPreprocess events because commands can be
     * used for spamming too.
     * 
     * @param event
     *            The PlayerCommandPreprocess Event
     */
    @EventHandler(
            priority = EventPriority.LOWEST)
    public void commandPreprocess(final PlayerCommandPreprocessEvent event) {

        final NCPPlayer player = NCPPlayer.getPlayer(event.getPlayer());
        final ChatConfig cc = (ChatConfig) getConfig(player);

        final String command = event.getMessage().split(" ")[0].substring(1).toLowerCase();

        // Protect the /plugins, /pl, /? commands to prevent players for seeing which plugins are installed
        if (cc.protectPlugins && (command.equals("plugins") || command.equals("pl") || command.equals("?"))
                && !player.hasPermission(Permissions.ADMIN_PLUGINS)) {
            event.getPlayer().sendMessage(
                    ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. "
                            + "Please contact the server administrators if you believe that this is in error.");
            event.setCancelled(true);
            return;
        }

        // If OP by console only is enabled, prevent the op/deop commands
        // to be used by a player who is OP or has the required permissions
        if (cc.opByConsoleOnly
                && (command.equals("op")
                        && (event.getPlayer().isOp() || player.hasPermission("bukkit.command.op.give")) || command
                        .equals("deop") && (event.getPlayer().isOp() || player.hasPermission("bukkit.command.op.take")))) {
            event.getPlayer().sendMessage(ChatColor.RED + "This command can only be executed from the console!");
            event.setCancelled(true);
            return;
        }

        // This type of event is derived from PlayerChatEvent, therefore
        // just treat it like that
        chat(event);
    }

    /**
     * We listen to PlayerLogin events for the arrivalslimit check
     * 
     * @param event
     *            The PlayerLogin Event
     */
    @EventHandler(
            priority = EventPriority.LOWEST)
    public void login(final PlayerLoginEvent event) {
        final NCPPlayer player = NCPPlayer.getPlayer(event.getPlayer());
        final ChatConfig cc = (ChatConfig) getConfig(player);
        final ChatData data = (ChatData) getData(player);

        /*** NOPWNAGE CHECK ***/
        // Check if the join is legit
        if (cc.noPwnageCheck && !player.hasPermission(Permissions.CHAT_NOPWNAGE))
            if (noPwnageCheck.handleJoin(player, data, cc))
                event.disallow(Result.KICK_OTHER, Check.removeColors(cc.noPwnageMessagesKick));

        /*** ARRIVALSLIMIT CHECK ***/
        // Do not check the players if the event is already cancelled,
        // if the server has just restarted or if it is a regular player
        if (event.getResult() == Result.KICK_OTHER
                || System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime() < 120000L
                || System.currentTimeMillis() - event.getPlayer().getFirstPlayed() > cc.arrivalsLimitNewTime)
            return;

        if (cc.arrivalsLimitCheck && arrivalsLimitCheck.check(player, data, cc))
            // If the player failed the check, disallow the login
            event.disallow(Result.KICK_OTHER, cc.arrivalsLimitKickMessage);
    }
}
