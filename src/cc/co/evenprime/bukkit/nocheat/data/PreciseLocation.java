package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.Location;

public class PreciseLocation {

    public double x;
    public double y;
    public double z;

    public PreciseLocation() {
        reset();
    }

    public void set(Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
    }

    public void set(PreciseLocation location) {
        x = location.x;
        y = location.y;
        z = location.z;
    }

    public boolean isSet() {
        return x != Double.MAX_VALUE;
    }
    public void reset() {
        x = Double.MAX_VALUE;
        y = Double.MAX_VALUE;
        z = Double.MAX_VALUE;
    }

    public boolean equals(Location location) {
        return location.getX() == x && location.getY() == y && location.getZ() == z;
    }
}
