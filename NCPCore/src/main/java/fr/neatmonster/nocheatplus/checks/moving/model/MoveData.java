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


    //////////////////////////////////////////
    // Guaranteed to be initialized with set.
    //////////////////////////////////////////
    /**
     * Indicate if data has been set. Likely there will be sets of properties
     * with a flag for each such set.
     */
    public boolean valid = false;

    /** Double.MAX_VALUE if not available, e.g. after a teleport. */
    public double yDistance = Double.MAX_VALUE;
    /** Double.MAX_VALUE if not available, e.g. after a teleport. */
    public double hDistance = Double.MAX_VALUE;

    // TODO: Last coords, 
    // TODO: Velocity used, fly check, ...


    //////////////////////////////////////////////////////////
    // Reset with set, could be lazily initialized.
    //////////////////////////////////////////////////////////

    /**
     * Head is obstructed. Should expect descending next move, if in air. <br>
     * Set at the beginning of SurvivalFly.check, if either end point is not on ground.
     */
    public boolean headObstructed = false;

    // TODO: ground/reset/web/...

    /**
     * 
     * @param fromX
     * @param fromY
     * @param fromZ
     * @param fromYaw
     * @param fromPitch
     * @param toX
     * @param toY
     * @param toZ
     * @param toYaw
     * @param toPitch
     */
    private void setPositions(final double fromX, final double fromY, final double fromZ, final float fromYaw, final float fromPitch,
            final double toX, final double toY, final double toZ, final float toYaw, final float toPitch) {
        yDistance = toY - fromY;
        hDistance = TrigUtil.distance(fromX, fromZ, toX, toZ);
    }

    /**
     * Set with join / teleport / set-back.
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     */
    private void setPositions(final double x, final double y, final double z, final float yaw, final float pitch) {
        yDistance = Double.MAX_VALUE;
        hDistance = Double.MAX_VALUE;
    }

    private void resetBase() {
        headObstructed = false;
        valid = true;
    }

    /**
     * Set some basic data and reset all other properties properly.
     * 
     * @param from
     * @param to
     */
    public void set(final PlayerLocation from, final PlayerLocation to) {
        setPositions(from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch(),
                to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
        resetBase();
    }

    /**
     * Set with join / teleport / set-back.
     * @param x
     * @param y
     * @param z
     * @param yaw 
     * @param pitch 
     */
    public void set(final double x, final double y, final double z, final float yaw, final float pitch) {
        setPositions(x, y, z, yaw, pitch);
        resetBase();
    }

    /**
     * Fast invalidation: just set the flags.
     */
    public void invalidate() {
        valid = false;
    }

}
