package fr.neatmonster.nocheatplus.utilities;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.IBlockAccess;
import net.minecraft.server.Material;
import net.minecraft.server.TileEntity;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;

/**
 * Access to type-ids using caching techniques.
 * @author mc_dev
 *
 */
public class TypeIdCache implements IBlockAccess{
    /**
     * TODO: Make a map for faster queries (without object creation).
     * TODO: Not sure the prime numbers are too big for normal use.
     * @author mc_dev
     *
     */
    private static class Pos3D{
        private static final int p1 = 73856093;
        private static final int p2 = 19349663;
        private static final int p3 = 83492791;
        // Cube coordinates: 
        public final int x;
        public final int y;
        public final int z;
        public final int hash;
        /**
         * 
         * @param x
         * @param y
         * @param z
         * @param size
         */
        public Pos3D  (final int x, final int y, final int z){
            // Cube related coordinates:
            this.x = x;
            this.y = y;
            this.z = z;
            // Hash
            hash = getHash(this.x, this.y, this.z);
        }
        
        @Override
        public final boolean equals(final Object obj) {
            if (obj instanceof Pos3D){
                final Pos3D other = (Pos3D) obj;
                return other.x == x && other.y == y && other.z == z;
            }
            else return false;
        }

        @Override
        public final int hashCode() {
            return hash;
        }
        
        public static final int getHash(final int x, final int y, final int z) {
            return p1 * x ^ p2 * y ^ p3 * z;
        }
    }
    
    /**
     * For getting ids.
     */
    private IBlockAccess access = null;
    /** Cached type-ids. */
    private final Map<Pos3D, Integer> idMap = new HashMap<Pos3D, Integer>();
    /** Cahced data values. */
    private final Map<Pos3D, Integer> dataMap = new HashMap<Pos3D, Integer>();
    
    // TODO: maybe make very fast access arrays for the ray tracing checks. 
//    private int[] id = null;
//    private int[] data = null;
    
    public TypeIdCache(){
    }
    
    public TypeIdCache(final World world){
        setAccess(world);
    }
    
    public TypeIdCache(final IBlockAccess access){
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
    
    
    public int getTypeId(final int x, final int y, final int z){
        final Pos3D pos = new Pos3D(x, y, z);
        final Integer pId = idMap.get(pos);
        if (pId != null) return pId;
        final Integer nId = access.getTypeId(x, y, z);
        idMap.put(pos, nId);
        return nId;
    }
    
    public int getData(final int x, final int y, final int z){
        final Pos3D pos = new Pos3D(x, y, z);
        final Integer pData = dataMap.get(pos);
        if (pData != null) return pData;
        final Integer nData = access.getData(x, y, z);
        dataMap.put(pos, nData);
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

    /**
     * Not optimized.
     */
    @Override
    public boolean s(int arg0, int arg1, int arg2) {
        return access.s(arg0, arg1, arg2);
    }
    
}
