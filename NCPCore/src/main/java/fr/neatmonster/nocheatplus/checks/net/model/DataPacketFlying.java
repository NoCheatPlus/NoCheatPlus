package fr.neatmonster.nocheatplus.checks.net.model;

public class DataPacketFlying {

    // TODO: Use MAX_VALUE for not set doubles/floats?
    // TODO: Consider private + access methods.
    // TODO: Consider AlmostBoolean for fault tolerance ?

    public final boolean onGround;
    public final boolean hasPos;
    public final boolean hasLook;
    public final double x, y, z;
    public final float yaw, pitch;
    public final long time;

    public DataPacketFlying(boolean onGround, long time) {
        this.onGround = onGround;
        hasPos = false;
        hasLook = false;
        x = y = z = 0.0;
        yaw = pitch = 0f;
        this.time = time;
    }

    public DataPacketFlying(boolean onGround, float yaw, float pitch, long time) {
        this.onGround = onGround;
        hasPos = false;
        hasLook = true;
        x = y = z = 0.0;
        this.yaw = yaw;
        this.pitch = pitch;
        this.time = time;
    }

    public DataPacketFlying(boolean onGround, double x, double y, double z, long time) {
        this.onGround = onGround;
        hasPos = true;
        hasLook = false;
        this.x = x;
        this.y = y;
        this.z = z;
        yaw = pitch = 0f;
        this.time = time;
    }

    public DataPacketFlying(boolean onGround, double x, double y, double z, float yaw, float pitch, long time) {
        this.onGround = onGround;
        hasPos = true;
        hasLook = true;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.time = time;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(256);
        builder.append("Flying(ground=");
        builder.append(onGround);
        if (hasPos) {
            builder.append(",x=");
            builder.append(x);
            builder.append(",y=");
            builder.append(y);
            builder.append(",z=");
            builder.append(z);
        }
        if (hasLook) {
            builder.append(",pitch=");
            builder.append(pitch);
            builder.append(",yaw=");
            builder.append(yaw);
        }
        // Skip time for now.
        builder.append(")");
        return builder.toString();
    }

}
