package fr.neatmonster.nocheatplus.checks.moving.location.setback;

import org.bukkit.Location;
import org.bukkit.World;

import fr.neatmonster.nocheatplus.utilities.RichBoundsLocation;

/**
 * Mutable location with timing and validity information. Not complex objects
 * are stored (world name instead).
 * 
 * @author asofold
 *
 */
public class SetBackEntry {

    private String worldName;
    private double x, y, z;
    private float pitch, yaw;
    private int time;
    private long msTime;
    // (use count, last use time, flags)
    private boolean isValid = false;

    SetBackEntry set(Location loc, int time, long msTime) {
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

    SetBackEntry set(RichBoundsLocation loc, int time, long msTime) {
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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

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

    public String getWorldName() {
        return worldName;
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
     * 
     * @param world
     * @return A new Location object, containing the given world, ready to be used.
     * @throws IllegalArgumentException
     *             In case the name of the given world does not match the stored
     *             one.
     */
    public Location getLocation(final World world) {
        if (!world.getName().equals(worldName)) {
            throw new IllegalArgumentException("The name of the given world must equal the stored world name.");
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

}
