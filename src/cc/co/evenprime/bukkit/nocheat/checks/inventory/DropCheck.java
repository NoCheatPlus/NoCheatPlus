package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

public class DropCheck extends InventoryCheck {

    public DropCheck(NoCheat plugin) {
        super(plugin, "inventory.drop", Permissions.INVENTORY_DROP);
    }

    @Override
    public boolean check(NoCheatPlayer player, InventoryData data, CCInventory cc) {

        boolean cancel = false;

        final long time = System.currentTimeMillis() / 1000;

        if(data.dropLastTime + cc.dropTimeFrame <= time) {
            data.dropLastTime = time;
            data.dropCount = 0;
        }
        // Security check, if the system time changes
        else if(data.dropLastTime > time) {
            data.dropLastTime = Integer.MIN_VALUE;
        }

        data.dropCount++;

        if(data.dropCount > cc.dropLimit) {

            data.dropVL = data.dropCount - cc.dropLimit;
            data.dropTotalVL++;
            data.dropFailed++;

            cancel = executeActions(player, cc.dropActions.getActions(data.dropVL));
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(CCInventory cc) {
        return cc.dropCheck;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", getData(player.getDataStore()).dropVL);
        else
            return super.getParameter(wildcard, player);
    }
}
