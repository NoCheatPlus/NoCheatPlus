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
package fr.neatmonster.nocheatplus.utilities.location;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeReference;
import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeTracker;
import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeTracker.BlockChangeEntry;
import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeTracker.Direction;
import fr.neatmonster.nocheatplus.components.location.IGetBox3D;
import fr.neatmonster.nocheatplus.components.location.IGetBlockPosition;
import fr.neatmonster.nocheatplus.components.location.IGetBukkitLocation;
import fr.neatmonster.nocheatplus.components.location.IGetPosition;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.collision.CollisionUtil;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache.IBlockCacheNode;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.map.MapUtil;

// TODO: Auto-generated Javadoc
/**
 * A location with bounds with a lot of extra stuff.
 * 
 * @author asofold
 *
 */
public class RichBoundsLocation implements IGetBukkitLocation, IGetBlockPosition, IGetBox3D {

    // TODO: Consider switching back from default to private visibility (use getters for other places).

    // Simple members // 

    /** Y parameter for growing the bounding box with the isOnGround check. */
    double yOnGround = 0.001;

    /** The block coordinates. */
    int blockX, blockY, blockZ;

    /** The exact coordinates. */
    double x, y, z;

    /** The pitch. */
    float yaw, pitch; // TODO: -> entity ?

    /** Bounding box. */
    double minX, maxX, minY, maxY, minZ, maxZ;

    /** Horizontal margin for the bounding box (center towards edge). */
    double boxMarginHorizontal;

    /** Vertical margin for the bounding box (y towards top). */
    double boxMarginVertical;

    // TODO: Check if onGround can be completely replaced by onGroundMinY and notOnGroundMaxY.
    /** Minimal yOnGround for which the player is on ground. No extra xz/y margin.*/
    double onGroundMinY = Double.MAX_VALUE;
    /** Maximal yOnGround for which the player is not on ground. No extra xz/y margin.*/
    double notOnGroundMaxY = Double.MIN_VALUE;


    // "Light" object members (reset to null on set) //

    // TODO: primitive+isSet? AlmostBoolean?
    // TODO: All properties that can be set should have a "checked" flag, thus resetting the flag suffices.

    // TODO: nodeAbove ?

    /** Type node for the block at the position. */
    IBlockCacheNode node = null;

    /** Type node of the block below. */
    IBlockCacheNode nodeBelow = null;

    /** All block flags collected for maximum used bounds. */
    Long blockFlags = null;

    /** Is the player on ladder?. */
    Boolean onClimbable = null;

    /** Simple test if the exact position is passable. */
    Boolean passable = null;

    /** Bounding box collides with blocks. */
    Boolean passableBox = null;

    /** Is the player above stairs?. */
    Boolean aboveStairs = null;

    /** Is the player in lava?. */
    Boolean inLava = null;

    /** Is the player in water?. */
    Boolean inWater = null;

    /** Is the player is web?. */
    Boolean inWeb = null;

    /** Is the player on ice?. */
    Boolean onIce = null;

    /** Is the player on the ground?. */
    Boolean onGround = null;
    
    Boolean onSoulSand = null;


    // "Heavy" object members that need to be set to null on cleanup. //

    /** Block property access. */
    BlockCache blockCache = null;

    /** Bukkit world. */
    World world = null;


