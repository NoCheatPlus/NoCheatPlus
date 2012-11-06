package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.INotifyReload;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree;

/*
 * MM'""""'YMM dP                  dP   M""MMMMMMMM oo            dP                                       
 * M' .mmm. `M 88                  88   M  MMMMMMMM               88                                       
 * M  MMMMMooM 88d888b. .d8888b. d8888P M  MMMMMMMM dP .d8888b. d8888P .d8888b. 88d888b. .d8888b. 88d888b. 
 * M  MMMMMMMM 88'  `88 88'  `88   88   M  MMMMMMMM 88 Y8ooooo.   88   88ooood8 88'  `88 88ooood8 88'  `88 
 * M. `MMM' .M 88    88 88.  .88   88   M  MMMMMMMM 88       88   88   88.  ... 88    88 88.  ... 88       
 * MM.     .dM dP    dP `88888P8   dP   M         M dP `88888P'   dP   `88888P' dP    dP `88888P' dP       
 * MMMMMMMMMMM                          MMMMMMMMMMM                                                        
 */
/**
 * Central location to listen to events that are relevant for the chat checks.
 * 
 * @see ChatEvent
 */
public class ChatListener implements Listener, INotifyReload {
    
    // Checks.
    
    /** Captcha handler. */
    private final Captcha captcha = new Captcha();

    /** The color check. */
    private final Color    color    = new Color();    
    
    /** Commands repetition check. */
    private final Commands commands = new Commands(); 
    
    /** Logins check (global) */
    private final Logins logins = new Logins();
    
    /** Chat message check. */
    private final Text text = new Text();
   
    /** Relogging check. */
    private final Relog relog = new Relog();
    
    // Auxiliary stuff.
    
    /** Commands to be ignored completely. */
    private final SimpleCharPrefixTree commandExclusions = new SimpleCharPrefixTree();
    
    /** Commands to be handled as chat. */
    private final SimpleCharPrefixTree chatCommands = new SimpleCharPrefixTree(); 
    
    
    public ChatListener(){
    	ConfigFile config = ConfigManager.getConfigFile();
    	initFilters(config);
    	// (text inits in constructor.)
    }
    
	private void initFilters(ConfigFile config) {
		commandExclusions.clear();
    	commandExclusions.feedAll(config.getStringList(ConfPaths.CHAT_COMMANDS_EXCLUSIONS), false, true);
    	chatCommands.clear();
    	chatCommands.feedAll(config.getStringList(ConfPaths.CHAT_COMMANDS_HANDLEASCHAT), false, true);
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
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        /*
         *  ____  _                          ____ _           _   
         * |  _ \| | __ _ _   _  ___ _ __   / ___| |__   __ _| |_ 
         * | |_) | |/ _` | | | |/ _ \ '__| | |   | '_ \ / _` | __|
         * |  __/| | (_| | |_| |  __/ |    | |___| | | | (_| | |_ 
         * |_|   |_|\__,_|\__, |\___|_|     \____|_| |_|\__,_|\__|
         *                |___/                                   
         */
        final Player player = event.getPlayer();
        
        // Tell TickTask to update cached permissions.
        TickTask.requestPermissionUpdate(player.getName(), CheckType.CHAT);

        // First the color check.
        if (color.isEnabled(player)) event.setMessage(color.check(player, event.getMessage(), false));

        // Then the no chat check.
        if (text.isEnabled(player) && text.check(player, event.getMessage(), captcha, false))
        	event.setCancelled(true);
    }

