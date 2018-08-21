package fr.neatmonster.nocheatplus.compat.bukkit.model;

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

public interface ShapeModel<W> {

    // TODO: Rather fill in all into node directly (data as well), avoid redundant casting etc.
    // TODO: Best route passable workaround through here too (base on a flag), + getGroundMinHeight?.

    // TODO: Refine +- might have BukkitBlockCacheNode etc.
    public double[] getShape(BlockCache blockCache, W world, int x, int y, int z);

    /**
     * Allow faking data.
     * 
     * @return Integer.MAX_VALUE, in case fake data is not supported, and the
     *         Bukkit method is used (as long as possible). 0 may be returned
     *         for performance.
     */
    public int getFakeData(BlockCache blockCache, W world, int x, int y, int z);

}
