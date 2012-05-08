package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityShootBowEvent;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * The InstantBowCheck will find out if a player pulled the string of his bow
 * too fast
 */
public class InstantBowCheck extends InventoryCheck {

    public class InstantBowCheckEvent extends InventoryEvent {

        public InstantBowCheckEvent(final InstantBowCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public InstantBowCheck() {
        super("instantbow");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final InventoryConfig cc = getConfig(player);
        final InventoryData data = getData(player);
        final EntityShootBowEvent event = (EntityShootBowEvent) args[0];

        boolean cancelled = false;

        final long time = System.currentTimeMillis();

        // How fast will the arrow be?
        final float bowForce = event.getForce();

        // Rough estimation of how long pulling the string should've taken
        final long expectedTimeWhenStringDrawn = data.lastBowInteractTime + (int) (bowForce * bowForce * 700F);

        if (expectedTimeWhenStringDrawn < time)
            // The player was slow enough, reward him by lowering the vl
            data.instantBowVL *= 0.90D;
        else if (data.lastBowInteractTime > time)
            // Security check if time ran backwards, reset
            data.lastBowInteractTime = 0;
        else {
            // Player was too fast, increase violation level and statistics
            final int vl = (int) (expectedTimeWhenStringDrawn - time) / 100;
            data.instantBowVL += vl;
            incrementStatistics(player, Id.INV_BOW, vl);

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancelled = executeActions(player, cc.bowActions, data.instantBowVL);
        }

        return cancelled;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final InstantBowCheckEvent event = new InstantBowCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).instantBowVL));
        else
            return super.getParameter(wildcard, player);
    }
}
