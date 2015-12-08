package fr.neatmonster.nocheatplus.checks.net.model;

/**
 * An immutable location data object with coordinates and pitch and yaw.
 * 
 * @author asofold
 *
 */
public class DataLocation {

    // TODO: hashCode + equals.

    public final double x, y, z;
    public final float yaw, pitch;

    public DataLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Quick test for same coordinates and looking direction.
     * 
     * @param other
     * @return
     */
    public boolean isSameLocation(final DataLocation other) {
        return y == other.y && x == other.x && z == other.z && pitch == other.pitch && yaw == other.yaw;
    }

    /**
     * Quick test for same coordinates and looking direction.
     * 
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     * @return
     */
    public boolean isSameLocation(final double x, final double y, final double z, final float yaw, final float pitch) {
        return x == this.x && y == this.y && z == this.z && yaw == this.yaw && pitch == this.pitch;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(256);
        builder.append("Location(");
        builder.append(",x=");
        builder.append(x);
        builder.append(",y=");
        builder.append(y);
        builder.append(",z=");
        builder.append(z);
        builder.append(",pitch=");
        builder.append(pitch);
        builder.append(",yaw=");
        builder.append(yaw);
        builder.append(")");
        return builder.toString();
    }

}
