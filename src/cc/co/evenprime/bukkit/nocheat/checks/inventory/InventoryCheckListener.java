package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import java.util.LinkedList;
import java.util.List;
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
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

public class InventoryCheckListener implements Listener, EventManager {

    private final DropCheck       dropCheck;
    private final InstantBowCheck instantBowCheck;
    private final InstantEatCheck instantEatCheck;

    private final NoCheat         plugin;

    public InventoryCheckListener(NoCheat plugin) {

        this.dropCheck = new DropCheck(plugin);
        this.instantBowCheck = new InstantBowCheck(plugin);
        this.instantEatCheck = new InstantEatCheck(plugin);

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    protected void handlePlayerDropItemEvent(final PlayerDropItemEvent event) {

        if(event.isCancelled())
            return;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final InventoryConfig cc = InventoryCheck.getConfig(player.getConfigurationStore());
        final InventoryData data = InventoryCheck.getData(player.getDataStore());

        if(player.hasPermission(Permissions.INVENTORY) || player.isDead()) {
            return;
        }

        boolean cancelled = false;

        // If it should be executed, do it
        if(cc.dropCheck && !player.hasPermission(Permissions.INVENTORY_DROP)) {
            cancelled = dropCheck.check(player, data, cc);
        }

        if(cancelled) {
            // Cancelling drop events is not save. So don't do it
            // and kick players instead by default
            //event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void interact(final PlayerInteractEvent event) {

        if(!event.hasItem() || !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;

        NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final InventoryData data = InventoryCheck.getData(player.getDataStore());

        if(event.getItem().getType() == Material.BOW) {
            data.lastBowInteractTime = System.currentTimeMillis();
        } else if(CheckUtil.isFood(event.getItem())) {
            // Remember food Material, because we don't have that info in the other event
            data.foodMaterial = event.getItem().getType();
            data.lastEatInteractTime = System.currentTimeMillis();
        } else {
            data.lastBowInteractTime = 0;
            data.lastEatInteractTime = 0;
            data.foodMaterial = null;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void foodchanged(final FoodLevelChangeEvent event) {
        if(!event.isCancelled() && event.getEntity() instanceof Player) {
            final NoCheatPlayer player = plugin.getPlayer((Player) event.getEntity());
            final InventoryConfig cc = InventoryCheck.getConfig(player.getConfigurationStore());
            final InventoryData data = InventoryCheck.getData(player.getDataStore());

            if(cc.eatCheck && !player.hasPermission(Permissions.INVENTORY_INSTANTEAT)) {

                boolean cancelled = instantEatCheck.check(player, event, data, cc);
                event.setCancelled(cancelled);
            }

            data.foodMaterial = null;
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void bowfired(final EntityShootBowEvent event) {
        if(!event.isCancelled() && event.getEntity() instanceof Player) {
            final NoCheatPlayer player = plugin.getPlayer((Player) event.getEntity());
            final InventoryConfig cc = InventoryCheck.getConfig(player.getConfigurationStore());

            if(cc.bowCheck && !player.hasPermission(Permissions.INVENTORY_INSTANTBOW)) {
                final InventoryData data = InventoryCheck.getData(player.getDataStore());
                boolean cancelled = instantBowCheck.check(player, event, data, cc);

                event.setCancelled(cancelled);
            }
        }
    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        InventoryConfig i = InventoryCheck.getConfig(cc);
        if(i.dropCheck)
            s.add("inventory.dropCheck");
        if(i.bowCheck)
            s.add("inventory.instantbow");
        if(i.eatCheck)
            s.add("inventory.instanteat");
        return s;
    }
}
