package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import java.util.Locale;
import org.bukkit.event.entity.EntityShootBowEvent;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

public class InstantBowCheck extends InventoryCheck {

    public InstantBowCheck(NoCheat plugin) {
        super(plugin, "inventory.instantbow", Permissions.INVENTORY_INSTANTBOW);
    }

    public boolean check(NoCheatPlayer player, EntityShootBowEvent event, InventoryData data, InventoryConfig cc) {

        boolean cancelled = false;

        long time = System.currentTimeMillis();
        float bowForce = event.getForce();
        long expectedTimeWhenStringDrawn = data.lastBowInteractTime + (int) (bowForce * bowForce * 700F);

        if(expectedTimeWhenStringDrawn < time) {
            // Acceptable, reduce VL
            data.instantBowVL *= 0.98D;
        } else if(data.lastBowInteractTime > time) {
            // Security, if time ran backwards, reset
            data.lastBowInteractTime = 0;
        } else {
            // Seems fishy, increase violation level
            int vl = ((int) (expectedTimeWhenStringDrawn - time)) / 100;
            data.instantBowVL += vl;
            incrementStatistics(player, Id.INV_BOW, vl);
            cancelled = executeActions(player, cc.bowActions.getActions(data.instantBowVL));
        }

        return cancelled;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).instantBowVL);
        else
            return super.getParameter(wildcard, player);
    }
}
