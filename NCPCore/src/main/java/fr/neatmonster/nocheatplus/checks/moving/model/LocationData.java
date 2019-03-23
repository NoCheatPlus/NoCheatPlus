/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.moving.model;

import fr.neatmonster.nocheatplus.components.location.IGetLocationWithLook;
import fr.neatmonster.nocheatplus.utilities.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.location.RichBoundsLocation;

/**
 * Some useful data about a location. Used in MoveData to keep track of
 * past-move properties.
 * 
 * @author asofold
 *
 */
public class LocationData implements IGetLocationWithLook {

    private String worldName;

    private double x, y, z;

    private float yaw, pitch;

    /** Must be checked before using any of the flags. */
    public boolean extraPropertiesValid = false;
    /** Basic environmental properties. */
    public boolean onClimbable, inWeb, inLava, inWater, inLiquid, onGround, onIce, onSoulSand;
    /** Aggregate properties (reset means potentially resetting fall damage). */
    public boolean resetCond, onGroundOrResetCond;

    /**
     * Set same as other.
     * @param other
     */
    public void set(final LocationData other) {
        setLocation(other.worldName, other.x, other.y, other.z, other.yaw, other.pitch);
        setExtraProperties(other);
    }

    /**
     * Set all that can be set.
     * @param loc
     */
    public void set(final RichBoundsLocation loc) {
        setLocation(loc);
        setExtraProperties(loc);
    }

    /**
     * Set location data.
     * @param loc
     */
    public void setLocation(final IGetLocationWithLook loc) {
        setLocation(loc.getWorldName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /**
     * Set location data.
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     */
    public void setLocation(final String worldName, final double x, final double y, final double z, final float yaw, final float pitch) {
        this.worldName = worldName;
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
    public void setExtraProperties(final RichBoundsLocation loc) {
        loc.collectBlockFlags(); // Just ensure.
        onClimbable = loc.isOnClimbable();
        inWeb = loc.isInWeb();
        onSoulSand = loc.isOnSoulSand();
        inLiquid = loc.isInLiquid();
        if (inLiquid) {
            inLava = loc.isInLava();
            inWater = loc.isInWater();
        }
        else {
            inLava = inWater = false;
        }
        onGround = loc.isOnGround();
        onIce = loc.isOnIce();
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
            onIce = other.onIce;
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
        onIce = false;
        resetCond = false;
        onGroundOrResetCond = false;
    }

    public void addExtraProperties(final StringBuilder builder) {
        if (!extraPropertiesValid) {
            return;
        }
        if (onGround) {
            builder.append(" ground");
        }
        if (resetCond) {
            builder.append(" resetcond");
        }
    }

    @Override
    public String getWorldName() {
        return worldName;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public float getYaw() {
        return yaw;
    }

    @Override
    public int hashCode() {
        return LocUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return "LocationData(" + worldName + "/" + LocUtil.simpleFormat(this) + ")";
    }

}
