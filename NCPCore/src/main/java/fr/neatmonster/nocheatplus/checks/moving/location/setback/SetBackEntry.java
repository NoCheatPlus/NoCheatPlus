package fr.neatmonster.nocheatplus.checks.moving.location.setback;

import org.bukkit.Location;
import org.bukkit.World;

import fr.neatmonster.nocheatplus.components.location.ILocationWithLook;

/**
 * Mutable location with timing and validity information. Not complex objects
 * are stored (world name instead).
 * 
 * @author asofold
 *
 */
public class SetBackEntry implements ILocationWithLook {

    private String worldName;
    private double x, y, z;
    private float pitch, yaw;
    private int time;
    private long msTime;
    // (use count, last use time, flags)
    private boolean isValid = false;

    public SetBackEntry set(final Location loc, final int time, final long msTime) {
        worldName = loc.getWorld().getName();
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        pitch = loc.getPitch();
        yaw = loc.getYaw();
        this.time = time;
        this.msTime = msTime;
        isValid = true;
        return this;
    }

    public SetBackEntry set(final ILocationWithLook loc, final int time, final long msTime) {
        worldName = loc.getWorldName();
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        pitch = loc.getPitch();
        yaw = loc.getYaw();
        this.time = time;
        this.msTime = msTime;
        isValid = true;
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

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

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

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    /**
     * Retrieve a Bukkit Location instance, using the given world.
     * 
     * @param world
     * @return A new Location object, containing the given world, ready to be
     *         used.
     * @throws IllegalArgumentException
     *             In case the name of the given world does not match the stored
     *             one.
     * @throws IllegalStateException
     *             In case the set-back entry is not valid.
     */
    public Location getLocation(final World world) {
        if (!world.getName().equals(worldName)) {
            throw new IllegalArgumentException("The name of the given world must equal the stored world name.");
        }
        if (!isValid) {
            throw new IllegalStateException("Can't return a Location instance from an invalid state.");
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

}
