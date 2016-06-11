package fr.neatmonster.nocheatplus.components.location;

/**
 * Simple immutable block position.
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

    // TODO: equals vs. IGetBlockPosition, Coord(Hash)Map compatible hashCode.

}
