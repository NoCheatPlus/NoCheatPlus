package fr.neatmonster.nocheatplus.utilities;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.compat.blocks.BlockChangeTracker;
import fr.neatmonster.nocheatplus.compat.blocks.BlockChangeTracker.BlockChangeEntry;
import fr.neatmonster.nocheatplus.compat.blocks.BlockChangeTracker.BlockChangeReference;
import fr.neatmonster.nocheatplus.compat.blocks.BlockChangeTracker.Direction;
import fr.neatmonster.nocheatplus.components.location.IBlockPosition;
import fr.neatmonster.nocheatplus.components.location.IBukkitLocation;
import fr.neatmonster.nocheatplus.components.location.IPosition;

/**
 * A location with bounds with a lot of extra stuff.
 * 
 * @author asofold
 *
 */
public class RichBoundsLocation implements IBukkitLocation, IBlockPosition {

    // TODO: Consider switching back from default to private visibility (use getters for other places).

    // Simple members // 

    /** Y parameter for growing the bounding box with the isOnGround check. */
    double yOnGround = 0.001;

    /** The block coordinates. */
    int blockX, blockY, blockZ;

    /** The exact coordinates. */
    double x, y, z;

    float yaw, pitch; // TODO: -> entity ?

    /** Bounding box. */
    double minX, maxX, minY, maxY, minZ, maxZ;

    // TODO: Check if onGround can be completely replaced by onGroundMinY and notOnGroundMaxY.
    /** Minimal yOnGround for which the player is on ground. No extra xz/y margin.*/
    double onGroundMinY = Double.MAX_VALUE;
    /** Maximal yOnGround for which the player is not on ground. No extra xz/y margin.*/
    double notOnGroundMaxY = Double.MIN_VALUE;


    // "Light" object members (reset to null on set) //

    // TODO: The following should be changed to primitive types, add one long for "checked"-flags. Booleans can be compressed into a long.
    // TODO: All properties that can be set should have a "checked" flag, thus resetting the flag suffices.

    /** Type id of the block at the position. */
    Integer typeId = null;

    /** Type id of the block below. */
    Integer typeIdBelow = null;

    /** Data value of the block this position is on. */
    Integer data = null;

    /** All block flags collected for maximum used bounds. */
    Long blockFlags = null;

    /** Is the player on ladder? */
    Boolean onClimbable = null;

    /** Simple test if the exact position is passable. */
    Boolean passable = null;

    /** Is the player above stairs? */
    Boolean aboveStairs = null;

    /** Is the player in lava? */
    Boolean inLava = null;

    /** Is the player in water? */
    Boolean inWater = null;

    /** Is the player is web? */
    Boolean inWeb = null;

    /** Is the player on ice? */
    Boolean onIce = null;

    /** Is the player on the ground? */
    Boolean onGround = null;


    // "Heavy" object members that need to be set to null on cleanup. //

    /** Block property access. */
    BlockCache blockCache = null;

    /** Bukkit world. */
    World world = null;


