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
package fr.neatmonster.nocheatplus.checks.moving.location.setback;

import org.bukkit.Location;
import org.bukkit.World;

import fr.neatmonster.nocheatplus.components.location.IGetLocationWithLook;
import fr.neatmonster.nocheatplus.components.location.ISetLocationWithLook;
import fr.neatmonster.nocheatplus.utilities.location.LocUtil;

/**
 * Mutable location with timing and validity information. Not complex objects
 * are stored (world name instead).
 * 
 * @author asofold
 *
 */
public class SetBackEntry implements IGetLocationWithLook, ISetLocationWithLook {

    // TODO: Support a hash for locations (can't be Location.hashCode()).

    private String worldName;
    private double x, y, z;
    private float pitch, yaw;
    private int time;
    private long msTime;
    // (use count, last use time, flags)
    private boolean isValid = false;

    public SetBackEntry set(final Location loc, final int time, final long msTime) {
        return set(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), time, msTime);
    }

    public SetBackEntry set(final IGetLocationWithLook loc, final int time, final long msTime) {
        return set(loc.getWorldName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), time, msTime);
    }

    // TODO: @throws
    public SetBackEntry set(final String worldName, final double x, final double y, final double z, final float yaw, final float pitch, final int time, final long msTime) {
        if (worldName == null) {
            throw new NullPointerException("World name must not be null.");
        }
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.time = time;
        this.msTime = msTime;
        this.isValid = true;
        return this;
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

    public int getTime() {
        return time;
    }

    public long getMsTime() {
        return msTime;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setMsTime(long msTime) {
        this.msTime = msTime;
    }

    public void setValid (boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * Convenience method to test with this instance is both valid and older
     * than the given one. The time field (getTime) is used for time comparison,
     * assuming both locations come from the same storage/context. Validity of
     * the given instance is not checked.
     * 
     * @param other
     * @return
     */
    public boolean isValidAndOlderThan(final SetBackEntry other) {
        return isValid && time < other.time; 
    }

    /**
     * Retrieve a Bukkit Location instance, using the given world.
     * 
     * @param world
     * @return A new Location object, containing the given world, ready to be
     *         used.
     * @throws IllegalStateException
     *             In case the set back entry is not valid.
     * @throws IllegalArgumentException
     *             In case the name of the given world does not match the stored
     *             one.
     */
    public Location getLocation(final World world) {
        if (!isValid) {
            throw new IllegalStateException("Can't return a Location instance from an invalid state.");
        }
        if (!world.getName().equals(worldName)) {
            throw new IllegalArgumentException("The name of the given world must equal the stored world name.");
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public int hashCode() {
        return LocUtil.hashCode(this);
    }

    // TODO: Equals !?

    @Override
    public String toString() {
        return "SetBackEntry(" + worldName + "/" + LocUtil.simpleFormat(this) + ")";
    }

}
