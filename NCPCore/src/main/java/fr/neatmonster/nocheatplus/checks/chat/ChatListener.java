package fr.neatmonster.nocheatplus.checks.chat;

import java.util.Collection;

import org.bukkit.ChatColor;
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
    
    public ChatListener(){
    	super(CheckType.CHAT);
    	ConfigFile config = ConfigManager.getConfigFile();
    	initFilters(config);
    	// (text inits in constructor.)
    }
    
    /**
     * Clear tree and feed lower case. Add versions with "/" if missing.
     * @param tree
     * @param inputs
     */
    private void feedCommands(SimpleCharPrefixTree tree, Collection<String> inputs){
    	tree.clear();
    	tree.feedAll(inputs, false, true);
    	for (String input : inputs){
    		if (!input.trim().startsWith("/")){
    			tree.feed("/" + input.trim().toLowerCase());
    		}
    	}
    }
    
    /**
     * Read string list from config and call feedCommands(tree, list).
     * @param tree
     * @param config
     * @param configPath
     */
    private void feedCommands(SimpleCharPrefixTree tree, ConfigFile config, String configPath){
    	feedCommands(tree, config.getStringList(configPath));
    }
    
	private void initFilters(ConfigFile config) {
		feedCommands(consoleOnlyCommands, config, ConfPaths.PROTECT_COMMANDS_CONSOLEONLY_CMDS);
		feedCommands(chatCommands, config, ConfPaths.CHAT_COMMANDS_HANDLEASCHAT);
		feedCommands(commandExclusions, config, ConfPaths.CHAT_COMMANDS_EXCLUSIONS);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerChangedWorld(final PlayerChangedWorldEvent event){
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
        if (color.isEnabled(player)){
        	event.setMessage(color.check(player, event.getMessage(), true));
        }
        
     // Trim is necessary because the server accepts leading spaces with commands.
        final String message = event.getMessage();
        final String lcMessage = message.trim().toLowerCase();
        final String[] split = lcMessage.split(" ", 2);
        final String alias = split[0].substring(1);
		final Command command = CommandUtil.getCommand(alias);
		final String lcAltMessage;
		if (command != null){
			lcAltMessage = "/" + command.getLabel().toLowerCase() + (split.length > 1 ? (" " + split[1]) : "");
		}
		else{
			lcAltMessage = lcMessage;
		}
        
        // Prevent /op and /deop commands from being used by players.
 		if (cc.consoleOnlyCheck && consoleOnlyCommands.hasAnyPrefixWords(lcMessage, lcAltMessage)) {
 			if (command == null || command.testPermission(player)){
 	 			player.sendMessage(ChatColor.RED + "I'm sorry, but this command can't be executed in chat. Use the console instead!");
 			}
 			event.setCancelled(true);
 			return;
 		}

        // Handle as chat or command.
 		final boolean handleAsChat = chatCommands.hasAnyPrefixWords(lcMessage, lcAltMessage);
        if (handleAsChat){
            // Treat as chat.
        	// TODO: Consider requesting permission updates on these, for consistency.
        	// TODO: Cut off the command (?).
            if (textChecks(player, message, true, false)) {
            	event.setCancelled(true);
            }
        }
        else if (!commandExclusions.hasAnyPrefixWords(lcMessage, lcAltMessage)){
            // Treat as command.
            if (commands.isEnabled(player) && commands.check(player, message, captcha)) {
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
        synchronized(data){
            captcha.resetCaptcha(cc, data);
        }
        // Fast relog check.
        if (relog.isEnabled(player) && relog.unsafeLoginCheck(player, cc, data)){
        	event.disallow(Result.KICK_OTHER, cc.relogKickMessage);
        }
        else if (logins.isEnabled(player) && logins.check(player, cc, data)){
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
            if (captcha.isEnabled(player) && captcha.shouldCheckCaptcha(cc, data)){
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
