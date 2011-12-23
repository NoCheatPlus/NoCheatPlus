package cc.co.evenprime.bukkit.nocheat.checks;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCInventory;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;
import cc.co.evenprime.bukkit.nocheat.data.InventoryData;


public abstract class InventoryCheck extends Check {

    public InventoryCheck(NoCheat plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    public abstract boolean check(NoCheatPlayer player, InventoryData data, CCInventory cc);

    public abstract boolean isEnabled(CCInventory cc);

    @Override
    protected final ExecutionHistory getHistory(NoCheatPlayer player) {
        return player.getData().inventory.history;
    }
}