    /**
     * We listen to PlayerCommandPreprocess events because commands can be used for spamming too.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        /*
         *  ____  _                          ____                                          _ 
         * |  _ \| | __ _ _   _  ___ _ __   / ___|___  _ __ ___  _ __ ___   __ _ _ __   __| |
         * | |_) | |/ _` | | | |/ _ \ '__| | |   / _ \| '_ ` _ \| '_ ` _ \ / _` | '_ \ / _` |
         * |  __/| | (_| | |_| |  __/ |    | |__| (_) | | | | | | | | | | | (_| | | | | (_| |
         * |_|   |_|\__,_|\__, |\___|_|     \____\___/|_| |_| |_|_| |_| |_|\__,_|_| |_|\__,_|
         *                |___/                                                              
         *  ____                                             
         * |  _ \ _ __ ___ _ __  _ __ ___   ___ ___  ___ ___ 
         * | |_) | '__/ _ \ '_ \| '__/ _ \ / __/ _ \/ __/ __|
         * |  __/| | |  __/ |_) | | | (_) | (_|  __/\__ \__ \
         * |_|   |_|  \___| .__/|_|  \___/ \___\___||___/___/
         *                |_|                                
         */
        final Player player = event.getPlayer();
        
        // Tell TickTask to update cached permissions.
        TickTask.requestPermissionUpdate(player.getName(), CheckType.CHAT);
        
        // Trim is necessary because the server accepts leading spaces with commands.
        // TODO: Maybe: only remove the leading whitespace or spaces.
        String lcMessage = event.getMessage().trim().toLowerCase();
        final String command = lcMessage.split(" ")[0].substring(1);

        final ChatConfig cc = ChatConfig.getConfig(player);
        
        // Protect some commands to prevent players for seeing which plugins are installed.
        if (cc.protectPlugins)
            if ((command.equals("plugins") || command.equals("pl")
                    || command.equals("version") || command.equals("ver"))
                    && !player.hasPermission(Permissions.ADMINISTRATION_PLUGINS)) {
                event.getPlayer().sendMessage(
                        ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. "
                                + "Please contact the server administrators if you believe that this is in error.");
                event.setCancelled(true);
                return;
            }

        // Prevent /op and /deop commands from being used in chat.
        if (cc.opInConsoleOnly && (command.equals("op") || command.equals("deop"))) {
            event.getPlayer().sendMessage(
                    ChatColor.RED + "I'm sorry, but this command can't be executed in chat. Use the console instead!");
            event.setCancelled(true);
            return;
        }

        // First the color check.
        if (color.isEnabled(player)) event.setMessage(color.check(player, event.getMessage(), true));
        
        // Reset lcMessage (might be canged by color check).
        lcMessage = event.getMessage().trim().toLowerCase();
        final boolean handleAsChat = chatCommands.hasPrefixWords(lcMessage);

        // Then the no pwnage check.
        if (handleAsChat){
            // Treat as chat.
            if (text.isEnabled(player) && text.check(player, event.getMessage(), captcha, true))
                event.setCancelled(true);
        }
        else if (!commandExclusions.hasPrefixWords(lcMessage)){
            // Treat as command.
            if (commands.isEnabled(player) && commands.check(player, event.getMessage(), captcha))
                event.setCancelled(true);
        }

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

        final Player player = event.getPlayer();
        final ChatConfig cc = ChatConfig.getConfig(player);
        final ChatData data = ChatData.getData(player);
        
        // Tell TickTask to update cached permissions.
        TickTask.requestPermissionUpdate(player.getName(), CheckType.CHAT);
        // Force permission update.
        TickTask.updatePermissions();
        
        // Reset captcha of player if needed.
        synchronized(data){
            captcha.resetCaptcha(cc, data);
        }
        // Fast relog check.
        if (relog.isEnabled(player) && relog.unsafeLoginCheck(player, cc, data))
            event.disallow(Result.KICK_OTHER, cc.relogKickMessage);
        else if (logins.isEnabled(player) && logins.check(player, cc, data))
            event.disallow(Result.KICK_OTHER, cc.loginsKickMessage);
    }
    
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
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
	public void onReload() {
		// Read some things from the global config file.
    	ConfigFile config = ConfigManager.getConfigFile();
    	initFilters(config);
    	text.onReload();
	}

}
