package fr.neatmonster.nocheatplus.utilities.locations;

import org.bukkit.Location;

/**
 * A class to store x,y,z triple data, instead of using bukkits Location
 * objects, which can't be easily recycled
 * 
 */
public final class PreciseLocation {

    public double x;
    public double y;
    public double z;

    public PreciseLocation() {
        reset();
    }

    public final boolean equals(final Location location) {
        return location.getX() == x && location.getY() == y && location.getZ() == z;
    }

    public final boolean isSet() {
        return x != Double.MAX_VALUE;
    }

    public final void reset() {
        x = Double.MAX_VALUE;
        y = Double.MAX_VALUE;
        z = Double.MAX_VALUE;
    }

    public final void set(final Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
    }

    public final void set(final PreciseLocation location) {
        x = location.x;
        y = location.y;
        z = location.z;
    }
}
