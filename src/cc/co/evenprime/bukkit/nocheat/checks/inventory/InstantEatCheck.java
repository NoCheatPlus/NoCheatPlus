package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import java.util.Locale;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

public class InstantEatCheck extends InventoryCheck {

    public InstantEatCheck(NoCheat plugin) {
        super(plugin, "inventory.instanteat");
    }

    public boolean check(NoCheatPlayer player, FoodLevelChangeEvent event, InventoryData data, InventoryConfig cc) {

        // Seems to be not the result of eating
        if(data.foodMaterial == null || event.getFoodLevel() <= player.getPlayer().getFoodLevel())
            return false;

        boolean cancelled = false;

        long time = System.currentTimeMillis();
        long expectedTimeWhenEatingFinished = data.lastEatInteractTime + 700;

        if(expectedTimeWhenEatingFinished < time) {
            // Acceptable, reduce VL
            data.instantEatVL *= 0.98D;
        } else if(data.lastEatInteractTime > time) {
            // Security, if time ran backwards, reset
            data.lastEatInteractTime = 0;
        } else {
            // Seems fishy, increase violation level
            int vl = ((int) (expectedTimeWhenEatingFinished - time)) / 100;
            data.instantEatVL += vl;
            incrementStatistics(player, Id.INV_EAT, vl);
            cancelled = executeActions(player, cc.eatActions.getActions(data.instantEatVL));
        }

        return cancelled;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).instantEatVL);
        else if(wildcard == ParameterName.FOOD)
            return getData(player.getDataStore()).foodMaterial.toString();
        else
            return super.getParameter(wildcard, player);
    }

}
