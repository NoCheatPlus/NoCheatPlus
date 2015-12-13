package fr.neatmonster.nocheatplus.checks.moving.model;

import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Carry data of a move, involving from- and to- locations.
 * 
 * @author asofold
 *
 */
public class MoveData {


    /////////////////////////////
    // Guaranteed to be set.
    /////////////////////////////
    /**
     * Indicate if data has been set. Likely there will be sets of properties
     * with a flag for each such set.
     */
    public boolean valid = false;

    public double yDistance = Double.MAX_VALUE;
    public double hDistance = Double.MAX_VALUE;

    // TODO: Last coords, 
    // TODO: Velocity used, fly check, ...


    /////////////////////////////
    // Not guaranteed to be set.
    /////////////////////////////

    public boolean headObstructed = false;

    // TODO: ground/reset/web/...


    /**
     * Set the minimal properties, likely to be used.
     * 
     * @param from
     * @param to
     */
    public void set(final PlayerLocation from, final PlayerLocation to) {
        set(from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch(),
                to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
    }

    /**
     * Set some basic move edge data and reset all other properties properly.
     * @param fromX
     * @param fromY
     * @param fromZ
     * @param toX
     * @param toY
     * @param toZ
     */
    public void set(final double fromX, final double fromY, final double fromZ, final float fromYaw, final float fromPitch,
            final double toX, final double toY, final double toZ, final float toYaw, final float toPitch) {
        yDistance = toY - fromY;
        hDistance = TrigUtil.distance(fromX, fromZ, toX, toZ);
        headObstructed = false;
        // Set Valid last.
        valid = true;
    }

    /**
     * Set with teleport / set-back.
     * @param x
     * @param y
     * @param z
     * @param yaw 
     * @param pitch 
     */
    public void set(final double x, final double y, final double z, final float yaw, final float pitch) {
        yDistance = Double.MAX_VALUE; // TODO: 0 ?
        hDistance = Double.MAX_VALUE; // TODO: 0 ?
        headObstructed = false;
        valid = true;
    }

    /**
     * Fast invalidation: just set the flags.
     */
    public void invalidate() {
        valid = false;
    }

    //    public void reset() {
    //        // TODO: Might not need this method: it's always set(...) or set invalid to true.
    //        valid = false;
    //        // Reset properties after resetting valid.
    //        yDistance = Double.MAX_VALUE;
    //        hDistance = Double.MAX_VALUE;
    //        headObstructed = false;
    //    }

}
