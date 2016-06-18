/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.ds.map.CoordHashMap;
import fr.neatmonster.nocheatplus.utilities.ds.map.CoordMap;

// TODO: Auto-generated Javadoc
/**
 * Access to type-ids and data using caching techniques.
 * @author mc_dev
 *
 */
public abstract class BlockCache {

    // TODO: New concepts (Might switch to material, inspect MC+CB code for reliability and performance of block-ids during runtime).

    /** The Constant ID_AIR. */
    private static final int ID_AIR = 0;

    /**
     * Convenience method to check if the bounds as returned by getBounds cover
     * a whole block.
     *
     * @param bounds
     *            Can be null, must have 6 fields.
     * @return true, if is full bounds
     */
    public static final boolean isFullBounds(final double[] bounds) {
        if (bounds == null) return false;
        for (int i = 0; i < 3; i ++) {
            if (bounds[i] > 0.0) return false;
            if (bounds[i + 3] < 1.0) return false;
        }
        return true;
    }

    /**
     * Check if chunks are loaded and load all not yet loaded chunks, using
     * normal world coordinates.<br>
     * NOTE: Not sure where to put this. Method does not use any caching.
     *
     * @param world
     *            the world
     * @param x
     *            the x
     * @param z
     *            the z
     * @param xzMargin
     *            the xz margin
     * @return Number of loaded chunks.
     */
    public static int ensureChunksLoaded(final World world, final double x, final double z, final double xzMargin) {
        int loaded = 0;
        final int minX = Location.locToBlock(x - xzMargin) / 16;
        final int maxX = Location.locToBlock(x + xzMargin) / 16;
        final int minZ = Location.locToBlock(z - xzMargin) / 16;
        final int maxZ = Location.locToBlock(z + xzMargin) / 16;
        for (int cx = minX; cx <= maxX; cx ++) {
            for (int cz = minZ; cz <= maxZ; cz ++) {
                if (!world.isChunkLoaded(cx, cz)) {
                    try {
                        world.loadChunk(cx, cz);
                        loaded ++;
                    } catch (Exception ex) {
                        // (Can't seem to catch more precisely: TileEntity with CB 1.7.10)
                        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().severe(Streams.STATUS, "Failed to load chunk at " + (cx * 16) + "," + (cz * 16) + " (real coordinates):\n" + StringUtil.throwableToString(ex));
                        // (Don't count as loaded.)
                    }
                }
            }
        }
        return loaded;
    }

    /** Cached type-ids. */
    private final CoordMap<Integer> idMap = new CoordHashMap<Integer>(23);

    /** Cached data values. */
    private final CoordMap<Integer> dataMap = new CoordHashMap<Integer>(23);

    /** Cached shape values. */
    private final CoordMap<double[]> boundsMap = new CoordHashMap<double[]>(23);

    /** The max block y. */
    protected int maxBlockY =  255;

    // TODO: Switch to nodes with all details on?

    /**
     * Instantiates a new block cache.
     */
    public BlockCache() {
    }

    /**
     * Instantiates a new block cache.
     *
     * @param world
     *            the world
     */
    public BlockCache(final World world) {
        setAccess(world);
    }

    /**
     * Does not do cleanup.
     *
     * @param world
     *            the new access
     */
    public abstract void setAccess(final World world);

    /**
     * Fetch the type id from the underlying world.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return the int
     */
    public abstract int fetchTypeId(int x, int y, int z);

    /**
     * Fetch the data from the underlying world.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return the int
     */
    public abstract int fetchData(int x, int y, int z);

    /**
     * Find out bounds for the block, this should not return null for
     * performance reasons.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return the double[]
     */
    public abstract double[] fetchBounds(int x, int y, int z);

    /**
     * This is a on-ground type check just for standing on minecarts / boats.
     *
     * @param entity
     *            the entity
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @return true, if successful
     */
    public abstract boolean standsOnEntity(Entity entity, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ);


    /**
     * Remove references.<br>
     * NOTE: You must delete world references with this one.
     */
    public void cleanup() {
        idMap.clear();
        dataMap.clear();
        boundsMap.clear();
    }

    /**
     * (convenience method, uses cache).
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return the type id
     */
    public int getTypeId(double x, double y, double z) {
        return getTypeId(Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z));
    }

    /**
     * (convenience method, uses cache).
     *
     * @param block
     *            the block
     * @return the type id
     */
    public int getTypeId(final Block block) {
        return getTypeId(block.getX(), block.getY(), block.getZ());
    }

    /**
     * Get type id with cache access.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return the type id
     */
    public int getTypeId(final int x, final int y, final int z) {
        final Integer pId = idMap.get(x, y, z);
        if (pId != null) {
            return pId;
        }
        final Integer nId = (y < 0 || y > maxBlockY) ? ID_AIR : fetchTypeId(x, y, z);
        idMap.put(x, y, z, nId);
        return nId;
    }

    /**
     * Get data value with cache access.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return the data
     */
    public int getData(final int x, final int y, final int z) {
        final Integer pData = dataMap.get(x, y, z);
        if (pData != null) {
            return pData;
        }
        final Integer nData = (y < 0 || y > maxBlockY) ? 0 : fetchData(x, y, z);
        dataMap.put(x, y, z, nData);
        return nData;
    }

    /**
     * Get block bounds - <b>Do not change these in-place, because the returned
     * array is cached internally.</b>
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return Array of floats (minX, minY, minZ, maxX, maxY, maxZ), may be null
     *         theoretically. Do not change these in place, because they might
     *         get cached.
     */
    public double[] getBounds(final int x, final int y, final int z) {
        final double[] pBounds = boundsMap.get(x, y, z);
        if (pBounds != null) {
            return pBounds;
        }
        // TODO: Convention for null bounds -> full ?
        final double[] nBounds = (y < 0 || y > maxBlockY) ? null : fetchBounds(x, y, z);
        boundsMap.put(x, y, z, nBounds);
        return nBounds;
    }

    /**
     * Convenience method to check if the bounds for a block cover the full
     * block.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return true, if is full bounds
     */
    public boolean isFullBounds(final int x, final int y, final int z) {
        return isFullBounds(getBounds(x, y, z));
    }

    /**
     * Get the maximal y coordinate a block can be at (non air).
     *
     * @return the max block y
     */
    public int getMaxBlockY() {
        return maxBlockY;
    }

}
