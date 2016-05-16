package fr.neatmonster.nocheatplus.components.location;

import org.bukkit.Location;

import fr.neatmonster.nocheatplus.checks.moving.location.LocUtil;

public class SimplePositionWithLook implements IPositionWithLook {

    // TODO: Package organization...

    private double x, y, z;
    private float yaw, pitch;

    /**
     * Empty constructor, undefined variable states (actually java defaults).
     */
    public SimplePositionWithLook() {
    }

    public SimplePositionWithLook(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Convenience method.
     * @param loc
     */
    public void set(final Location loc)  {
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
        yaw = loc.getYaw();
        pitch = loc.getPitch();
    }

    /**
     * Convenience method.
     * @param pos
     */
    public void set(final IGetPositionWithLook pos) {
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
        yaw = pos.getYaw();
        pitch = pos.getPitch();
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
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public String toString() {
        return "SimplePositionWithLook(" + LocUtil.simpleFormat(this) + ")";
    }

    // TODO: hashCode, equals?

}