    public RichBoundsLocation(final BlockCache blockCache) {
        this.blockCache = blockCache;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public String getWorldName() {
        return world.getName();
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public float getYaw() {
        return yaw;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    public Vector getVector() {
        return new Vector(x, y, z);
    }

    /**
     * Gets a new Location instance representing this position.
     * 
     * @return the location
     * @throws NullPointerException, if the world stored internally is null.
     */
    public Location getLocation() {
        if (this.world == null) {
            throw new NullPointerException("World is null.");
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public int getBlockX() {
        return blockX;
    }

    @Override
    public int getBlockY() {
        return blockY;
    }

    @Override
    public int getBlockZ() {
        return blockZ;
    }

    /**
     * Return the bounding box as a new double array (minX, minY, minZ, maxX,
     * maxY, maxZ).
     * 
     * @return
     */
    public double[] getBoundsAsDoubles() {
        return new double[] {minX, minY, minZ, maxX, maxY, maxZ};
    }

    /**
     * Compares block coordinates (not the world).
     * 
     * @param other
     * @return
     */
    public final boolean isSameBlock(final IBlockPosition other) {
        return blockX == other.getBlockX() && blockZ == other.getBlockZ() && blockY == other.getBlockY();
    }

    /**
     * Block coordinate comparison.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public final boolean isSameBlock(final int x, final int y, final int z) {
        return blockX == x && blockZ == z && blockY == y;
    }

    /**
     * Compares block coordinates (not the world).
     * @param loc
     * @return
     */
    public final boolean isSameBlock(final Location loc) {
        return blockX == loc.getBlockX() && blockZ == loc.getBlockZ() && blockY == loc.getBlockY();
    }

    /**
     * Check if this location is above the given one (blockY + 1).
     * @param loc
     * @return
     */
    public boolean isBlockAbove(final IBlockPosition loc) {
        return blockY == loc.getBlockY() + 1 && blockX == loc.getBlockX() && blockZ == loc.getBlockZ();
    }

    /**
     * Check if this location is above the given one (blockY + 1).
     * @param loc
     * @return
     */
    public boolean isBlockAbove(final Location loc) {
        return blockY == loc.getBlockY() + 1 && blockX == loc.getBlockX() && blockZ == loc.getBlockZ();
    }

    /**
     * Compares exact coordinates (not the world).
     * 
     * @param loc
     * @return
     */
    public boolean isSamePos(final IPosition loc) {
        return x == loc.getX() && z == loc.getZ() && y == loc.getY();
    }

    /**
     * Compares exact coordinates (not the world).
     * 
     * @param loc
     * @return
     */
    public boolean isSamePos(final Location loc) {
        return x == loc.getX() && z == loc.getZ() && y == loc.getY();
    }

    /**
     * Manhattan distance, see Trigutil.
     * @param other
     * @return
     */
    public int manhattan(final IBlockPosition other) {
        // TODO: Consider using direct field access from other methods as well.
        return TrigUtil.manhattan(this.blockX, this.blockY, this.blockZ, other.getBlockX(), other.getBlockY(), other.getBlockZ());
    }

    /**
     * Maximum block distance comparing dx, dy, dz.
     * @param other
     * @return
     */
    public int maxBlockDist(final IBlockPosition other) {
        // TODO: Consider using direct field access from other methods as well.
        return TrigUtil.maxDistance(this.blockX, this.blockY, this.blockZ, other.getBlockX(), other.getBlockY(), other.getBlockZ());
    }

    /**
     * Quick check for really bad coordinates (actual problem, if true is returned.).
     * @return
     */
    public boolean hasIllegalCoords() {
        return CheckUtils.isBadCoordinate(minX, maxX, minY, maxY, minZ, maxZ);
    }

    /**
     * Get the collected block-flags. This will return null if collectBlockFlags has not been called.
     * @return
     */
    public Long getBlockFlags() {
        return blockFlags;
    }

    /**
     * Set the block flags which are usually collected on base of bounding box, yOnGround and other considerations, such as 1.5 high blocks.
     * @param blockFlags
     */
    public void setBlockFlags(Long blockFlags) {
        this.blockFlags = blockFlags;
    }

    /**
     * Not cached.
     * @return
     */
    public int getTypeIdAbove() {
        return blockCache.getTypeId(blockX, blockY + 1,  blockZ);
    }

    /**
     * Convenience method: delegate to BlockProperties.isDoppwnStream .
     * 
     * @param xDistance
     * @param zDistance
     * @return
     */
    public boolean isDownStream(final double xDistance, final double zDistance)
    {
        return BlockProperties.isDownStream(blockCache, blockX, blockY, blockZ, getData(), xDistance, zDistance);
    }

    public Integer getTypeId() {
        if (typeId == null) typeId = getTypeId(blockX, blockY, blockZ);
        return typeId;
    }

    public Integer getTypeIdBelow() {
        if (typeIdBelow == null) typeIdBelow = getTypeId(blockX, blockY - 1, blockZ);
        return typeIdBelow;
    }

    public Integer getData() {
        if (data == null) data = getData(blockX, blockY, blockZ);
        return data;
    }

    /**
     * Uses id cache if present.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public final int getTypeId(final int x, final int y, final int z) {
        return blockCache.getTypeId(x, y, z);
    }

    /**
     * Uses id cache if present.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public final int getData(final int x, final int y, final int z) {
        return blockCache.getData(x, y, z);
    }

    /**
     * Set the id cache for faster id getting.
     * 
     * @param cache
     */
    public void setBlockCache(final BlockCache cache) {
        this.blockCache = cache;
    }

    /**
     * Get the underlying BLockCache.
     * @return
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
     * Checks if the player is on a ladder or vine.
     * 
     * @return If so.
     */
    public boolean isOnClimbable() {
        if (onClimbable == null) {
            // Climbable blocks.
            if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_CLIMBABLE) == 0 ) {
                onClimbable = false;
                return false;
            }
            onClimbable = (BlockProperties.getBlockFlags(getTypeId()) & BlockProperties.F_CLIMBABLE) != 0;
            // TODO: maybe use specialized bounding box.
            //          final double d = 0.1d;
            //          onClimbable = BlockProperties.collides(getBlockAccess(), minX - d, minY - d, minZ - d, maxX + d, minY + 1.0, maxZ + d, BlockProperties.F_CLIMBABLE);
        }
        return onClimbable;
    }

    /**
     * Check if solid blocks hit the box.
     * @param xzMargin
     * @param yMargin
     * @return
     */
    public boolean isNextToGround(final double xzMargin, final double yMargin) {
        // TODO: Adjust to check block flags ?
        return BlockProperties.collides(blockCache, minX - xzMargin, minY - yMargin, minZ - xzMargin, maxX + xzMargin, maxY + yMargin, maxZ + xzMargin, BlockProperties.F_GROUND);
    }

    /**
     * Reset condition for flying checks (sf + nofall): liquids, web, ladder (not on-ground, though).
     * @return
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
     * @return
     */
    public boolean standsOnBlock(final int id) {
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
            // TODO: inset still needed ?
            final double inset = 0.001d;
            inWeb = BlockProperties.collidesId(blockCache, minX + inset, minY + inset, minZ + inset, maxX - inset, maxY - inset, maxZ - inset, Material.WEB);
        }
        return inWeb;
    }

    /**
     * Check the location is on ice, only regarding the center. Currently
     * demands to be on ground as well.
     * 
     * @return
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
                final int id = bY == blockY ? getTypeId() : (bY == blockY -1 ? getTypeIdBelow() : blockCache.getTypeId(blockX,  bY, blockZ));
                final long flags = BlockProperties.getBlockFlags(id);
                // TODO: Might remove check for variable ?
                if ((flags & BlockProperties.F_GROUND) != 0 && (flags & BlockProperties.F_VARIABLE) == 0) {
                    final double[] bounds = blockCache.getBounds(blockX, bY, blockZ);
                    // Check collision if not inside of the block. [Might be a problem for cauldron or similar + something solid above.]
                    // TODO: Might need more refinement.
                    if (bounds != null && y - bY >= bounds[4] && BlockProperties.collidesBlock(blockCache, x, minY - yOnGround, z, x, minY, z, blockX, bY, blockZ, id, bounds, flags)) {
                        // TODO: BlockHeight is needed for fences, use right away (above)?
                        if (!BlockProperties.isPassableWorkaround(blockCache, blockX, bY, blockZ, minX - blockX, minY - yOnGround - bY, minZ - blockZ, id, maxX - minX, yOnGround, maxZ - minZ,  1.0)
                                || (flags & BlockProperties.F_GROUND_HEIGHT) != 0 &&  BlockProperties.getGroundMinHeight(blockCache, blockX, bY, blockZ, id, bounds, flags) <= y - bY) {
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
     * Simple block-on-ground check for given margin (no entities). Meant for checking bigger margin than the normal yOnGround.
     * @param yOnGround Margin below the player.
     * @return
     */
    public boolean isOnGround(final double yOnGround) {
        if (notOnGroundMaxY >= yOnGround) return false;
        else if (onGroundMinY <= yOnGround) return true;
        return  isOnGround(yOnGround, 0D, 0D, 0L);
    }

    /**
     * SSimple block-on-ground check for given margin (no entities). Meant for checking bigger margin than the normal yOnGround.
     * @param yOnGround
     * @param ignoreFlags Flags to not regard as ground.
     * @return
     */
    public boolean isOnGround(final double yOnGround, final long ignoreFlags) {
        if (ignoreFlags == 0) {
            if (notOnGroundMaxY >= yOnGround) return false;
            else if (onGroundMinY <= yOnGround) return true;
        }
        return isOnGround(yOnGround, 0D, 0D, ignoreFlags);
    }


    /**
     * Simple block-on-ground check for given margin (no entities). Meant for checking bigger margin than the normal yOnGround.
     * @param yOnGround Margin below the player.
     * @param xzMargin
     * @param yMargin Extra margin added below and above.
     * @return
     */
    public boolean isOnGround(final double yOnGround, final double xzMargin, final double yMargin) {
        if (xzMargin >= 0 && onGroundMinY <= yOnGround) return true;
        if (xzMargin <= 0 && yMargin == 0) {
            if (notOnGroundMaxY >= yOnGround) return false;
        }
        return isOnGround(yOnGround, xzMargin, yMargin, 0);
    }

    /**
     * Simple block-on-ground check for given margin (no entities). Meant for checking bigger margin than the normal yOnGround.
     * @param yOnGround Margin below the player.
     * @param xzMargin
     * @param yMargin Extra margin added below and above.
     * @param ignoreFlags Flags to not regard as ground.
     * @return
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
     * Check if solid blocks hit the box.
     * @param xzMargin
     * @param yMargin
     * @return
     */
    public boolean isNextToSolid(final double xzMargin, final double yMargin) {
        // TODO: Adjust to check block flags ?
        return BlockProperties.collides(blockCache, minX - xzMargin, minY - yMargin, minZ - xzMargin, maxX + xzMargin, maxY + yMargin, maxZ + xzMargin, BlockProperties.F_SOLID);
    }

    /**
     * Convenience method for testing for either.
     * @return
     */
    public boolean isOnGroundOrResetCond() {
        return isOnGround() || isResetCond();
    }

    public double getyOnGround() {
        return yOnGround;
    }

    /**
     * This resets onGround and blockFlags.
     * @param yOnGround
     */
    public void setyOnGround(final double yOnGround) {
        this.yOnGround = yOnGround;
        this.onGround = null;
        blockFlags = null;
    }

    /**
     * Test if the foot location is passable (not the bounding box).
     * <br>The result is cached.
     * @return
     */
    public boolean isPassable() {
        if (passable == null) {
            passable = BlockProperties.isPassable(blockCache, x, y, z, getTypeId());
            //          passable = BlockProperties.isPassableExact(blockCache, x, y, z, getTypeId());
        }
        return passable;
    }

    /**
     * Set block flags using yOnGround, unless already set. Check the maximally
     * used bounds for the block checking, to have flags ready for faster
     * denial.
     * 
     * @param maxYonGround
     */
    public void collectBlockFlags() {
        if (blockFlags == null) {
            collectBlockFlags(yOnGround);
        }
    }

    /**
     * Check the maximally used bounds for the block checking,
     * to have flags ready for faster denial.
     * @param maxYonGround
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
     * Check chunks within xzMargin radius for if they are loaded and load unloaded chunks.
     * @param xzMargin
     * @return Number of chunks loaded.
     */
    public int ensureChunksLoaded(final double xzMargin) {
        return BlockCache.ensureChunksLoaded(world, x, z, xzMargin);
    }

    /**
     * Check for push using the full bounding box (pistons). The given
     * BlockChangeReference is not changed, it has to be updated externally.
     * 
     * @param blockChangeTracker
     * @param oldChangeId
     * @param direction
     * @param coverDistance
     *            The (always positive) distance to cover.
     * @return A matching BlockChangeEntry with the minimal id. If no entry was
     *         found, null is returned.
     */
    public BlockChangeEntry getBlockChangeIdPush(final BlockChangeTracker blockChangeTracker, final BlockChangeReference ref, final Direction direction, final double coverDistance) {
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
                    final BlockChangeEntry entry = blockChangeTracker.getBlockChangeEntry(ref, tick, worldId, x, y, z, direction);
                    if (entry != null && (minEntry == null || entry.id < minEntry.id)) {
                        // Check vs. coverDistance, exclude cases where the piston can't push that far.
                        if (coverDistance > 0.0 && coversDistance(x, y, z, direction, coverDistance)) {
                            minEntry = entry;
                        }
                    }
                }
            }
        }
        return minEntry;
    }

    /**
     * Test, if the block intersects the bounding box, if assuming full bounds.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean isBlockIntersecting(final int x, final int y, final int z) {
        return TrigUtil.intersectsBlock(minX, maxX, x)
                && TrigUtil.intersectsBlock(minY, maxY, y)
                && TrigUtil.intersectsBlock(minZ, maxZ, z);
    }

    /**
     * Test if a block fully pushed into that direction can push the player by coverDistance.
     * 
     * @param x Block coordinates.
     * @param y
     * @param z
     * @param direction
     * @param coverDistance
     * @return
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
     * Minimal optimizations: take block flags directly, on-ground max/min bounds, only set stairs if not on ground and not reset-condition.
     * @param other
     */
    public void prepare(final RichBoundsLocation other) {
        // Simple first.
        this.blockFlags = other.blockFlags; //  Assume set.
        this.notOnGroundMaxY = other.notOnGroundMaxY;
        this.onGroundMinY = other.onGroundMinY;
        this.passable = other.passable;
        // Access methods.
        this.typeId = other.getTypeId();
        this.typeIdBelow = other.getTypeIdBelow();
        this.onGround = other.isOnGround();
        this.inWater = other.isInWater();
        this.inLava = other.isInLava();
        this.inWeb = other.isInWeb();
        this.onIce = other.isOnIce();
        this.onClimbable = other.isOnClimbable();
        // Complex checks last.
        if (!onGround && !isResetCond()) {
            // TODO: if resetCond is checked, set on the other !?
            this.aboveStairs = other.isAboveStairs();
        }
    }

    public void set(final Location location, final double fullWidth, final double fullHeight, final double yOnGround) {

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
        // TODO: With current bounding box the stance is never checked.

        // Set world / block access.
        world = location.getWorld();

        if (world == null) {
            throw new NullPointerException("World is null.");
        }

        // Reset cached values.
        typeId = typeIdBelow = data = null;
        aboveStairs = inLava = inWater = inWeb = onIce = onGround = onClimbable = passable = null;
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
