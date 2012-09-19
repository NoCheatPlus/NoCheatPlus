package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.neatmonster.nocheatplus.checks.combined.Improbable;

/*
 * M#"""""""'M  dP                   dP       MM"""""""`YM dP                            
 * ##  mmmm. `M 88                   88       MM  mmmmm  M 88                            
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  M'        .M 88 .d8888b. .d8888b. .d8888b. 
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   MM  MMMMMMMM 88 88'  `88 88'  `"" 88ooood8 
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. MM  MMMMMMMM 88 88.  .88 88.  ... 88.  ... 
 * M#       .;M dP `88888P' `88888P' dP   `YP MM  MMMMMMMM dP `88888P8 `88888P' `88888P' 
 * M#########M                                MMMMMMMMMMMM                               
 * 
 * M""MMMMMMMM oo            dP                                       
 * M  MMMMMMMM               88                                       
 * M  MMMMMMMM dP .d8888b. d8888P .d8888b. 88d888b. .d8888b. 88d888b. 
 * M  MMMMMMMM 88 Y8ooooo.   88   88ooood8 88'  `88 88ooood8 88'  `88 
 * M  MMMMMMMM 88       88   88   88.  ... 88    88 88.  ... 88       
 * M         M dP `88888P'   dP   `88888P' dP    dP `88888P' dP       
 * MMMMMMMMMMM                                                        
 */
/**
 * Central location to listen to events that are relevant for the block place checks.
 * 
 * @see BlockPlaceEvent
 */
public class BlockPlaceListener implements Listener {

    /** The direction check. */
    private final Direction direction = new Direction();

    /** The fast place check. */
    private final FastPlace fastPlace = new FastPlace();

    /** The no swing check. */
    private final NoSwing   noSwing   = new NoSwing();

    /** The reach check. */
    private final Reach     reach     = new Reach();

    /** The speed check. */
    private final Speed     speed     = new Speed();

    /**
     * We listen to BlockPlace events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(final BlockPlaceEvent event) {
        /*
         *  ____  _            _      ____  _                
         * | __ )| | ___   ___| | __ |  _ \| | __ _  ___ ___ 
         * |  _ \| |/ _ \ / __| |/ / | |_) | |/ _` |/ __/ _ \
         * | |_) | | (_) | (__|   <  |  __/| | (_| | (_|  __/
         * |____/|_|\___/ \___|_|\_\ |_|   |_|\__,_|\___\___|
         */
        // We don't care about null blocks.
        if (event.getBlock() == null || event.getBlockAgainst() == null)
            return;

        final Player player = event.getPlayer();
        final Block block = event.getBlockPlaced();

        boolean cancelled = false;

        // First, the fast place check.
        if (fastPlace.isEnabled(player)){
        	if (fastPlace.check(player, block))
                cancelled = true;
        	else         // Combined speed:
                if (Improbable.check(player, 1f, System.currentTimeMillis()))
                	cancelled = true;
        }

        // Second, the no swing check (player doesn't swing his arm when placing a lily pad).
        if (!cancelled && event.getBlockPlaced().getType() != Material.WATER_LILY && noSwing.isEnabled(player)
                && noSwing.check(player))
            cancelled = true;

        // Third, the reach check.
        if (!cancelled && reach.isEnabled(player) && reach.check(player, block.getLocation()))
            cancelled = true;

        // Fourth, the direction check.
        if (!cancelled && direction.isEnabled(player)
                && direction.check(player, block.getLocation(), event.getBlockAgainst().getLocation()))
            cancelled = true;

        // If one of the checks requested to cancel the event, do so.
        if (cancelled)
            event.setCancelled(cancelled);
    }

    /**
     * We listen to PlayerAnimation events because it is (currently) equivalent to "player swings arm" and we want to
     * check if he did that between block breaks.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerAnimation(final PlayerAnimationEvent event) {
        /*
         *  ____  _                            _          _                 _   _             
         * |  _ \| | __ _ _   _  ___ _ __     / \   _ __ (_)_ __ ___   __ _| |_(_) ___  _ __  
         * | |_) | |/ _` | | | |/ _ \ '__|   / _ \ | '_ \| | '_ ` _ \ / _` | __| |/ _ \| '_ \ 
         * |  __/| | (_| | |_| |  __/ |     / ___ \| | | | | | | | | | (_| | |_| | (_) | | | |
         * |_|   |_|\__,_|\__, |\___|_|    /_/   \_\_| |_|_|_| |_| |_|\__,_|\__|_|\___/|_| |_|
         *                |___/                                                               
         */
        // Just set a flag to true when the arm was swung.
        BlockPlaceData.getData(event.getPlayer()).noSwingArmSwung = true;
    }

    /**
     * We listener to PlayerInteract events to prevent players from spamming the server with monster eggs.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        /*
         *  ____  _                         ___       _                      _   
         * |  _ \| | __ _ _   _  ___ _ __  |_ _|_ __ | |_ ___ _ __ __ _  ___| |_ 
         * | |_) | |/ _` | | | |/ _ \ '__|  | || '_ \| __/ _ \ '__/ _` |/ __| __|
         * |  __/| | (_| | |_| |  __/ |     | || | | | ||  __/ | | (_| | (__| |_ 
         * |_|   |_|\__,_|\__, |\___|_|    |___|_| |_|\__\___|_|  \__,_|\___|\__|
         *                |___/                                                  
         */
        // We are only interested by monster eggs.
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getPlayer().getItemInHand() == null
                || event.getPlayer().getItemInHand().getType() != Material.MONSTER_EGG)
            return;

        final Player player = event.getPlayer();

        // Do the actual check...
        if (speed.isEnabled(player) && speed.check(player))
            // If the check was positive, cancel the event.
            event.setCancelled(true);
    }

    /**
     * We listen to ProjectileLaunch events to prevent players from launching projectiles too quickly.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        /*
         *  ____            _           _   _ _        _                           _     
         * |  _ \ _ __ ___ (_) ___  ___| |_(_) | ___  | |    __ _ _   _ _ __   ___| |__  
         * | |_) | '__/ _ \| |/ _ \/ __| __| | |/ _ \ | |   / _` | | | | '_ \ / __| '_ \ 
         * |  __/| | | (_) | |  __/ (__| |_| | |  __/ | |__| (_| | |_| | | | | (__| | | |
         * |_|   |_|  \___// |\___|\___|\__|_|_|\___| |_____\__,_|\__,_|_| |_|\___|_| |_|
         *               |__/                                                            
         */
        // The shooter needs to be a player.
        if (!(event.getEntity().getShooter() instanceof Player))
            return;

        // And the projectile must be one the following:
        switch (event.getEntityType()) {
        case ENDER_PEARL:
            break;
        case ENDER_SIGNAL:
            break;
        case EGG:
            break;
        case SNOWBALL:
            break;
        case THROWN_EXP_BOTTLE:
            break;
        default:
            return;
        }

        final Player player = (Player) event.getEntity().getShooter();

        // Do the actual check...
        if (speed.isEnabled(player) && speed.check(player))
            // If the check was positive, cancel the event.
            event.setCancelled(true);
    }
}
