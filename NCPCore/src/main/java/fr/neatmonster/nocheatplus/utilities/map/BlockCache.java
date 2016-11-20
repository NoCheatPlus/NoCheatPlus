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
package fr.neatmonster.nocheatplus.utilities.map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.utilities.ds.map.CoordHashMap;
import fr.neatmonster.nocheatplus.utilities.ds.map.CoordMap;

/**
 * Access to block properties using caching technique (id, data, bounds).
 * 
 * @author asofold
 *
 */
public abstract class BlockCache {

    // TODO: New concepts (Might switch to material, inspect MC+CB code for reliability and performance of block-ids during runtime).

    /** The Constant ID_AIR. */
    private static final int ID_AIR = 0;

    public static class BlockCacheNode {

        public static final short FETCHED_ID = 0x01;
        public static final short FETCHED_DATA = 0x02;
        public static final short FETCHED_BOUNDS = 0x04;

        private short fetched = 0;
        private int id = 0;
        private int data = 0;
        private double[] bounds = null;

        public boolean isIdFetched() {
            return (fetched & FETCHED_ID) != 0;
        }

        public boolean isDataFetched() {
            return (fetched & FETCHED_DATA) != 0;
        }

        public boolean isBoundsFetched() {
            return (fetched & FETCHED_BOUNDS) != 0;
        }

        public int getId() {
            return id;
        }

        public int getData() {
            return data;
        }

        public double[] getBounds() {
            return bounds;
        }

        public void setId(int id) {
            this.id = id;
            fetched |= FETCHED_ID;
        }

        public void setData(int data) {
            this.data = data;
            fetched |= FETCHED_DATA;
        }

        public void setBounds(double[] bounds) {
            this.bounds = bounds;
            fetched |= FETCHED_BOUNDS;
        }

        public void set(int id, int data, double[] bounds) {
            setId(id);
            setData(data);
            setBounds(bounds);
        }

        void reset() {
            fetched = 0;
            id = 0;
            data  = 0;
            bounds = null;
        }

    }

    // Instance

    /** Nodes for cached block properties. */
    private final CoordMap<BlockCacheNode> nodeMap = new CoordHashMap<BlockCacheNode>(23);

    /** The max block y. */
    protected int maxBlockY =  255;

    private final BlockCacheNode airNode = new BlockCacheNode();
    // TODO: setBlockCacheConfig -> set static nodes (rather only by id).

    /**
     * Instantiates a new block cache.
     */
    public BlockCache() {
        airNode.set(0, 0, null);
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
     * @return This BlockCache instance for chaining.
     */
    public abstract BlockCache setAccess(final World world);

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
        nodeMap.clear();
    }

    /**
     * If there is no node stored, create a new node only with the type id set.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    private BlockCacheNode getOrCreateNode(final int x, final int y, final int z) {
        BlockCacheNode node = nodeMap.get(x, y, z);
        if (node != null) {
            return node;
        }
        final int id = (y < 0 || y > maxBlockY) ? ID_AIR : fetchTypeId(x, y, z);
        // (Later: Static id-node map from config.)
        if (id == ID_AIR) {
            return airNode;
        }
        else {
            node = new BlockCacheNode();
            node.id = id; // The id is always needed.
            nodeMap.put(x, y, z, node);
            return node;
        }
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
        return getOrCreateNode(x, y, z).getId();
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
        final BlockCacheNode node = getOrCreateNode(x, y, z);
        if (node.isDataFetched()) {
            return node.getData();
        }
        final int nData = fetchData(x, y, z);
        node.setData(nData);
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
        final BlockCacheNode node = getOrCreateNode(x, y, z);
        if (node.isBoundsFetched()) {
            return node.getBounds();
        }
        final double[] nBounds = fetchBounds(x, y, z);
        // TODO: Convention for null bounds -> full ?
        node.setBounds(nBounds);
        return nBounds;
    }

    /**
     * Get the internally stored BlockCacheNode instance for the given
     * coordinates. Creation/updating is controlled with forceSetAll.
     * 
     * @param x
     * @param y
     * @param z
     * @param forceSetAll
     * @return If forceSetAll is true, a node will always be returned with all
     *         properties set. If forceSetAll is false, null might be returned,
     *         if no node is present for the given coordinates.
     */
    public BlockCacheNode getBlockCacheNode(int x, int y, int z, boolean forceSetAll) {
        if (forceSetAll) {
            final BlockCacheNode node = getOrCreateNode(x, y, z);
            if (!node.isDataFetched()) {
                node.setData(fetchData(x, y, z));
            }
            if (!node.isBoundsFetched()) {
                node.setBounds(fetchBounds(x, y, z));
            }
            return node;
        }
        else {
            return nodeMap.get(x, y, z);
        }
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
        return MapUtil.isFullBounds(getBounds(x, y, z));
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
