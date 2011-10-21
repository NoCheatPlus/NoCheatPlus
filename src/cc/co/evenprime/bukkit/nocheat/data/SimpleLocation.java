package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * To avoid constantly creating and referencing "Location" objects, which
 * in turn reference a whole lot of other unnecessary stuff, rather use
 * our own "Location" object which is easily reusable.
 * 
 */
public final class SimpleLocation {

    public int x;
    public int y;
    public int z;
    
    public SimpleLocation() {
        reset();
    }

    public final boolean equals(Block block) {
        return block.getX() == x && block.getY() == y && block.getZ() == z;
    }

    public final void set(Block block) {
        x = block.getX();
        y = block.getY();
        z = block.getZ();
    }

    public final void setLocation(Location location) {
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
    }
    
    public final boolean isSet() {
        return x != Integer.MAX_VALUE;
    }
    public final void reset() {
        x = Integer.MAX_VALUE;
        y = Integer.MAX_VALUE;
        z = Integer.MAX_VALUE;
    }

}
