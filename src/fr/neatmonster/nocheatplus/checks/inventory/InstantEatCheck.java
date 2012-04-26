package fr.neatmonster.nocheatplus.checks.inventory;

import java.util.Locale;

import org.bukkit.event.entity.FoodLevelChangeEvent;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * The InstantEatCheck will find out if a player eats his food too fast
 */
public class InstantEatCheck extends InventoryCheck {

    public InstantEatCheck() {
        super("instanteat");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final InventoryConfig cc = getConfig(player);
        final InventoryData data = getData(player);
        final FoodLevelChangeEvent event = (FoodLevelChangeEvent) args[0];

        // Hunger level change seems to not be the result of eating
        if (data.foodMaterial == null || event.getFoodLevel() <= player.getBukkitPlayer().getFoodLevel())
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
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).instantEatVL);
        else if (wildcard == ParameterName.FOOD)
            return getData(player).foodMaterial.toString();
        else
            return super.getParameter(wildcard, player);
    }

}
