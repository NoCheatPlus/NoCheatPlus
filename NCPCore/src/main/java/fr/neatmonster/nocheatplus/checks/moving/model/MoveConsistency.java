package fr.neatmonster.nocheatplus.checks.moving.model;

import org.bukkit.Location;

import fr.neatmonster.nocheatplus.components.location.IGetPosition;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Consistency of a Location/thing concerning a move with a from and a to location.
 * @author mc_dev
 *
 */
public enum MoveConsistency {

    // TODO: Doesn't make much sense, this way (latency can mean much greater distances and other issues are different).

    /**
     * Consistent with the to-location. Highest priority.
     */
    TO,
    /**
     * Consistent with the from-location.
     */
    FROM,
    /**
     * Not consistent. Lowest priority.
     */
    INCONSISTENT;



    public static final double maxDistance = 1.25;
    public static final double maxDistanceSq = maxDistance * maxDistance;

    public static MoveConsistency getConsistency(final Location from, final Location to, final Location loc) {
        if (to != null && TrigUtil.distanceSquared(to, loc) < maxDistanceSq) {
            return TO;
        } else if (from != null && TrigUtil.distanceSquared(from, loc) < maxDistanceSq) {
            return FROM;
        } else {
            return INCONSISTENT;
        }
    }

    public static MoveConsistency getConsistency(final IGetPosition from, final IGetPosition to, final Location loc) {
        if (to != null && TrigUtil.distanceSquared(to, loc) < maxDistanceSq) {
            return TO;
        } else if (from != null && TrigUtil.distanceSquared(from, loc) < maxDistanceSq) {
            return FROM;
        } else {
            return INCONSISTENT;
        }
    }

    public static MoveConsistency getConsistency(final MoveData thisMove, final Location loc) {
        return getConsistency(thisMove.from, thisMove.to, loc);
    }

}
