package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
 */
public class InventoryListener implements Listener {
    private final Drop       drop       = new Drop();
    private final InstantBow instantBow = new InstantBow();
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
        // Only if a player shot the arrow.
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            if (instantBow.isEnabled(player) && instantBow.check(player, event.getForce()))
                // The check requested the event to be cancelled.
                event.setCancelled(true);
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
     * We listen to DropItem events for the Drop check.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void onPlayerDropItem(final PlayerDropItemEvent event) {
        // If the player died, all his items are dropped so ignore him.
        if (event.getPlayer().isDead())
            return;

        if (drop.isEnabled(event.getPlayer()) && drop.check(event.getPlayer()))
            // Cancelling drop events is not save (in certain circumstances items will disappear completely). So don't
            // do it and kick players instead by default.
            event.getPlayer().kickPlayer("You're not allowed to crash the server by dropping items!");
    }

    /**
     * We listen to PlayerInteract events for the InstantEat and InstantBow checks.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.LOWEST)
    public void onPlayerInteractEvent(final PlayerInteractEvent event) {
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
