package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;

/*
 * M""M                                       dP                              
 * M  M                                       88                              
 * M  M 88d888b. dP   .dP .d8888b. 88d888b. d8888P .d8888b. 88d888b. dP    dP 
 * M  M 88'  `88 88   d8' 88ooood8 88'  `88   88   88'  `88 88'  `88 88    88 
 * M  M 88    88 88 .88'  88.  ... 88    88   88   88.  .88 88       88.  .88 
 * M  M dP    dP 8888P'   `88888P' dP    dP   dP   `88888P' dP       `8888P88 
 * MMMM                                                                   .88 
 *                                                                    d8888P  
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
 * Central location to listen to events that are relevant for the inventory checks.
 * 
 * @see InventoryEvent
 */
public class InventoryListener implements Listener {

    /** The drop check. */
    private final Drop       drop       = new Drop();

    /** The fast click check. */
    private final FastClick  fastClick  = new FastClick();

    /** The instant bow check. */
    private final InstantBow instantBow = new InstantBow();

    /** The instant eat check. */
    private final InstantEat instantEat = new InstantEat();

    /**
     * We listen to EntityShootBow events for the InstantBow check.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityShootBow(final EntityShootBowEvent event) {
        /*
         *  _____       _   _ _           ____  _                 _     ____                
         * | ____|_ __ | |_(_) |_ _   _  / ___|| |__   ___   ___ | |_  | __ )  _____      __
         * |  _| | '_ \| __| | __| | | | \___ \| '_ \ / _ \ / _ \| __| |  _ \ / _ \ \ /\ / /
         * | |___| | | | |_| | |_| |_| |  ___) | | | | (_) | (_) | |_  | |_) | (_) \ V  V / 
         * |_____|_| |_|\__|_|\__|\__, | |____/|_| |_|\___/ \___/ \__| |____/ \___/ \_/\_/  
         *                        |___/                                                     
         */
        // Only if a player shot the arrow.
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            if (instantBow.isEnabled(player)){
                final long now = System.currentTimeMillis();
                final Location loc = player.getLocation();
                if (Combined.checkYawRate(player, loc.getYaw(), now, loc.getWorld().getName()))
                    event.setCancelled(true);
                // Still check instantBow.
            	if (instantBow.check(player, event.getForce()))
            		 // The check requested the event to be cancelled.
                    event.setCancelled(true);
            	else if (Improbable.check(player, 1f, now))
            		// COmbined fighting speed.
                	event.setCancelled(true);
            }  
        }
    }

    /**
     * We listen to FoodLevelChange events because Bukkit doesn't provide a PlayerFoodEating Event (or whatever it would
     * be called).
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        /*
         *  _____               _   _                   _    ____ _                            
         * |  ___|__   ___   __| | | |    _____   _____| |  / ___| |__   __ _ _ __   __ _  ___ 
         * | |_ / _ \ / _ \ / _` | | |   / _ \ \ / / _ \ | | |   | '_ \ / _` | '_ \ / _` |/ _ \
         * |  _| (_) | (_) | (_| | | |__|  __/\ V /  __/ | | |___| | | | (_| | | | | (_| |  __/
         * |_|  \___/ \___/ \__,_| |_____\___| \_/ \___|_|  \____|_| |_|\__,_|_| |_|\__, |\___|
         *                                                                          |___/      
         */
        // Only if a player ate food.
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            if (instantEat.isEnabled(player) && instantEat.check(player, event.getFoodLevel()))
                event.setCancelled(true);
            // Forget the food material, as the info is no longer needed.
            InventoryData.getData(player).instantEatFood = null;
        }
    }

    /**
     * We listen to InventoryClick events for the FastClick check.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryClick(final InventoryClickEvent event) {
        /*
         *  ___                      _                      ____ _ _      _    
         * |_ _|_ ____   _____ _ __ | |_ ___  _ __ _   _   / ___| (_) ___| | __
         *  | || '_ \ \ / / _ \ '_ \| __/ _ \| '__| | | | | |   | | |/ __| |/ /
         *  | || | | \ V /  __/ | | | || (_) | |  | |_| | | |___| | | (__|   < 
         * |___|_| |_|\_/ \___|_| |_|\__\___/|_|   \__, |  \____|_|_|\___|_|\_\
         *                                         |___/                       
         */
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player) event.getWhoClicked();
            if (fastClick.isEnabled(player)){
            	if (fastClick.check(player))
                    // The check requested the event to be cancelled.
                    event.setCancelled(true);
                // Combined speed:
                else if (Improbable.check(player, 1f, System.currentTimeMillis()))
                	event.setCancelled(true);
            }
        }
    }

    /**
     * We listen to DropItem events for the Drop check.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void onPlayerDropItem(final PlayerDropItemEvent event) {
        /*
         *  ____  _                         ____                  
         * |  _ \| | __ _ _   _  ___ _ __  |  _ \ _ __ ___  _ __  
         * | |_) | |/ _` | | | |/ _ \ '__| | | | | '__/ _ \| '_ \ 
         * |  __/| | (_| | |_| |  __/ |    | |_| | | | (_) | |_) |
         * |_|   |_|\__,_|\__, |\___|_|    |____/|_|  \___/| .__/ 
         *                |___/                            |_|    
         */
        // If the player died, all his items are dropped so ignore him.
        if (event.getPlayer().isDead())
            return;

        if (drop.isEnabled(event.getPlayer())){
        	if (drop.check(event.getPlayer())){
        		// TODO: Is the following command still correct? If so, adapt actions.
                // Cancelling drop events is not save (in certain circumstances items will disappear completely). So don't
                // do it and kick players instead by default.
                event.setCancelled(true);
        	}
            else if (Improbable.check(event.getPlayer(), 1f, System.currentTimeMillis())){
            	// Combined speed
            	event.setCancelled(true);
            }
        }


    }

    /**
     * We listen to PlayerInteract events for the InstantEat and InstantBow checks.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractEvent(final PlayerInteractEvent event) {
        /*
         *  ____  _                         ___       _                      _   
         * |  _ \| | __ _ _   _  ___ _ __  |_ _|_ __ | |_ ___ _ __ __ _  ___| |_ 
         * | |_) | |/ _` | | | |/ _ \ '__|  | || '_ \| __/ _ \ '__/ _` |/ __| __|
         * |  __/| | (_| | |_| |  __/ |     | || | | | ||  __/ | | (_| | (__| |_ 
         * |_|   |_|\__,_|\__, |\___|_|    |___|_| |_|\__\___|_|  \__,_|\___|\__|
         *                |___/                                                  
         */
        // Only interested in right-clicks while holding an item.
        if (!event.hasItem()
                || !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;

        final InventoryData data = InventoryData.getData(event.getPlayer());

        if (event.getItem().getType() == Material.BOW)
            // It was a bow, the player starts to pull the string, remember this time.
            data.instantBowLastTime = System.currentTimeMillis();
        else if (event.getItem().getType().isEdible()) {
            // It was food, the player starts to eat some food, remember this time and the type of food.
            data.instantEatFood = event.getItem().getType();
            data.instantEatLastTime = System.currentTimeMillis();
        } else {
            // Nothing that we are interested in, reset data.
            data.instantBowLastTime = 0;
            data.instantEatLastTime = 0;
            data.instantEatFood = null;
        }
    }
}
