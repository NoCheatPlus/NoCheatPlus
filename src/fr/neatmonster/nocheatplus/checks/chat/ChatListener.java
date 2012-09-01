package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.neatmonster.nocheatplus.players.Permissions;

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
public class ChatListener implements Listener {

    /** The color check. */
    private final Color    color    = new Color();

    /** The no pwnage check. */
    private final NoPwnage noPwnage = new NoPwnage();
    
    /** Global chat check (experiment: alternative / supplement). */
    private final GlobalChat globalChat = new GlobalChat();

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

        // First the color check.
        event.setMessage(color.check(player, event.getMessage(), false));

        // Then the no pwnage check.
        if (noPwnage.check(player, event.getMessage(), false))
        	event.setCancelled(true);
        else if (globalChat.check(player, event.getMessage(), (ICaptcha) noPwnage))
        	// Only check those that got through.
        	// (ICaptcha to start captcha if desired.)
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
        final String command = event.getMessage().split(" ")[0].substring(1).toLowerCase();

        // Protect some commands to prevent players for seeing which plugins are installed.
        if (ChatConfig.getConfig(player).protectPlugins)
            if ((command.equalsIgnoreCase("plugins") || command.equalsIgnoreCase("pl")
                    || command.equalsIgnoreCase("version") || command.equalsIgnoreCase("ver"))
                    && !player.hasPermission(Permissions.ADMINISTRATION_PLUGINS)) {
                event.getPlayer().sendMessage(
                        ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. "
                                + "Please contact the server administrators if you believe that this is in error.");
                event.setCancelled(true);
                return;
            }

        // Prevent /op and /deop commands from being used in chat.
        if (ChatConfig.getConfig(player).opInConsoleOnly && (command.equals("op") || command.equals("deop"))) {
            event.getPlayer().sendMessage(
                    ChatColor.RED + "I'm sorry, but this command can't be executed in chat. Use the console instead!");
            event.setCancelled(true);
            return;
        }

        // First the color check.
        event.setMessage(color.check(player, event.getMessage(), true));

        // Then the no pwnage check.
        if (noPwnage.check(player, event.getMessage(), true))
        	event.setCancelled(true);
    }

    /**
     * We listen to this type of events to prevent spambots from login to the server.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        /*
         *  ____  _                             _       _       
         * |  _ \| | __ _ _   _  ___ _ __      | | ___ (_)_ __  
         * | |_) | |/ _` | | | |/ _ \ '__|  _  | |/ _ \| | '_ \ 
         * |  __/| | (_| | |_| |  __/ |    | |_| | (_) | | | | |
         * |_|   |_|\__,_|\__, |\___|_|     \___/ \___/|_|_| |_|
         *                |___/                                 
         */
        final Player player = event.getPlayer();
        final ChatConfig cc = ChatConfig.getConfig(player);

        // Execute the no pwnage check.
        if (noPwnage.isEnabled(player) && noPwnage.checkLogin(player))
            event.disallow(Result.KICK_OTHER, cc.noPwnageReloginKickMessage);
    }

    /**
     * When a player moves, he will be checked for various suspicious behaviors.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerMove(final PlayerMoveEvent event) {
        /*
         *  _____  _                         __  __                
         * |  __ \| |                       |  \/  |               
         * | |__) | | __ _ _   _  ___ _ __  | \  / | _____   _____ 
         * |  ___/| |/ _` | | | |/ _ \ '__| | |\/| |/ _ \ \ / / _ \
         * | |    | | (_| | |_| |  __/ |    | |  | | (_) \ V /  __/
         * |_|    |_|\__,_|\__, |\___|_|    |_|  |_|\___/ \_/ \___|
         *                  __/ |                                  
         *                 |___/                                   
         */
        ChatData.getData(event.getPlayer()).noPwnageLastMovedTime = System.currentTimeMillis();
    }
}
