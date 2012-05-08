package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckUtils;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.SimpleLocation;

/**
 * The DirectionCheck will find out if a player tried to interact with something
 * that's not in his field of view.
 * 
 */
public class DirectionCheck extends BlockBreakCheck {

    public class DirectionCheckEvent extends BlockBreakEvent {

        public DirectionCheckEvent(final DirectionCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public DirectionCheck() {
        super("direction");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final BlockBreakConfig cc = getConfig(player);
        final BlockBreakData data = getData(player);

        final SimpleLocation brokenBlock = data.brokenBlockLocation;

        boolean cancel = false;

        // How far "off" is the player with his aim. We calculate from the
        // players eye location and view direction to the center of the target
        // block. If the line of sight is more too far off, "off" will be
        // bigger than 0
        double off = CheckUtils.directionCheck(player, brokenBlock.x + 0.5D, brokenBlock.y + 0.5D,
                brokenBlock.z + 0.5D, 1D, 1D, cc.directionPrecision);

        final long time = System.currentTimeMillis();

        if (off < 0.1D)
            // Player did likely nothing wrong
            // reduce violation counter to reward him
            data.directionVL *= 0.9D;
        else {
            // Player failed the check
            // Increment violation counter
            if (data.instaBrokenBlockLocation.equals(brokenBlock))
                // Instabreak block failures are very common, so don't be as
                // hard on people failing them
                off /= 5;

            // Add to the overall violation level of the check and add to
            // statistics
            data.directionVL += off;
            incrementStatistics(player, Id.BB_DIRECTION, off);

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.directionActions, data.directionVL);

            if (cancel)
                // if we should cancel, remember the current time too
                data.directionLastViolationTime = time;
        }

        // If the player is still in penalty time, cancel the event anyway
        if (data.directionLastViolationTime + cc.directionPenaltyTime > time) {
            // A saveguard to avoid people getting stuck in penalty time
            // indefinitely in case the system time of the server gets changed
            if (data.directionLastViolationTime > time)
                data.directionLastViolationTime = 0;

            // He is in penalty time, therefore request cancelling of the event
            return true;
        }

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final DirectionCheckEvent event = new DirectionCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(getData(player).directionVL);
        else
            return super.getParameter(wildcard, player);
    }
}