    /**
     * Instantiates a new rich bounds location.
     *
     * @param blockCache
     *            BlockCache instance, may be null.
     */
    public RichBoundsLocation(final BlockCache blockCache) {
        this.blockCache = blockCache;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.location.IGetBukkitLocation#getWorld()
     */
    @Override
    public World getWorld() {
        return world;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.location.IGetLocation#getWorldName()
     */
    @Override
    public String getWorldName() {
        return world.getName();
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.location.IGetPosition#getX()
     */
    @Override
    public double getX() {
        return x;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.location.IGetPosition#getY()
     */
    @Override
    public double getY() {
        return y;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.location.IGetPosition#getZ()
     */
    @Override
    public double getZ() {
        return z;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.location.IGetLook#getYaw()
     */
    @Override
    public float getYaw() {
        return yaw;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.location.IGetLook#getPitch()
     */
    @Override
    public float getPitch() {
        return pitch;
    }

    /**
     * Gets the vector.
     *
     * @return the vector
     */
    public Vector getVector() {
        return new Vector(x, y, z);
    }

    /**
     * Gets a new Location instance representing this position.
     *
     * @return the location
     */
    public Location getLocation() {
        if (this.world == null) {
            throw new NullPointerException("World is null.");
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.location.IGetBlockPosition#getBlockX()
     */
    @Override
    public int getBlockX() {
        return blockX;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.location.IGetBlockPosition#getBlockY()
     */
    @Override
    public int getBlockY() {
        return blockY;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.location.IGetBlockPosition#getBlockZ()
     */
    @Override
    public int getBlockZ() {
        return blockZ;
    }

    /**
     * Return the bounding box as a new double array (minX, minY, minZ, maxX,
     * maxY, maxZ).
     *
     * @return the bounds as doubles
     */
    public double[] getBoundsAsDoubles() {
        return new double[] {minX, minY, minZ, maxX, maxY, maxZ};
    }

    @Override
    public double getMinX() {
        return minX;
    }

    @Override
    public double getMinZ() {
        return minZ;
    }

    @Override
    public double getMaxX() {
        return maxX;
    }

    @Override
    public double getMaxZ() {
        return maxZ;
    }

    @Override
    public double getMinY() {
        return minY;
    }

    @Override
    public double getMaxY() {
        return maxY;
    }

    /**
     * Get the bounding box margin from the center (x ,z) to the edge of the
     * box. This value may be adapted from entity width or other input, and it
     * might be cut down to a certain resolution (e.g. 1/1000).
     *
     * @return the box margin horizontal
     */
    public double getBoxMarginHorizontal() {
        return boxMarginHorizontal;
    }

    /**
     * Get the bounding box margin from the y coordinate (feet for entities) to
     * the top.
     *
     * @return the box margin vertical
     */
    public double getBoxMarginVertical() {
        return boxMarginVertical;
    }

    /**
     * Compares block coordinates (not the world).
     *
     * @param other
     *            the other
     * @return true, if is same block
     */
    public final boolean isSameBlock(final IGetBlockPosition other) {
        return blockX == other.getBlockX() && blockZ == other.getBlockZ() && blockY == other.getBlockY();
    }

    /**
     * Block coordinate comparison.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return true, if is same block
     */
    public final boolean isSameBlock(final int x, final int y, final int z) {
        return blockX == x && blockZ == z && blockY == y;
    }

    /**
     * Compares block coordinates (not the world).
     *
     * @param loc
     *            the loc
     * @return true, if is same block
     */
    public final boolean isSameBlock(final Location loc) {
        return blockX == loc.getBlockX() && blockZ == loc.getBlockZ() && blockY == loc.getBlockY();
    }

    /**
     * Check if this location is above the given one (blockY + 1).
     *
     * @param loc
     *            the loc
     * @return true, if is block above
     */
    public boolean isBlockAbove(final IGetBlockPosition loc) {
        return blockY == loc.getBlockY() + 1 && blockX == loc.getBlockX() && blockZ == loc.getBlockZ();
    }

    /**
     * Check if this location is above the given one (blockY + 1).
     *
     * @param loc
     *            the loc
     * @return true, if is block above
     */
    public boolean isBlockAbove(final Location loc) {
        return blockY == loc.getBlockY() + 1 && blockX == loc.getBlockX() && blockZ == loc.getBlockZ();
    }

    /**
     * Compares exact coordinates (not the world).
     *
     * @param loc
     *            the loc
     * @return true, if is same pos
     */
    public boolean isSamePos(final IGetPosition loc) {
        return x == loc.getX() && z == loc.getZ() && y == loc.getY();
    }

    /**
     * Compares exact coordinates (not the world).
     *
     * @param loc
     *            the loc
     * @return true, if is same pos
     */
    public boolean isSamePos(final Location loc) {
        return x == loc.getX() && z == loc.getZ() && y == loc.getY();
    }

    /**
     * Manhattan distance, see Trigutil.
     *
     * @param other
     *            the other
     * @return the int
     */
    public int manhattan(final IGetBlockPosition other) {
        // TODO: Consider using direct field access from other methods as well.
        return TrigUtil.manhattan(this.blockX, this.blockY, this.blockZ, other.getBlockX(), other.getBlockY(), other.getBlockZ());
    }

    /**
     * Maximum block distance comparing dx, dy, dz.
     *
     * @param other
     *            the other
     * @return the int
     */
    public int maxBlockDist(final IGetBlockPosition other) {
        // TODO: Consider using direct field access from other methods as well.
        return TrigUtil.maxDistance(this.blockX, this.blockY, this.blockZ, other.getBlockX(), other.getBlockY(), other.getBlockZ());
    }

    /**
     * Quick check for really bad coordinates (actual problem, if true is
     * returned.).
     *
     * @return true, if successful
     */
    public boolean hasIllegalCoords() {
        return LocUtil.isBadCoordinate(minX, maxX, minY, maxY, minZ, maxZ);
    }

    /**
     * Get the collected block-flags. This will return null if collectBlockFlags
     * has not been called.
     *
     * @return the block flags
     */
    public Long getBlockFlags() {
        return blockFlags;
    }

    /**
     * Set the block flags which are usually collected on base of bounding box,
     * yOnGround and other considerations, such as 1.5 high blocks.
     *
     * @param blockFlags
     *            the new block flags
     */
    public void setBlockFlags(Long blockFlags) {
        this.blockFlags = blockFlags;
    }

    /**
     * Not cached.
     *
     * @return the type id above
     */
    public Material getTypeIdAbove() {
        return blockCache.getType(blockX, blockY + 1,  blockZ);
    }

    /**
     * Convenience method: delegate to BlockProperties.isDoppwnStream .
     *
     * @param xDistance
     *            the x distance
     * @param zDistance
     *            the z distance
     * @return true, if is down stream
     */
    public boolean isDownStream(final double xDistance, final double zDistance)
    {
        return BlockProperties.isDownStream(blockCache, blockX, blockY, blockZ, getData(), xDistance, zDistance);
    }

    /**
     * Get existing or create.
     * @return
     */
    public IBlockCacheNode getOrCreateBlockCacheNode() {
        if (node == null) {
            node = blockCache.getOrCreateBlockCacheNode(blockX, blockY, blockZ, false);
        }
        return node;
    }

    /**
     * Get existing or create.
     * @return
     */
    public IBlockCacheNode getOrCreateBlockCacheNodeBelow() {
        if (nodeBelow == null) {
            nodeBelow = blockCache.getOrCreateBlockCacheNode(blockX, blockY - 1, blockZ, false);
        }
        return nodeBelow;
    }
    
    /**
     * Get existing or create.
     * @return
     */
    public IBlockCacheNode getOrCreateBlockCacheNodeBelowLiq() {
        if (nodeBelow == null) {
            nodeBelow = blockCache.getOrCreateBlockCacheNode(blockX, blockY - 1.2, blockZ, false);
        }
        return nodeBelow;
    }

    /**
     * Gets the type id.
     *
     * @return the type id
     */
    public Material getTypeId() {
        if (node == null) {
            getOrCreateBlockCacheNode();
        }
        return node.getType();
    }

    /**
     * Gets the type id below.
     *
     * @return the type id below
     */
    public Material getTypeIdBelow() {
        if (nodeBelow == null) {
            getOrCreateBlockCacheNodeBelow();
        }
        return nodeBelow.getType();
    }
    
    /**
     * Gets the type id slightly lower.
     *
     * @return the type id below
     */
    public Material getTypeIdBelowLiq() {
        if (nodeBelow == null) {
            getOrCreateBlockCacheNodeBelowLiq();
        }
        return nodeBelow.getType();
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public Integer getData() {
        if (node == null) {
            node = blockCache.getOrCreateBlockCacheNode(blockX, blockY, blockZ, false);
            return node.getData(blockCache, blockX, blockY, blockZ);
        }
        return node.getData();
    }

    /**
     * Uses id cache if present.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return the type id
     */
    public final Material getTypeId(final int x, final int y, final int z) {
        return blockCache.getType(x, y, z);
    }

    /**
     * Uses id cache if present.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return the data
     */
    public final int getData(final int x, final int y, final int z) {
        return blockCache.getData(x, y, z);
    }

    /**
     * Set the id cache for faster id getting.
     *
     * @param cache
     *            the new block cache
     */
    public void setBlockCache(final BlockCache cache) {
        this.blockCache = cache;
    }

    /**
     * Get the underlying BLockCache.
     *
     * @return the block cache
     */
    public final BlockCache getBlockCache() {
        return blockCache;
    }


    /**
     * Checks if the player is above stairs.
     * 
     * @return true, if the player above on stairs
     */
    public boolean isAboveStairs() {
        if (aboveStairs == null) {
            if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_STAIRS) == 0 ) {
                aboveStairs = false;
                return false;
            }
            // TODO: Distinguish based on actual height off .0 ?
            // TODO: diff still needed ?
            final double diff = 0; // 0.001;
            aboveStairs = BlockProperties.collides(blockCache, minX - diff, minY - 1.0, minZ - diff, maxX + diff, minY + 0.25, maxZ + diff, BlockProperties.F_STAIRS);
        }
        return aboveStairs;
    }

    /**
     * Checks if the player is in lava.
     * 
     * @return true, if the player is in lava
     */
    public boolean isInLava() {
        if (inLava == null) {
            if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_LAVA) == 0 ) {
                inLava = false;
                return false;
            }
            // TODO: ...
            //          final double dX = -0.10000000149011612D;
            //          final double dY = -0.40000000596046448D;
            //          final double dZ = dX;
            //          inLava = BlockProperties.collides(blockCache, minX - dX, minY - dY, minZ - dZ, maxX + dX, maxY + dY, maxZ + dZ, BlockProperties.F_LAVA);
            inLava = BlockProperties.collides(blockCache, minX, minY, minZ, maxX, maxY, maxZ, BlockProperties.F_LAVA);
        }
        return inLava;
    }

    /**
     * Checks if the player is in water.
     * 
     * @return true, if the player is in water
     */
    public boolean isInWater() {
        if (inWater == null) {
            if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_WATER) == 0 ) {
                inWater = false;
                return false;
            }
            // TODO: ...
            //          final double dX = -0.001D;
            //          final double dY = -0.40000000596046448D - 0.001D;
            //          final double dZ = dX;
            //          inWater = BlockProperties.collides(blockCache, minX - dX, minY - dY, minZ - dZ, maxX + dX, maxY + dY, maxZ + dZ, BlockProperties.F_WATER);
            inWater = BlockProperties.collides(blockCache, minX, minY, minZ, maxX, maxY, maxZ, BlockProperties.F_WATER);

        }
        return inWater;
    }

    /**
     * Checks if the player is in a liquid.
     * 
     * @return true, if the player is in a liquid
     */
    public boolean isInLiquid() {
        // TODO: optimize (check liquid first and only if liquid check further)
        if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_LIQUID) == 0 ) return false;
        // TODO: This should check for F_LIQUID too, Use a method that returns all found flags (!).
        return isInWater() || isInLava();
    }

    /**
     * Checks if the player is on a ladder or vine. Contains special casing for
     * trap doors above climbable.
     * 
     * @return If so.
     */
    public boolean isOnClimbable() {
        if (onClimbable == null) {
            // Early return with flags set and no climbable nearby.
            final Material typeId = getTypeId();
            if (blockFlags != null && (blockFlags & BlockProperties.F_CLIMBABLE) == 0
                    // Special case trap doors: // Better than increasing maxYOnGround.
                    && (blockFlags & BlockProperties.F_PASSABLE_X4) == 0
                    ) {
                onClimbable = false;
                return false;
            }
            final long thisFlags = BlockProperties.getBlockFlags(typeId);
            onClimbable = (thisFlags & BlockProperties.F_CLIMBABLE) != 0;
            // TODO: maybe use specialized bounding box.
            //          final double d = 0.1d;
            //          onClimbable = BlockProperties.collides(getBlockAccess(), minX - d, minY - d, minZ - d, maxX + d, minY + 1.0, maxZ + d, BlockProperties.F_CLIMBABLE);
            if (!onClimbable) {
                // Special case trap door (simplified preconditions check).
                // TODO: Distance to the wall?
                if ((thisFlags & BlockProperties.F_PASSABLE_X4) != 0
                        && BlockProperties.isTrapDoorAboveLadderSpecialCase(blockCache, blockX, blockY, blockZ)) {
                    onClimbable = true;
                }
            }
        }
        return onClimbable;
    }

    /**
     * Check if solid blocks hit the box.
     *
     * @param xzMargin
     *            the xz margin
     * @param yMargin
     *            the y margin
     * @return true, if is next to ground
     */
    public boolean isNextToGround(final double xzMargin, final double yMargin) {
        // TODO: Adjust to check block flags ?
        return BlockProperties.collides(blockCache, minX - xzMargin, minY - yMargin, minZ - xzMargin, maxX + xzMargin, maxY + yMargin, maxZ + xzMargin, BlockProperties.F_GROUND);
    }

    /**
     * Reset condition for flying checks (sf + nofall): liquids, web, ladder
     * (not on-ground, though).
     *
     * @return true, if is reset cond
     */
    public boolean isResetCond() {
        // NOTE: if optimizing, setYOnGround has to be kept in mind. 
        return isInLiquid()  || isOnClimbable() || isInWeb();
    }

    /**
     * Check if the location is on ground and if it is hitting the bounding box
     * of a block with the given id. Currently this is coarse (not checking if
     * it is really possible to stand on such a block).
     *
     * @param id
     *            the id
     * @return true, if successful
     */
    public boolean standsOnBlock(final Material id) {
        if (!isOnGround()) {
            return false;
        }
        return BlockProperties.collidesBlock(this.blockCache, minX, minY - yOnGround, minZ, maxX, minY, maxZ, id);
    }

    /**
     * Checks if the player is above a ladder or vine.<br>
     * Does not save back value to field.
     * 
     * @return If so.
     */
    public boolean isAboveLadder() {
        if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_CLIMBABLE) == 0 ) return false;
        // TODO: bounding box ?
        return (BlockProperties.getBlockFlags(getTypeIdBelow()) & BlockProperties.F_CLIMBABLE) != 0;
    }

    /**
     * Checks if the player is in web.
     * 
     * @return true, if the player is in web
     */
    public boolean isInWeb() {
        if (inWeb == null) {
            if (blockFlags == null || (blockFlags & BlockProperties.F_COBWEB) != 0L ) {
                // TODO: inset still needed / configurable?
                final double inset = 0.001d;
                inWeb = BlockProperties.collides(blockCache, minX + inset, minY + inset, minZ + inset, 
                        maxX - inset, maxY - inset, maxZ - inset, 
                        BlockProperties.F_COBWEB);
            }
            else {
                inWeb = false;
            }
        }
        return inWeb;
    }

    /**
     * Check the location is on ice, only regarding the center. Currently
     * demands to be on ground as well.
     *
     * @return true, if is on ice
     */
    public boolean isOnIce() {
        if (onIce == null) {
            // TODO: Use a box here too ?
            // TODO: check if player is really sneaking (refactor from survivalfly to static access in Combined ?)!
            if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_ICE) == 0) {
                // TODO: check onGroundMinY !?
                onIce = false;
            } else {
                // TODO: Might skip the isOnGround part, e.g. if boats sink in slightly. Needs testing.
                onIce = isOnGround() && BlockProperties.collides(blockCache, minX, minY - yOnGround, minZ, maxX, minY, maxZ, BlockProperties.F_ICE);
            }
        }
        return onIce;
    }
    
