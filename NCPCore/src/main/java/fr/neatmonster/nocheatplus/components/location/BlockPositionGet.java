package fr.neatmonster.nocheatplus.components.location;

import fr.neatmonster.nocheatplus.utilities.ds.map.CoordHash;

/**
 * Simple immutable block position. Both hashCode and equals are implemented,
 * with equals accepting any IGetBlockPosition instance for comparison of block
 * coordinates.
 * 
 * @author asofold
 *
 */
public class BlockPositionGet implements IGetBlockPosition {

    private final int x;
    private final int y;
    private final int z;

    public BlockPositionGet(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getBlockX() {
        return x;
    }

    @Override
    public int getBlockY() {
        return y;
    }

    @Override
    public int getBlockZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return CoordHash.hashCode3DPrimes(x, y, z);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof IGetBlockPosition) {
            final IGetBlockPosition other = (IGetBlockPosition) obj;
            return x == other.getBlockX() && y == other.getBlockY() && z == other.getBlockZ();
        }
        return false;
    }

}
