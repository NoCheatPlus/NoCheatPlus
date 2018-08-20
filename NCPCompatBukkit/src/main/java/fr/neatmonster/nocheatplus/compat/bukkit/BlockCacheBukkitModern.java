package fr.neatmonster.nocheatplus.compat.bukkit;

import org.bukkit.World;

/**
 * BlockCache for MCAccessBukkitModern.
 * 
 * @author asofold
 *
 */
public class BlockCacheBukkitModern extends BlockCacheBukkit {

    public BlockCacheBukkitModern(World world) {
        super(world);
    }

    @Override
    public int fetchData(int x, int y, int z) {
        // TODO: Might fake here too.
        return super.fetchData(x, y, z);
    }

    @Override
    public double[] fetchBounds(int x, int y, int z) {
        // TODO: Fetch what's possible to fetch/guess (...).
        return super.fetchBounds(x, y, z);
    }

}
