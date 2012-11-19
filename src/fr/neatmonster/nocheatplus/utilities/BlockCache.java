package fr.neatmonster.nocheatplus.utilities;

import net.minecraft.server.Block;
import net.minecraft.server.IBlockAccess;
import net.minecraft.server.Material;
import net.minecraft.server.TileEntity;
import net.minecraft.server.Vec3DPool;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

import fr.neatmonster.nocheatplus.utilities.ds.CoordMap;

/**
 * Access to type-ids and data using caching techniques.
 * @author mc_dev
 *
 */
public class BlockCache implements IBlockAccess{
    
    /**
     * For getting ids.
     */
    private IBlockAccess access = null;
    
    /** Cached type-ids. */
    private final CoordMap<Integer> idMap = new CoordMap<Integer>();
    
    /** Cached data values. */
    private final CoordMap<Integer> dataMap = new CoordMap<Integer>();
    
    // TODO: maybe make very fast access arrays for the ray tracing checks. 
//    private int[] id = null;
//    private int[] data = null;
    
    public BlockCache(){
    }
    
    public BlockCache(final World world){
        setAccess(world);
    }
    
    public BlockCache(final IBlockAccess access){
        setAccess(access);
    }
    
    /**
     * Does not do cleanup.
     * @param world
     */
    public void setAccess(final World world){
        setAccess(((CraftWorld) world).getHandle());
    }
    
    /**
     * Does not do cleanup.
     * @param access
     */
    public void setAccess(final IBlockAccess access){
        this.access = access;
    }
    
    /**
     * Remove references.
     */
    public void cleanup(){
        access = null;
        idMap.clear();
        dataMap.clear();
    }
    
    @Override
	public int getTypeId(final int x, final int y, final int z) {
		final Integer pId = idMap.get(x, y, z);
		if (pId != null) return pId;
		final Integer nId = access.getTypeId(x, y, z);
		idMap.put(x, y, z, nId);
		return nId;
	}

    @Override
	public int getData(final int x, final int y, final int z) {
		final Integer pData = dataMap.get(x, y, z);
		if (pData != null) return pData;
		final Integer nData = access.getData(x, y, z);
		dataMap.put(x, y, z, nData);
		return nData;
	}

    /**
     * Not Optimized.
     */
    @Override
    public Material getMaterial(int arg0, int arg1, int arg2) {
        return access.getMaterial(arg0, arg1, arg2);
    }

    /**
     * Not optimized.
     */
    @Override
    public TileEntity getTileEntity(int arg0, int arg1, int arg2) {
        return access.getTileEntity(arg0, arg1, arg2);
    }

    @Override
    public Vec3DPool getVec3DPool() {
        return access.getVec3DPool();
    }

    @Override
    public boolean isBlockFacePowered(int arg0, int arg1, int arg2, int arg3) {
        return access.isBlockFacePowered(arg0, arg1, arg2, arg3);
    }

	@Override
	public boolean t(int x, int y, int z) {
		// Routes to Block.i(getTypeId(x,y,z)) <- ominous i !
		return access.t(x, y, z);
	}
	
	/**
	 * Compatibility: CB 1.4.2
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	public boolean s(int x, int y, int z) {
		return Block.i(getTypeId(x, y, z));
	}
    
}
