package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * The InstantEatCheck will find out if a player eats his food too fast
 */
public class InstantEatCheck extends InventoryCheck {

    public class InstantEatCheckEvent extends InventoryEvent {

        public InstantEatCheckEvent(final InstantEatCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

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
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final InstantEatCheckEvent event = new InstantEatCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).instantEatVL));
        else if (wildcard == ParameterName.FOOD)
            return getData(player).foodMaterial.toString();
        else
            return super.getParameter(wildcard, player);
    }

}
