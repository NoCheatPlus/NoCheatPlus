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

import fr.neatmonster.nocheatplus.checks.Check;
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


// TODO: SYNC


/**
 * Central location to listen to events that are relevant for the chat checks.
 */
public class ChatListener implements Listener {
    private final Arrivals arrivals = new Arrivals();
    private final Color    color    = new Color();
    private final NoPwnage noPwnage = new NoPwnage();

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
        if (color.isEnabled(player))
            event.setMessage(color.check(player, event.getMessage()));

        // Then the no pwnage check.
        if (noPwnage.check(player, event, false))
            player.kickPlayer(Check.removeColors(ChatConfig.getConfig(player).noPwnageKickMessage));
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
        if (ChatConfig.getConfig(player).protectPlugins
                && (command.equals("plugins") || command.equals("pl") || command.equals("?"))
                && !player.hasPermission(Permissions.ADMINISTRATION_PLUGINS)) {
            event.getPlayer().sendMessage(
                    ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. "
                            + "Please contact the server administrators if you believe that this is in error.");
            event.setCancelled(true);
            return;
        }

        // First the color check.
        if (color.isEnabled(player))
            event.setMessage(color.check(player, event.getMessage()));

        // Then the no pwnage check.
        if (noPwnage.check(player, event, true))
            player.kickPlayer(Check.removeColors(ChatConfig.getConfig(player).noPwnageKickMessage));
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
        final ChatConfig cc = ChatConfig.getConfig(player); // Non critical use (concurrency).

        // First the arrivals check, if enabled of course.
        if (arrivals.isEnabled(player) && arrivals.check(player))
            // The player failed the check, disallow the login.
            event.disallow(Result.KICK_OTHER, cc.arrivalsMessage);

        // Then the no pwnage check, if the login isn't already disallowed.
        if (event.getResult() != Result.KICK_OTHER && noPwnage.check(player))
            event.disallow(Result.KICK_OTHER, cc.noPwnageReloginKickMessage);
    }
}