    public boolean isOnSoulSand() {
        if (onSoulSand == null) {
            // TODO: Use a box here too ?
            // TODO: check if player is really sneaking (refactor from survivalfly to static access in Combined ?)!
            if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_SOULSAND) == 0) {
                // TODO: check onGroundMinY !?
                onSoulSand = false;
            } else {
                // TODO: Might skip the isOnGround part, e.g. if boats sink in slightly. Needs testing.
                onSoulSand = isOnGround() && BlockProperties.collides(blockCache, minX, minY - yOnGround, minZ, maxX, minY, maxZ, BlockProperties.F_SOULSAND);
            }
        }
        return onSoulSand;
    }

    /**
     * Test if the location is on rails (assuming minecarts with some magic
     * bounds/behavior).
     *
     * @return true, if is on rails
     */
    public boolean isOnRails() {
        return BlockProperties.isRails(getTypeId())
                // TODO: Checking the block below might be over-doing it.
                || y - blockY < 0.3625 && BlockProperties.isAscendingRails(getTypeIdBelow(), getData(blockX, blockY - 1, blockZ));
    }

    /**
     * Checks if the thing is on ground, including entities such as Minecart, Boat.
     * 
     * @return true, if the player is on ground
     */
    public boolean isOnGround() {
        if (onGround != null) {
            return onGround;
        }
        // Check cached values and simplifications.
        if (notOnGroundMaxY >= yOnGround) {
            onGround = false;
        }
        else if (onGroundMinY <= yOnGround) {
            onGround = true;
        }
        else {
            // Shortcut check (currently needed for being stuck + sf).
            if (blockFlags == null || (blockFlags.longValue() & BlockProperties.F_GROUND) != 0) {
                // TODO: Consider dropping this shortcut.
                final int bY = Location.locToBlock(y - yOnGround);
                final IBlockCacheNode useNode = bY == blockY ? getOrCreateBlockCacheNode() : (bY == blockY -1 ? getOrCreateBlockCacheNodeBelow() : blockCache.getOrCreateBlockCacheNode(blockX,  bY, blockZ, false));
                final Material id = useNode.getType();
                final long flags = BlockProperties.getBlockFlags(id);
                // TODO: Might remove check for variable ?
                if ((flags & BlockProperties.F_GROUND) != 0 && (flags & BlockProperties.F_VARIABLE) == 0) {
                    final double[] bounds = useNode.getBounds(blockCache, blockX, bY, blockZ);
                    // Check collision if not inside of the block. [Might be a problem for cauldron or similar + something solid above.]
                    // TODO: Might need more refinement.
                    if (bounds != null && y - bY >= bounds[4] && BlockProperties.collidesBlock(blockCache, x, minY - yOnGround, z, x, minY, z, blockX, bY, blockZ, useNode, null, flags)) {
                        // TODO: BlockHeight is needed for fences, use right away (above)?
                        if (!BlockProperties.isPassableWorkaround(blockCache, blockX, bY, blockZ, minX - blockX, minY - yOnGround - bY, minZ - blockZ, useNode, maxX - minX, yOnGround, maxZ - minZ,  1.0)
                                || (flags & BlockProperties.F_GROUND_HEIGHT) != 0 &&  BlockProperties.getGroundMinHeight(blockCache, blockX, bY, blockZ, useNode, flags) <= y - bY) {
                            //                          NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, "*** onground SHORTCUT");
                            onGround = true;
                        }
                    }
                }
                if (onGround == null) {
                    //                  NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, "*** fetch onground std");
                    // Full on-ground check (blocks).
                    // Note: Might check for half-block height too (getTypeId), but that is much more seldom.
                    onGround = BlockProperties.isOnGround(blockCache, minX, minY - yOnGround, minZ, maxX, minY, maxZ, 0L);
                }
            }
            else {
                onGround = false;
            }
        }
        if (onGround) {
            onGroundMinY = Math.min(onGroundMinY, yOnGround);
        }
        else {
            notOnGroundMaxY = Math.max(notOnGroundMaxY, yOnGround);
        }
        return onGround;
    }

    /**
     * Simple block-on-ground check for given margin (no entities). Meant for
     * checking bigger margin than the normal yOnGround.
     *
     * @param yOnGround
     *            Margin below the player.
     * @return true, if is on ground
     */
    public boolean isOnGround(final double yOnGround) {
        if (notOnGroundMaxY >= yOnGround) return false;
        else if (onGroundMinY <= yOnGround) return true;
        return  isOnGround(yOnGround, 0D, 0D, 0L);
    }

    /**
     * SSimple block-on-ground check for given margin (no entities). Meant for
     * checking bigger margin than the normal yOnGround.
     *
     * @param yOnGround
     *            the y on ground
     * @param ignoreFlags
     *            Flags to not regard as ground.
     * @return true, if is on ground
     */
    public boolean isOnGround(final double yOnGround, final long ignoreFlags) {
        if (ignoreFlags == 0) {
            if (notOnGroundMaxY >= yOnGround) return false;
            else if (onGroundMinY <= yOnGround) return true;
        }
        return isOnGround(yOnGround, 0D, 0D, ignoreFlags);
    }


    /**
     * Simple block-on-ground check for given margin (no entities). Meant for
     * checking bigger margin than the normal yOnGround.
     *
     * @param yOnGround
     *            Margin below the player.
     * @param xzMargin
     *            the xz margin
     * @param yMargin
     *            Extra margin added below and above.
     * @return true, if is on ground
     */
    public boolean isOnGround(final double yOnGround, final double xzMargin, final double yMargin) {
        if (xzMargin >= 0 && onGroundMinY <= yOnGround) return true;
        if (xzMargin <= 0 && yMargin == 0) {
            if (notOnGroundMaxY >= yOnGround) return false;
        }
        return isOnGround(yOnGround, xzMargin, yMargin, 0);
    }

    /**
     * Simple block-on-ground check for given margin (no entities). Meant for
     * checking bigger margin than the normal yOnGround.
     *
     * @param yOnGround
     *            Margin below the player.
     * @param xzMargin
     *            the xz margin
     * @param yMargin
     *            Extra margin added below and above.
     * @param ignoreFlags
     *            Flags to not regard as ground.
     * @return true, if is on ground
     */
    public boolean isOnGround(final double yOnGround, final double xzMargin, final double yMargin, final long ignoreFlags) {
        if (ignoreFlags == 0) {
            if (xzMargin >= 0 && onGroundMinY <= yOnGround) return true;
            if (xzMargin <= 0 && yMargin == 0) {
                if (notOnGroundMaxY >= yOnGround) return false;
            }
        }
        //      NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, "*** Fetch on-ground: yOnGround=" + yOnGround + " xzM=" + xzMargin + " yM=" + yMargin + " ign=" + ignoreFlags);
        final boolean onGround = BlockProperties.isOnGround(blockCache, minX - xzMargin, minY - yOnGround - yMargin, minZ - xzMargin, maxX + xzMargin, minY + yMargin, maxZ + xzMargin, ignoreFlags);
        if (ignoreFlags == 0) {
            if (onGround) {
                if (xzMargin <= 0 && yMargin == 0) {
                    onGroundMinY = Math.min(onGroundMinY, yOnGround);
                }
            }
            else {
                if (xzMargin >= 0) {
                    notOnGroundMaxY = Math.max(notOnGroundMaxY, yOnGround);
                }
            }
        }
        return onGround;
    }

    /**
     * Check on-ground in a very opportunistic way, in terms of
     * fcfs+no-consistency+no-actual-side-condition-checks.
     * <hr>
     * Assume this gets called after the ordinary isOnGround has returned false.
     * 
     * @param loc
     * @param yShift
     * @param blockChangetracker
     * @param blockChangeRef
     * @return
     */
    public final boolean isOnGroundOpportune(
            final double yOnGround, final long ignoreFlags,
            final BlockChangeTracker blockChangeTracker, final BlockChangeReference blockChangeRef,
            final int tick) {
        // TODO: Consider updating onGround+dist cache.
        return blockChangeTracker.isOnGround(blockCache, blockChangeRef, tick, world.getUID(), 
                minX, minY - yOnGround, minZ, maxX, maxY, maxZ, ignoreFlags);
    }

    /**
     * Check if solid blocks hit the box.
     *
     * @param xzMargin
     *            the xz margin
     * @param yMargin
     *            the y margin
     * @return true, if is next to solid
     */
    public boolean isNextToSolid(final double xzMargin, final double yMargin) {
        // TODO: Adjust to check block flags ?
        return BlockProperties.collides(blockCache, minX - xzMargin, minY - yMargin, minZ - xzMargin, maxX + xzMargin, maxY + yMargin, maxZ + xzMargin, BlockProperties.F_SOLID);
    }

    /**
     * Convenience method for testing for either.
     *
     * @return true, if is on ground or reset cond
     */
    public boolean isOnGroundOrResetCond() {
        return isOnGround() || isResetCond();
    }

    /**
     * Gets the y on ground.
     *
     * @return the y on ground
     */
    public double getyOnGround() {
        return yOnGround;
    }

    /**
     * This resets onGround and blockFlags.
     *
     * @param yOnGround
     *            the new y on ground
     */
    public void setyOnGround(final double yOnGround) {
        this.yOnGround = yOnGround;
        this.onGround = null;
        blockFlags = null;
    }

    /**
     * Test if the foot location is passable (not the bounding box). <br>
     * The result is cached.
     *
     * @return true, if is passable
     */
    public boolean isPassable() {
        if (passable == null) {
            if (isBlockFlagsPassable()) {
                passable = true;
            }
            else {
                if (node == null) {
                    node = blockCache.getOrCreateBlockCacheNode(blockX, blockY, blockZ, false);
                }
                passable = BlockProperties.isPassable(blockCache, x, y, z, node, null);
                //passable = BlockProperties.isPassableExact(blockCache, x, y, z, getTypeId());
            }
        }
        return passable;
    }

    /**
     * Test if the bounding box is colliding (passable check with accounting for
     * workarounds).
     *
     * @return true, if is passable box
     */
    public boolean isPassableBox() {
        // TODO: Might need a variation with margins as parameters.
        if (passableBox == null) {
            if (isBlockFlagsPassable()) {
                passableBox = true;
            }
            else if (passable != null && !passable) {
                passableBox = false;
            }
            else {
                // Fetch.
                passableBox = BlockProperties.isPassableBox(blockCache, minX, minY, minZ, maxX, maxY, maxZ);
            }
        }
        return passableBox;
    }

    /**
     * Checks if block flags are set and are (entirely) passable.
     *
     * @return true, if is block flags passable
     */
    private boolean isBlockFlagsPassable() {
        return blockFlags != null && (blockFlags & (BlockProperties.F_SOLID | BlockProperties.F_GROUND)) == 0;
    }

    /**
     * Set block flags using yOnGround, unless already set. Check the maximally
     * used bounds for the block checking, to have flags ready for faster
     * denial.
     */
    public void collectBlockFlags() {
        if (blockFlags == null) {
            collectBlockFlags(yOnGround);
        }
    }

    /**
     * Check the maximally used bounds for the block checking, to have flags
     * ready for faster denial.
     *
     * @param maxYonGround
     *            the max yon ground
     */
    public void collectBlockFlags(double maxYonGround) {
        maxYonGround = Math.max(yOnGround, maxYonGround);
        // TODO: Clearly refine this for 1.5 high blocks.
        // TODO: Check which checks need blocks below.
        final double yExtra = 0.6; // y - blockY - maxYonGround > 0.5 ? 0.5 : 1.0;
        // TODO: xz margin still needed ?
        final double xzM = 0; //0.001;
        blockFlags = BlockProperties.collectFlagsSimple(blockCache, minX - xzM, minY - yExtra - maxYonGround, minZ - xzM, maxX + xzM, Math.max(maxY, minY + 1.5), maxZ + xzM);
    }

    /**
     * Check chunks within 1 block distance for if they are loaded and load unloaded chunks.
     * @return Number of chunks loaded.
     */
    public int ensureChunksLoaded() {
        return ensureChunksLoaded(1.0);
    }

    /**
     * Check chunks within xzMargin radius for if they are loaded and load
     * unloaded chunks.
     *
     * @param xzMargin
     *            the xz margin
     * @return Number of chunks loaded.
     */
    public int ensureChunksLoaded(final double xzMargin) {
        return MapUtil.ensureChunksLoaded(world, x, z, xzMargin);
    }

    /**
     * Check for tracked block changes, having moved a block into a certain
     * direction, using the full bounding box (pistons).
     * BlockChangeReference.updateSpan is called with the earliest entry found
     * (updateFinal has to be called extra). This is an opportunistic version
     * without any consistency checking done, just updating the span by the
     * earliest entry found.
     *
     * @param blockChangeTracker
     *            the block change tracker
     * @param ref
     *            the ref
     * @param direction
     *            Pass null to ignore the direction.
     * @param coverDistance
     *            The (always positive) distance to cover.
     * @return Returns true, iff an entry was found.
     */
    public boolean matchBlockChange(final BlockChangeTracker blockChangeTracker, final BlockChangeReference ref, 
            final Direction direction, final double coverDistance) {
        final int tick = TickTask.getTick();
        final UUID worldId = world.getUID();
        final int iMinX = Location.locToBlock(minX);
        final int iMaxX = Location.locToBlock(maxX);
        final int iMinY = Location.locToBlock(minY);
        final int iMaxY = Location.locToBlock(maxY);
        final int iMinZ = Location.locToBlock(minZ);
        final int iMaxZ = Location.locToBlock(maxZ);
        BlockChangeEntry minEntry = null;
        for (int x = iMinX; x <= iMaxX; x++) {
            for (int z = iMinZ; z <= iMaxZ; z++) {
                for (int y = iMinY; y <= iMaxY; y++) {
                    final BlockChangeEntry entry = blockChangeTracker.getBlockChangeEntry(ref, tick, worldId, 
                            x, y, z, direction);
                    if (entry != null && (minEntry == null || entry.id < minEntry.id)) {
                        // Check vs. coverDistance, exclude cases where the piston can't push that far.
                        if (coverDistance > 0.0 && coversDistance(x, y, z, direction, coverDistance)) {
                            minEntry = entry;
                        }
                    }
                }
            }
        }
        if (minEntry == null) {
            return false;
        }
        else {
            ref.updateSpan(minEntry);
            return true;
        }
    }

    /**
     * Check for tracked block changes, having moved a block into a certain
     * direction, confined to certain blocks hitting the player, using the full
     * bounding box (pistons), only regarding blocks having flags in common with
     * matchFlags. Thus not the replaced state at a position is regarded, but
     * the state that should result from a block having been pushed there.
     * BlockChangeReference.updateSpan is called with the earliest entry found
     * (updateFinal has to be called extra). This is an opportunistic version
     * without any consistency checking done, just updating the span by the
     * earliest entry found.
     *
     * @param blockChangeTracker
     *            the block change tracker
     * @param ref
     *            the ref
     * @param direction
     *            Pass null to ignore the direction.
     * @param coverDistance
     *            The (always positive) distance to cover.
     * @param matchFlags
     *            Only blocks with past states having any flags in common with
     *            matchFlags. If matchFlags is zero, the parameter is ignored.
     * @return Returns true, iff an entry was found.
     */
    public boolean matchBlockChangeMatchResultingFlags(final BlockChangeTracker blockChangeTracker, 
            final BlockChangeReference ref, final Direction direction, final double coverDistance, 
            final long matchFlags) {
        /*
         * TODO: Not sure with code duplication. Is it better to run
         * BlockChangeTracker.getBlockChangeMatchFlags for the other method too?
         */
        // TODO: Intended use is bouncing off slime, thus need confine to foot level ?
        final int tick = TickTask.getTick();
        final UUID worldId = world.getUID();
        // Shift the entire search box to the opposite direction (if direction is given).
        final BlockFace blockFace = direction == null ? BlockFace.SELF : direction.blockFace;
        final int iMinX = Location.locToBlock(minX) - blockFace.getModX();
        final int iMaxX = Location.locToBlock(maxX) - blockFace.getModX();
        final int iMinY = Location.locToBlock(minY) - blockFace.getModY();
        final int iMaxY = Location.locToBlock(maxY) - blockFace.getModY();
        final int iMinZ = Location.locToBlock(minZ) - blockFace.getModZ();
        final int iMaxZ = Location.locToBlock(maxZ) - blockFace.getModZ();
        BlockChangeEntry minEntry = null;
        for (int x = iMinX; x <= iMaxX; x++) {
            for (int z = iMinZ; z <= iMaxZ; z++) {
                for (int y = iMinY; y <= iMaxY; y++) {
                    final BlockChangeEntry entry = blockChangeTracker.getBlockChangeEntryMatchFlags(
                            ref, tick, worldId, x, y, z, direction, matchFlags);
                    if (entry != null && (minEntry == null || entry.id < minEntry.id)) {
                        // Check vs. coverDistance, exclude cases where the piston can't push that far.
                        if (coverDistance > 0.0 && coversDistance(
                                x + blockFace.getModX(), y + blockFace.getModY(), z + blockFace.getModZ(), 
                                direction, coverDistance)) {
                            minEntry = entry;
                        }
                    }
                }
            }
        }
        if (minEntry == null) {
            return false;
        }
        else {
            ref.updateSpan(minEntry);
            return true;
        }
    }

    /**
     * Test, if the block intersects the bounding box, if assuming full bounds.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return true, if the block is intersecting
     */
    public boolean isBlockIntersecting(final int x, final int y, final int z) {
        return CollisionUtil.intersectsBlock(minX, maxX, x)
                && CollisionUtil.intersectsBlock(minY, maxY, y)
                && CollisionUtil.intersectsBlock(minZ, maxZ, z);
    }

    /**
     * Test, if either of two blocks intersects the bounding box, if assuming
     * full bounds.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @param blockFace
     *            An additional block to check from the coordinates into that
     *            direction.
     * @return true, if either block is intersecting
     */
    public boolean isBlockIntersecting(final int x, final int y, final int z, final BlockFace blockFace) {
        return isBlockIntersecting(x, y, z) 
                || isBlockIntersecting(x + blockFace.getModX(), y + blockFace.getModY(), z + blockFace.getModZ());
    }

    /**
     * Test if a block fully moved into that direction can move the player by
     * coverDistance.
     *
     * @param x
     *            Block coordinates.
     * @param y
     *            the y
     * @param z
     *            the z
     * @param direction
     *            the direction
     * @param coverDistance
     *            the cover distance
     * @return true, if successful
     */
    private boolean coversDistance(final int x, final int y, final int z, final Direction direction, final double coverDistance) {
        switch (direction) {
            case Y_POS: {
                return y + 1.0 - Math.max(minY, (double) y) >= coverDistance;
            }
            case Y_NEG: {
                return Math.min(maxY, (double) y + 1) - y >= coverDistance;
            }
            case X_POS: {
                return x + 1.0 - Math.max(minX, (double) x) >= coverDistance;
            }
            case X_NEG: {
                return Math.min(maxX, (double) x + 1) - x >= coverDistance;
            }
            case Z_POS: {
                return z + 1.0 - Math.max(minZ, (double) z) >= coverDistance;
            }
            case Z_NEG: {
                return Math.min(maxZ, (double) z + 1) - z >= coverDistance;
            }
            default: {
                // Assume anything does (desired direction is NONE, read as ALL, thus accept all).
                return true;
            }
        }
    }

    /**
     * Set cached info according to other.<br>
     * Minimal optimizations: take block flags directly, on-ground max/min
     * bounds, only set stairs if not on ground and not reset-condition.
     *
     * @param other
     *            the other
     */
    public void prepare(final RichBoundsLocation other) {
        // Simple first.
        this.blockFlags = other.blockFlags; //  Assume set.
        this.notOnGroundMaxY = other.notOnGroundMaxY;
        this.onGroundMinY = other.onGroundMinY;
        this.passable = other.passable;
        this.passableBox = other.passableBox;
        // Access methods.
        this.node = other.node;
        this.nodeBelow = other.nodeBelow;
        this.onGround = other.isOnGround();
        this.inWater = other.isInWater();
        this.inLava = other.isInLava();
        this.inWeb = other.isInWeb();
        this.onIce = other.isOnIce();
        this.onSoulSand = other.isOnSoulSand();
        this.onClimbable = other.isOnClimbable();
        // Complex checks last.
        if (!onGround && !isResetCond()) {
            // TODO: if resetCond is checked, set on the other !?
            this.aboveStairs = other.isAboveStairs();
        }
    }

    /**
     * Sets the.
     *
     * @param location
     *            the location
     * @param fullWidth
     *            the full width
     * @param fullHeight
     *            the full height
     * @param yOnGround
     *            the y on ground
     */
    public void set(final Location location, final double fullWidth, final double fullHeight, final double yOnGround) {
        doSet(location, fullWidth, fullHeight, yOnGround);
    }

    /**
     * Do set.
     *
     * @param location
     *            the location
     * @param fullWidth
     *            the full width
     * @param fullHeight
     *            the full height
     * @param yOnGround
     *            the y on ground
     */
    protected void doSet(final Location location, final double fullWidth, final double fullHeight, final double yOnGround) {
        // Set coordinates.
        blockX = location.getBlockX();
        blockY = location.getBlockY();
        blockZ = location.getBlockZ();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        yaw = location.getYaw();
        pitch = location.getPitch();

        // Set bounding box.
        final double dxz = Math.round(fullWidth * 500.0) / 1000.0; // this.width / 2; // 0.3;
        minX = x - dxz;
        minY = y;
        minZ = z - dxz;
        maxX = x + dxz;
        maxY = y + fullHeight;
        maxZ = z + dxz;
        this.boxMarginHorizontal = dxz;
        this.boxMarginVertical = fullHeight;
        // TODO: With current bounding box the stance is never checked.

        // Set world / block access.
        world = location.getWorld();

        if (world == null) {
            throw new NullPointerException("World is null.");
        }

        // Reset cached values.
        node = nodeBelow = null;
        aboveStairs = inLava = inWater = inWeb = onIce = onSoulSand = onGround = onClimbable = passable = passableBox = null;
        onGroundMinY = Double.MAX_VALUE;
        notOnGroundMaxY = Double.MIN_VALUE;
        blockFlags = null;

        this.yOnGround = yOnGround;
    }

    /**
     * Set some references to null.
     */
    public void cleanup() {
        world = null;
        blockCache = null; // No reset here.
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override    public int hashCode() {
        return LocUtil.hashCode(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(128);
        builder.append("RichBoundsLocation(");
        builder.append(world == null ? "null" : world.getName());
        builder.append('/');
        builder.append(Double.toString(x));
        builder.append(", ");
        builder.append(Double.toString(y));
        builder.append(", ");
        builder.append(Double.toString(z));
        builder.append(')');
        return builder.toString();
    }

}
