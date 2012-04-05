package me.neatmonster.nocheatplus.checks.inventory;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.data.Statistics.Id;

import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 * The InstantEatCheck will find out if a player eats his food too fast
 */
public class InstantEatCheck extends InventoryCheck {

    public InstantEatCheck(final NoCheatPlus plugin) {
        super(plugin, "inventory.instanteat");
    }

    public boolean check(final NoCheatPlusPlayer player, final FoodLevelChangeEvent event, final InventoryData data,
            final InventoryConfig cc) {

        // Hunger level change seems to not be the result of eating
        if (data.foodMaterial == null || event.getFoodLevel() <= player.getPlayer().getFoodLevel())
            return false;

        boolean cancelled = false;

        final long time = System.currentTimeMillis();
        // rough estimation about how long it should take to eat
        final long expectedTimeWhenEatingFinished = data.lastEatInteractTime + 700;

        if (expectedTimeWhenEatingFinished < time)
            // Acceptable, reduce VL to reward the player
            data.instantEatVL *= 0.60D;
        else if (data.lastEatInteractTime > time)
            // Security test, if time ran backwards, reset
            data.lastEatInteractTime = 0;
        else {
            // Player was too fast, increase violation level and statistics
            final int vl = (int) (expectedTimeWhenEatingFinished - time) / 100;
            data.instantEatVL += vl;
            incrementStatistics(player, Id.INV_EAT, vl);

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancelled = executeActions(player, cc.eatActions, data.instantEatVL);
        }

        return cancelled;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).instantEatVL);
        else if (wildcard == ParameterName.FOOD)
            return getData(player).foodMaterial.toString();
        else
            return super.getParameter(wildcard, player);
    }

}
