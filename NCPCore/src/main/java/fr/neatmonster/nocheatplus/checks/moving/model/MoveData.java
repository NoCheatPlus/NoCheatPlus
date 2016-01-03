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
     * Start point of a move, or a static location (join/teleport).
     */
    public final LocationData from = new LocationData();

    /**
     * Indicate if coordinates for a move end-point and distances are present.
     * Always set on setPositions call.
     */
    public boolean toIsValid = false; // Must initialize.

    /////////////////////////////////////////////////////////////////////
    // Only set if a move end-point is set, i.e. toIsValid set to true.
    /////////////////////////////////////////////////////////////////////

    // Coordinates and distances.

    /**
     * End point of a move.
     */
    public final LocationData to = new LocationData();

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
     * Typical maximum walk speed, accounting for player capabilities. Set in
     * SurvivalFly.check.
     */
    public double walkSpeed;

    // Properties involving the environment.

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

    /**
     * Somehow the player has touched ground with this move (including
     * workarounds), thus the client might move up next move. This flag is only
     * updated by from/to.onGround, if MoveData.setExtraProperties is called for
     * this instance.
     */
    public boolean touchedGround;

    /**
     * Set if touchedGround has been set due to applying a workaround
     * exclusively.
     */
    public boolean touchedGroundWorkaround;

    // Bounds set by checks.

    /**
     * Allowed horizontal base distance (as if moving off the spot, excluding
     * bunny/friction). Set in SurvivalFly.check.
     */
    public double hAllowedDistanceBase;

    /**
     * Allowed horizontal distance (including frictions, workarounds like bunny
     * hopping). Set in SurvivalFly.check.
     */
    public double hAllowedDistance;

    /** This move was a bunny hop. */
    public boolean bunnyHop;

    // TODO: verVel/horvel used?

    // Meta stuff.

    /**
     * The fly check that was using the current data. One of MOVING_SURVIVALFLY,
     * MOVING_CREATIVEFLY, UNKNOWN.
     */
    public CheckType flyCheck;
    
    public boolean mightBeMultipleMoves;

    private void setPositions(final PlayerLocation from, final PlayerLocation to) {
        this.from.setLocation(from);
        this.to.setLocation(to);
        yDistance = this.to.y - this.from.y;
        hDistance = TrigUtil.distance(this.from.x, this.from.z, this.to.x, this.to.z);
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
        from.setLocation(x, y, z, yaw, pitch);
        toIsValid = false;
    }

    private void resetBase() {
        // Reset extra properties.
        from.extraPropertiesValid = false;
        to.extraPropertiesValid = false;
        // Properties of the player.
        walkSpeed = 0.2;
        // Properties involving the environment.
        headObstructed = false;
        downStream = false;
        touchedGround = false;
        touchedGroundWorkaround = false;
        bunnyHop = false;
        // Bounds set by checks.
        hAllowedDistanceBase = 0.0;
        hAllowedDistance = 0.0;
        // Meta stuff.
        flyCheck = CheckType.UNKNOWN;
        mightBeMultipleMoves = false;
        // Done.
        valid = true;
    }

    /**
     * Set some basic data and reset all other properties properly. Does not set
     * extra properties for locations.
     * 
     * @param from
     * @param to
     */
    public void set(final PlayerLocation from, final PlayerLocation to) {
        setPositions(from, to);
        resetBase();
        // TODO: this.from/this.to setExtraProperties ?
    }

    /**
     * Set with join / teleport / set-back. Does not set extra properties for
     * locations.
     * 
     * @param loc
     */
    public void set(final PlayerLocation loc) {
        setPositions(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        resetBase();
    }

    /**
     * Set with join / teleport / set-back, also set extra properties.
     * 
     * @param loc
     */
    public void setWithExtraProperties(final PlayerLocation loc) {
        set(loc);
        from.setExtraProperties(loc);
        if (this.from.onGround) {
            this.touchedGround = true;
        }
    }

    /**
     * Update extra properties (onGround and other) within LocationData (from,
     * to), update touchedGround.
     * 
     * @param from
     * @param to
     */
    public void setExtraProperties(final PlayerLocation from, final PlayerLocation to) {
        this.from.setExtraProperties(from);
        this.to.setExtraProperties(to);
        if (this.from.onGround || this.to.onGround) {
            this.touchedGround = true;
        }
    }

    /**
     * Fast invalidation: just set the flags.
     */
    public void invalidate() {
        valid = false;
        toIsValid = false;
        from.extraPropertiesValid = false;
        to.extraPropertiesValid = false;
    }

}
