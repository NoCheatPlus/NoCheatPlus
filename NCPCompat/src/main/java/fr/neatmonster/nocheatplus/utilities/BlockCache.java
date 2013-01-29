package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.utilities.ds.CoordMap;

/**
 * Access to type-ids and data using caching techniques.
 * @author mc_dev
 *
 */
public abstract class BlockCache {
    
    /** Cached type-ids. */
    private final CoordMap<Integer> idMap = new CoordMap<Integer>();
    
    /** Cached data values. */
    private final CoordMap<Integer> dataMap = new CoordMap<Integer>();
    
    /** Cached shape values. */
    private final CoordMap<double[]> boundsMap = new CoordMap<double[]>();
    
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
	 * Get block bounds
	 * @param x
	 * @param y
	 * @param z
	 * @return Array of floats (minX, minY, minZ, maxX, maxY, maxZ), may be null theoretically.
	 */
	public double[] getBounds(final int x, final int y, final int z){
		final double[] pBounds = boundsMap.get(x, y, z);
		if (pBounds != null) return pBounds;
		final double[] nBounds = fetchBounds(x, y, z);
		boundsMap.put(x, y, z, nBounds);
		return nBounds;
	}
	
	/**
	 * Get the maximal y coordinate a block can be at (non air).
	 * @return
	 */
	public int getMaxBlockY(){
		return maxBlockY;
	}
    
}
