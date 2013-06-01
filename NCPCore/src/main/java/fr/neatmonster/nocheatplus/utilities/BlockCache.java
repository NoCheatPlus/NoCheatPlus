package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.utilities.ds.CoordMap;

/**
 * Access to type-ids and data using caching techniques.
 * @author mc_dev
 *
 */
public abstract class BlockCache {
	
	/**
	 * Convenience method to check if the bounds as returned by getBounds cover a whole block.
	 * @param bounds Can be null, must have 6 fields.
	 * @return
	 */
	public static final boolean isFullBounds(final double[] bounds) {
		if (bounds == null) return false;
		for (int i = 0; i < 3; i ++){
			if (bounds[i] > 0.0) return false;
			if (bounds[i + 3] < 1.0) return false;
		}
		return true;
	}
	
	/**
	 * Check if chunks are loaded and load all not yet loaded chunks, using normal world coordinates.<br>
	 * NOTE: Not sure where to put this. Method does not use any caching.
	 * @param world
	 * @param x
	 * @param z
	 * @param xzMargin
	 * @return Number of loaded chunks.
	 */
	public static int ensureChunksLoaded(final World world, final double x, final double z, final double xzMargin) {
		int loaded = 0;
		final int minX = Location.locToBlock(x - xzMargin) / 16;
		final int maxX = Location.locToBlock(x + xzMargin) / 16;
		final int minZ = Location.locToBlock(z - xzMargin) / 16;
		final int maxZ = Location.locToBlock(z + xzMargin) / 16;
		for (int cx = minX; cx <= maxX; cx ++){
			for (int cz = minZ; cz <= maxZ; cz ++){
				if (!world.isChunkLoaded(cx, cz)){
					world.loadChunk(cx, cz);
					loaded ++;
				}
			}
		}
		return loaded;
	}
    
    /** Cached type-ids. */
    private final CoordMap<Integer> idMap = new CoordMap<Integer>(23);
    
    /** Cached data values. */
    private final CoordMap<Integer> dataMap = new CoordMap<Integer>(23);
    
    /** Cached shape values. */
    private final CoordMap<double[]> boundsMap = new CoordMap<double[]>(23);
    
    private int maxBlockY =  255;
    
    // TODO: switch to nodes with all details on, store a working node ?
    
    // TODO: maybe make very fast access arrays for the ray tracing checks. 
//    private int[] id = null;
//    private int[] data = null;
    
    public BlockCache(){
    }
    
    public BlockCache(final World world){
        setAccess(world);
    }
    
    /**
     * Does not do cleanup.
     * @param world
     */
    public abstract void setAccess(final World world);
    
    /**
     * Fetch the type id from the underlying world.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public abstract int fetchTypeId(int x, int y, int z);
	
    /**
     * Fetch the data from the underlying world.
     * @param x
     * @param y
     * @param z
     * @return
     */
	public abstract int fetchData(int x, int y, int z);
	
	public abstract double[] fetchBounds(int x, int y, int z);
	
	/**
	 * This is a on-ground type check just for standing on minecarts / boats.
	 * @param entity
	 * @param minX
	 * @param minY
	 * @param minZ
	 * @param maxX
	 * @param maxY
	 * @param maxZ
	 * @return
	 */
	public abstract boolean standsOnEntity(Entity entity, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ);

    
    /**
     * Remove references.<br>
     * NOTE: You must delete world references with this one.
     */
    public void cleanup(){
        idMap.clear();
        dataMap.clear();
        boundsMap.clear();
    }
    
    /**
     * (convenience method, uses cache).
     * @param eX
     * @param eY
     * @param eZ
     * @return
     */
	public int getTypeId(double x, double y, double z) {
		return getTypeId(Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z));
	}
	
	/**
	 * (convenience method, uses cache).
	 * @param block
	 * @return
	 */
	public int getTypeId(final Block block) {
		return getTypeId(block.getX(), block.getY(), block.getZ());
	}
    
    /**
     * Get type id with cache access.
     * @param x
     * @param y
     * @param z
     * @return
     */
	public int getTypeId(final int x, final int y, final int z) {
		final Integer pId = idMap.get(x, y, z);
		if (pId != null) return pId;
		final Integer nId = fetchTypeId(x, y, z);
		idMap.put(x, y, z, nId);
		return nId;
	}

	/**
	 * Get data value with cache access.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public int getData(final int x, final int y, final int z) {
		final Integer pData = dataMap.get(x, y, z);
		if (pData != null) return pData;
		final Integer nData = fetchData(x, y, z);
		dataMap.put(x, y, z, nData);
		return nData;
	}
	
	/**
	 * Get block bounds - <b>Do not change these in-place, because the returned array is cached internally.</b>
	 * @param x
	 * @param y
	 * @param z
	 * @return Array of floats (minX, minY, minZ, maxX, maxY, maxZ), may be null theoretically. Do not change these in place, because they might get cached.
	 */
	public double[] getBounds(final int x, final int y, final int z){
		final double[] pBounds = boundsMap.get(x, y, z);
		if (pBounds != null) return pBounds;
		final double[] nBounds = fetchBounds(x, y, z);
		boundsMap.put(x, y, z, nBounds);
		return nBounds;
	}
	
	/**
	 * Convenience method to check if the bounds for a block cover the full block.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public boolean isFullBounds(final int x, final int y, final int z){
		return isFullBounds(getBounds(x, y, z));
	}
	
	/**
	 * Get the maximal y coordinate a block can be at (non air).
	 * @return
	 */
	public int getMaxBlockY(){
		return maxBlockY;
	}
    
}
