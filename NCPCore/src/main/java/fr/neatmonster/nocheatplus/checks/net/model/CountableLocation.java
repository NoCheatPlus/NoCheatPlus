package fr.neatmonster.nocheatplus.checks.net.model;

/**
 * Location allowing for a count and a time, both mutable. Meant for queues,
 * increasing count and updating time with repetition.
 * 
 * @author asofold
 *
 */
public class CountableLocation extends DataLocation {

    public int count;
    public long time;

    public CountableLocation(double x, double y, double z, float yaw, float pitch, int count, long time) {
        super(x, y, z, yaw, pitch);
        this.time = time;
        this.count = count;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(256);
        builder.append("CountableLocation(");
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
        builder.append(",count=");
        builder.append(count);
        // Skip time for now.
        builder.append(")");
        return builder.toString();
    }

}
