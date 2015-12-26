package fr.neatmonster.nocheatplus.checks.moving.model;

import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/**
 * Some useful data about a location. Used in MoveData to keep track of
 * past-move properties.
 * 
 * @author asofold
 *
 */
public class LocationData {

    public double x, y, z;

    public float yaw, pitch;

    /** Must be checked before using any of the flags. */
    public boolean extraPropertiesValid = false;
    /** Basic environmental properties. */
    public boolean onClimbable, inWeb, inLava, inWater, inLiquid, onGround;
    /** Aggregate properties (reset means potentially resetting fall damage). */
    public boolean resetCond, onGroundOrResetCond;

    /**
     * Set same as other.
     * @param other
     */
    public void set(final LocationData other) {
        setLocation(other.x, other.y, other.z, other.yaw, other.pitch);
        setExtraProperties(other);
    }

    /**
     * Set all that can be set.
     * @param loc
     */
    public void set(final PlayerLocation loc) {
        setLocation(loc);
        setExtraProperties(loc);
    }

    /**
     * Set location data.
     * @param loc
     */
    public void setLocation(final PlayerLocation loc) {
        setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /**
     * Set location data.
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     */
    public void setLocation(final double x, final double y, final double z, final float yaw, final float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Set extra properties based on the given PlayerLocation instance.
     * 
     * @param loc
     */
    public void setExtraProperties(final PlayerLocation loc) {
        loc.collectBlockFlags(); // Just ensure.
        onClimbable = loc.isOnClimbable();
        inWeb = loc.isInWeb();
        inLiquid = loc.isInLiquid();
        if (inLiquid) {
            inLava = loc.isInLava();
            inWater = loc.isInWater();
        }
        else {
            inLava = inWater = false;
        }
        onGround = loc.isOnGround();
        resetCond = inLiquid || inWeb || onClimbable;
        onGroundOrResetCond = onGround || resetCond;
        // Set valid flag last.
        extraPropertiesValid = true;
    }

    /**
     * Set extra properties same as the given LocationData instance.
     * 
     * @param loc
     */
    public void setExtraProperties(final LocationData other) {
        if (other.extraPropertiesValid) {
            onClimbable = other.onClimbable;
            inWeb = other.inWeb;
            inLiquid = other.inLiquid;
            inLava = other.inLava;
            inWater = other.inWater;
            onGround = other.onGround;
            // Use aggregate properties 1:1, allowing for hacks.
            resetCond = other.resetCond;
            onGroundOrResetCond = other.onGroundOrResetCond;
        }
        // Set valid flag last.
        extraPropertiesValid = other.extraPropertiesValid;
    }

    public void resetExtraProperties() {
        extraPropertiesValid = false;
        onClimbable = false;
        inWeb = false;
        inLiquid = false;
        inLava = false;
        inWater = false;
        onGround = false;
        resetCond = false;
        onGroundOrResetCond = false;
    }

}
