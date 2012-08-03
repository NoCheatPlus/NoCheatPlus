package fr.neatmonster.nocheatplus.checks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

/*
 * M""MMM""MMM""M                   dP                                                          dP          
 * M  MMM  MMM  M                   88                                                          88          
 * M  MMP  MMP  M .d8888b. 88d888b. 88  .dP  .d8888b. 88d888b. .d8888b. dP    dP 88d888b. .d888b88 .d8888b. 
 * M  MM'  MM' .M 88'  `88 88'  `88 88888"   88'  `88 88'  `88 88'  `88 88    88 88'  `88 88'  `88 Y8ooooo. 
 * M  `' . '' .MM 88.  .88 88       88  `8b. 88.  .88 88       88.  .88 88.  .88 88    88 88.  .88       88 
 * M    .d  .dMMM `88888P' dP       dP   `YP `88888P8 dP       `88888P' `88888P' dP    dP `88888P8 `88888P' 
 * MMMMMMMMMMMMMM                                                                                           
 */
/**
 * Only place that listens to player-teleport related events and dispatches them to relevant checks.
 */
public class Workarounds implements Listener {

    /**
     * A listener listening to PlayerMoveEvents.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.HIGHEST)
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
        // No typo here. I really only handle cancelled events and ignore others.
        if (!event.isCancelled())
            return;

        // Fix a common mistake that other developers make (cancelling move events is crazy, rather set the target
        // location to the from location).
        event.setCancelled(false);
        event.setTo(event.getFrom().clone());
    }

    /**
     * A listener listening to PlayerToggleSprintEvents.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.HIGHEST)
    public void onPlayerToggleSprint(final PlayerToggleSprintEvent event) {
        /*
         *  _____  _                         _______                _         _____            _       _   
         * |  __ \| |                       |__   __|              | |       / ____|          (_)     | |  
         * | |__) | | __ _ _   _  ___ _ __     | | ___   __ _  __ _| | ___  | (___  _ __  _ __ _ _ __ | |_ 
         * |  ___/| |/ _` | | | |/ _ \ '__|    | |/ _ \ / _` |/ _` | |/ _ \  \___ \| '_ \| '__| | '_ \| __|
         * | |    | | (_| | |_| |  __/ |       | | (_) | (_| | (_| | |  __/  ____) | |_) | |  | | | | | |_ 
         * |_|    |_|\__,_|\__, |\___|_|       |_|\___/ \__, |\__, |_|\___| |_____/| .__/|_|  |_|_| |_|\__|
         *                  __/ |                        __/ | __/ |               | |                     
         *                 |___/                        |___/ |___/                |_|                     
         */
        // Some plugins cancel "sprinting", which makes no sense at all because it doesn't stop people from sprinting
        // and rewards them by reducing their hunger bar as if they were walking instead of sprinting.
        if (event.isCancelled() && event.isSprinting())
            event.setCancelled(false);
    }
}
