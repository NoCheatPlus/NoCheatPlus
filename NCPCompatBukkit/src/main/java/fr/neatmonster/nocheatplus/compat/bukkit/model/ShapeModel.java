package fr.neatmonster.nocheatplus.compat.bukkit.model;

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

public interface ShapeModel<W> {

    // TODO: Refine +- might have BukkitBlockCacheNode etc.
    public double[] getShape(BlockCache blockCache, W world, int x, int y, int z);

}
