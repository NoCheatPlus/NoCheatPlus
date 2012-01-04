package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.events.EventManagerImpl;

public class InventoryEventManager extends EventManagerImpl {

    private final List<InventoryCheck> checks;

    public InventoryEventManager(NoCheat plugin) {
        super(plugin);

        this.checks = new ArrayList<InventoryCheck>(1);
        
        // Don't use this check now, it's buggy
        //this.checks.add(new DropCheck(plugin));

        //registerListener(Event.Type.PLAYER_DROP_ITEM, Priority.Lowest, true, plugin.getPerformance(EventType.INVENTORY));
        registerListener(Event.Type.PLAYER_TELEPORT, Priority.Monitor, true, null);
    }

    @Override
    protected void handlePlayerTeleportEvent(final PlayerTeleportEvent event, final Priority priority) {

        try {
        NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        if(InventoryCheck.getConfig(player.getConfigurationStore()).closebeforeteleports && event.getTo() != null && !(event.getTo().getWorld().equals(player.getPlayer().getWorld()))) {
            player.closeInventory();
        }
        } catch(NullPointerException e) {
            
        }
    }

    @Override
    protected void handlePlayerDropItemEvent(final PlayerDropItemEvent event, final Priority priority) {

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final CCInventory cc = InventoryCheck.getConfig(player.getConfigurationStore());

        if(!cc.check || player.hasPermission(Permissions.INVENTORY)) {
            return;
        }

        final InventoryData data = InventoryCheck.getData(player.getDataStore());

        boolean cancelled = false;

        for(InventoryCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                cancelled = check.check(player, data, cc);
            }
        }

        if(cancelled) {
            event.setCancelled(true);
            player.closeInventory();
        }
    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        CCInventory i = InventoryCheck.getConfig(cc);
        if(i.check && i.dropCheck)
            s.add("inventory.dropCheck");
        return s;
    }
}
