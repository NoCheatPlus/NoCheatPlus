package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

public class InventoryCheckListener implements Listener, EventManager {

    private final List<InventoryCheck> checks;
    private final NoCheat              plugin;

    public InventoryCheckListener(NoCheat plugin) {

        this.checks = new ArrayList<InventoryCheck>(1);

        // Don't use this check now, it's buggy
        this.checks.add(new DropCheck(plugin));

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    protected void handlePlayerDropItemEvent(final PlayerDropItemEvent event) {

        if(event.isCancelled())
            return;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final InventoryConfig cc = InventoryCheck.getConfig(player.getConfigurationStore());

        if(!cc.check || player.hasPermission(Permissions.INVENTORY) || player.isDead()) {
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
            // Cancelling drop events is not save. So don't do it
            // and kick players instead by default
            //event.setCancelled(true);
        }
    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        InventoryConfig i = InventoryCheck.getConfig(cc);
        if(i.check && i.dropCheck)
            s.add("inventory.dropCheck");
        return s;
    }
}
