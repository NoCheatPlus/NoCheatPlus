package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerDropItemEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.InventoryCheck;
import cc.co.evenprime.bukkit.nocheat.checks.inventory.DropCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCInventory;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.InventoryData;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.EventType;

public class InventoryEventManager extends EventManagerImpl {

    private final List<InventoryCheck> checks;

    public InventoryEventManager(NoCheat plugin) {
        super(plugin);

        this.checks = new ArrayList<InventoryCheck>(1);
        this.checks.add(new DropCheck(plugin));

        registerListener(Event.Type.PLAYER_DROP_ITEM, Priority.Lowest, true, plugin.getPerformance(EventType.INVENTORY));
    }

    @Override
    protected void handlePlayerDropItemEvent(final PlayerDropItemEvent event, final Priority priority) {

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final CCInventory cc = player.getConfiguration().inventory;

        if(!cc.check || player.hasPermission(Permissions.INVENTORY)) {
            return;
        }

        final InventoryData data = player.getData().inventory;

        boolean cancelled = false;

        for(InventoryCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                cancelled = check.check(player, data, cc);
            }
        }

        if(cancelled)
            event.setCancelled(cancelled);
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.inventory.check && cc.inventory.dropCheck)
            s.add("inventory.dropCheck");
        return s;
    }

}
