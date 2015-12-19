package fr.neatmonster.nocheatplus.checks.moving.model;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Carry data of a move, involving from- and to- locations. This is for
 * temporary storage and often resetting, also to encapsulate some data during
 * checking.
 * 
 * @author asofold
 *
 */
public class MoveData {

    // TODO: Use objects for from and to (could lead to redesign, think of PlayerLocation)?

    /**
     * Not enforced, but meant to be an invalidated MoveData instance.
     */
    public static final MoveData alwaysInvalidated = new MoveData();

    //////////////////////////////////////////
    // Guaranteed to be initialized with set.
    //////////////////////////////////////////
    /**
     * Indicate if data has been set. Likely there will be sets of properties
     * with a flag for each such set.
     */
    public boolean valid = false; // Must initialize.

    /**
     * Start position coordinates.
     */
    public double fromX, fromY, fromZ;
    /** Looking direction of the start position. */
    public float fromYaw, fromPitch;

    /**
     * Indicate if coordinates for a move end-point and distances are present.
     * Always set on setPositions call.
     */
    public boolean toIsValid = false; // Must initialize.

    /////////////////////////////////////////////////////////////////////
    // Only set if a move end-point is set, i.e. toIsValid set to true.
    /////////////////////////////////////////////////////////////////////

    // Coordinates and distances.

    /** End-point of a move. Only valid if toIsValid is set to true. */
    public double toX, toY, toZ;

    /** Looking direction of a move end-point. Only valid if toIsValid is set to true. */
    public float toYaw, toPitch;

    /**
     * The vertical distance covered by a move. Note the sign for moving up or
     * down. Only valid if toIsValid is set to true.
     */
    public double yDistance;

    /**
     * The horizontal distance covered by a move. Note the sign for moving up or
     * down. Only valid if toIsValid is set to true.
     */
    public double hDistance;

    //////////////////////////////////////////////////////////
    // Reset with set, could be lazily set during checking.
    //////////////////////////////////////////////////////////

    // Properties of the player.

    /**
     * Walk speed modifier. Set in SurvivalFly.check.
     */
    public double walkSpeed;

    // Special properties of the environment.

    /**
     * Head is obstructed. Should expect descending next move, if in air. <br>
     * Set at the beginning of SurvivalFly.check, if either end-point is not on
     * ground.
     */
    public boolean headObstructed;

    /**
     * Player is moving downstream in flowing liquid (horizontal rather). Set in
     * SurvivalFly.check.
     */
    public boolean downStream;

    // Meta stuff.

    /**
     * The fly check that was using the current data. One of MOVING_SURVIVALFLY,
     * MOVING_CREATIVEFLY, UNKNOWN.
     */
    public CheckType flyCheck;

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
        this.fromX = fromX;
        this.fromY = fromY;
        this.fromZ = fromZ;
        this.fromYaw = fromYaw;
        this.fromPitch = fromPitch;
        this.toX = toX;
        this.toY = toY;
        this.toZ = toZ;
        this.toYaw = toYaw;
        this.toPitch = toPitch;
        toIsValid = true;
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
        this.fromX = x;
        this.fromY = y;
        this.fromZ = z;
        this.fromYaw = yaw;
        this.fromPitch = pitch;
        toIsValid = false;
    }

    private void resetBase() {
        // Properties of the player.
        walkSpeed = 0.2;
        // Special properties of the environment.
        headObstructed = false;
        downStream = false;
        // Meta stuff.
        flyCheck = CheckType.UNKNOWN;
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
        toIsValid = false;
    }

}
