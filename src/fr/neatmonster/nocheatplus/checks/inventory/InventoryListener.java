package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckUtils;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;

/**
 * Central location to listen to events that are
 * relevant for the inventory checks
 * 
 */
public class InventoryListener extends CheckListener {

    private final DropCheck       dropCheck;
    private final InstantBowCheck instantBowCheck;
    private final InstantEatCheck instantEatCheck;

    public InventoryListener() {
        super("inventory");

        dropCheck = new DropCheck();
        instantBowCheck = new InstantBowCheck();
        instantEatCheck = new InstantEatCheck();
    }

    /**
     * We listen to EntityShootBowEvent for the instantbow check
     * 
     * @param event
     *            The EntityShootBowEvent
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void bowfired(final EntityShootBowEvent event) {
        // Only if a player shot the arrow
        if (event.getEntity() instanceof Player) {
            final NCPPlayer player = NCPPlayer.getPlayer((Player) event.getEntity());
            final InventoryConfig cc = (InventoryConfig) getConfig(player);

            // Only if he should get checked
            if (cc.bowCheck && !player.hasPermission(Permissions.INVENTORY_INSTANTBOW)) {
                getData(player);
                final boolean cancelled = instantBowCheck.check(player, event);

                // The check requested the bowshooting turo get cancelled
                event.setCancelled(cancelled);
            }
        }
    }

    /**
     * We listen to FoodLevelChange Event because Bukkit doesn't provide a
     * PlayerFoodEating Event (or whatever it would be called).
     * 
     * @param event
     *            The FoodLevelChangeEvent
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void foodchanged(final FoodLevelChangeEvent event) {
        // Only if a player ate food
        if (event.getEntity() instanceof Player) {
            final NCPPlayer player = NCPPlayer.getPlayer((Player) event.getEntity());
            final InventoryConfig cc = (InventoryConfig) getConfig(player);
            final InventoryData data = (InventoryData) getData(player);

            // Only if he should get checked
            if (cc.eatCheck && !player.hasPermission(Permissions.INVENTORY_INSTANTEAT)) {

                final boolean cancelled = instantEatCheck.check(player, event);

                // The check requested the foodlevelchange to get cancelled
                event.setCancelled(cancelled);
            }

            // Forget the food material, as the info is no longer needed
            data.foodMaterial = null;
        }
    }

    /**
     * We listen to DropItem Event for the dropCheck
     * 
     * @param event
     *            The PlayerDropItem Event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void handlePlayerDropItemEvent(final PlayerDropItemEvent event) {

        if (event.getPlayer().isDead())
            return;

        boolean cancelled = false;

        final NCPPlayer player = NCPPlayer.getPlayer(event.getPlayer());
        final InventoryConfig cc = (InventoryConfig) getConfig(player);
        getData(player);

        // If it should be executed, do it
        if (cc.dropCheck && !player.hasPermission(Permissions.INVENTORY_DROP))
            cancelled = dropCheck.check(player);

        if (cancelled) {
            // Cancelling drop events is not save (in certain circumstances
            // items will disappear completely). So don't do it and kick
            // players instead by default

            // event.setCancelled(true);
        }
    }

    /**
     * We listen to PlayerInteractEvent for the instantEat and instantBow
     * checks
     * 
     * @param event
     *            The PlayerInteractEvent
     */
    @EventHandler(
            priority = EventPriority.LOWEST)
    public void interact(final PlayerInteractEvent event) {

        // Only interested in right-clicks while holding an item
        if (!event.hasItem()
                || !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;

        final NCPPlayer player = NCPPlayer.getPlayer(event.getPlayer());
        final InventoryData data = (InventoryData) getData(player);

        if (event.getItem().getType() == Material.BOW)
            // It was a bow, the player starts to pull the string
            // Remember this time
            data.lastBowInteractTime = System.currentTimeMillis();
        else if (CheckUtils.isFood(event.getItem())) {
            // It was food, the player starts to eat some food
            // Remember this time and the type of food
            data.foodMaterial = event.getItem().getType();
            data.lastEatInteractTime = System.currentTimeMillis();
        } else {
            // Nothing that we are interested in, reset data
            data.lastBowInteractTime = 0;
            data.lastEatInteractTime = 0;
            data.foodMaterial = null;
        }
    }
}
