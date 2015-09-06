package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.compat.MCAccess;

/**
 * An utility class used to know a lot of things for a player and a location
 * given.
 */
public class PlayerLocation {

    // Final members //
    private final MCAccess mcAccess;

    // Simple members // 

    /** Y parameter for growing the bounding box with the isOnGround check. */
    private double yOnGround = 0.001;

    /** The block coordinates. */
    private int blockX, blockY, blockZ;

    /** The exact coordinates. */
    private double x, y, z;

    private float yaw, pitch;

    /** Full bounding box width. */
    private double width;

    private double height;

    private double eyeHeight;

    /** Bounding box of the player. */
    private double minX, maxX, minY, maxY, minZ, maxZ;

    // TODO: Check if onGround can be completely replaced by onGroundMinY and notOnGroundMaxY.
    /** Minimal yOnGround for which the player is on ground. No extra xz/y margin.*/
    private double onGroundMinY = Double.MAX_VALUE;
    /** Maximal yOnGround for which the player is not on ground. No extra xz/y margin.*/
    private double notOnGroundMaxY = Double.MIN_VALUE;

    // "Light" object members (reset to null) //

    // TODO: The following should be changed to primitive types, add one long for "checked"-flags. Booleans can be compressed into a long.
    // TODO: All properties that can be set should have a "checked" flag, thus resetting the flag suffices.

    /** Type id of the block at the position. */
    private Integer typeId = null;

    /** Type id of the block below. */
    private Integer typeIdBelow = null;

    /** Data value of the block this position is on. */
    private Integer data = null;

    /** All block flags collected for maximum used bounds. */
    private Long blockFlags = null;

    /** Is the player on ice? */
    private Boolean onIce = null;

    /** Is the player on ladder? */
    private Boolean onClimbable = null;

    /** Simple test if the exact position is passable. */
    private Boolean passable = null;

    /** Is the player above stairs? */
    private Boolean aboveStairs = null;

    /** Is the player in lava? */
    private Boolean inLava = null;

    /** Is the player in water? */
    private Boolean inWater = null;

    /** Is the player is web? */
    private Boolean inWeb = null;

    /** Is the player on the ground? */
    private Boolean onGround = null;

    // "Heavy" object members (reset to null for cleanup). //

    /** The player ! */
    private Player player = null;

    /** Bukkit world. */
    private World world = null;

    // "Heavy" object members (further cleanup call needed + set to null for cleanup) //

    /** Optional block property cache. */
    private BlockCache blockCache = null;


    public PlayerLocation(final MCAccess mcAccess, final BlockCache blockCache) {
        this.mcAccess = mcAccess;
        this.blockCache = blockCache;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the location.
     * 
     * @return the location
     * @throws NullPointerException, if the world stored internally is null.
     */
    public Location getLocation() {
        if (this.world == null) {
            throw new NullPointerException("World is null.");
        }
        return new Location(world, x, y, z);
    }

    /**
     * Get the world!
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     * Gets the blockX.
     * 
     * @return the blockX
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the boundY.
     * 
     * @return the boundY
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the blockZ.
     * 
     * @return the blockZ
     */
    public double getZ() {
        return z;
    }

    /**
     * Gets the yaw.
     * 
     * @return the yaw
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Gets the pitch.
     * 
     * @return the pitch
     */
    public float getPitch() {
        return pitch;
    }

    public Vector getVector() {
        return new Vector(x, y, z);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getEyeHeight() {
        return eyeHeight;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    /**
     * Compares block coordinates (not the world).
     * 
     * @param other
     * @return
     */
    public final boolean isSameBlock(final PlayerLocation other) {
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
    public boolean isBlockAbove(final PlayerLocation loc) {
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
    public boolean isSamePos(final PlayerLocation loc) {
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
    public int manhattan(final PlayerLocation other) {
        // TODO: Consider using direct field access from other methods as well.
        return TrigUtil.manhattan(this.blockX, this.blockY, this.blockZ, other.blockX, other.blockY, other.blockZ);
    }

    /**
     * Maximum block distance comparing dx, dy, dz.
     * @param other
     * @return
     */
    public int maxBlockDist(final PlayerLocation other) {
        // TODO: Consider using direct field access from other methods as well.
        return TrigUtil.maxDistance(this.blockX, this.blockY, this.blockZ, other.blockX, other.blockY, other.blockZ);
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
     * Checks if the player is on ice.
     * 
     * @return true, if the player is on ice
     */
    public boolean isOnIce() {
        if (onIce == null) {
            // TODO: Use a box here too ?
            // TODO: check if player is really sneaking (refactor from survivalfly to static access in Combined ?)!
            if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_ICE) == 0) {
                // TODO: check onGroundMinY !?
                onIce = false;
            } else {
                final int id;
                if (player.isSneaking() || player.isBlocking()) {
                    id = getTypeId(blockX, Location.locToBlock(minY - 0.1D), blockZ);
                }
                else {
                    id = getTypeIdBelow().intValue();
                }
                onIce = BlockProperties.isIce(id);
            }
        }
        return onIce;
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
     * Check if a player may climb upwards (isOnClimbable returned true, player does not move from/to ground).<br>
     * Having checked the other stuff is prerequisite for calling this (!).
     * @param jumpHeigth Height the player is allowed to have jumped.
     * @return
     */
    public boolean canClimbUp(double jumpHeigth) {
        // TODO: distinguish vines.
        if (BlockProperties.isAttachedClimbable(getTypeId())) {
            // Check if vine is attached to something solid
            if (BlockProperties.canClimbUp(blockCache, blockX, blockY, blockZ)) {
                return true;
            }
            // Check the block at head height.
            final int headY = Location.locToBlock(y + eyeHeight);
            if (headY > blockY) {
                for (int cy = blockY + 1; cy <= headY; cy ++) {
                    if (BlockProperties.canClimbUp(blockCache, blockX, cy, blockZ)) {
                        return true;
                    }
                }
            }
            // Finally check possible jump height.
            // TODO: This too is inaccurate.
            if (isOnGround(jumpHeigth)) {
                // Here ladders are ok.
                return true;
            }
            return false;
        }
        return true;
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
     * Checks if the player is on ground, including entities such as Minecart, Boat.
     * 
     * @return true, if the player is on ground
     */
    public boolean isOnGround() {
        if (onGround != null) {
            return onGround;
        }
        // Check cached values and simplifications.
        if (notOnGroundMaxY >= yOnGround) onGround = false;
        else if (onGroundMinY <= yOnGround) onGround = true;
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
            else onGround = false;
        }
        if (onGround) onGroundMinY = Math.min(onGroundMinY, yOnGround);
        else {
            //          NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, "*** onground check entities");
            // TODO: further confine this ?
            notOnGroundMaxY = Math.max(notOnGroundMaxY, yOnGround);
            final double d1 = 0.25D;
            onGround = blockCache.standsOnEntity(player, minX - d1, minY - yOnGround - d1, minZ - d1, maxX + d1, minY + 0.25 + d1, maxZ + d1);
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
     * Simple check with custom margins (Boat, Minecart). 
     * @param yOnGround Margin below the player.
     * @param xzMargin
     * @param yMargin Extra margin added below and above.
     * @return
     */
    public boolean standsOnEntity(final double yOnGround, final double xzMargin, final double yMargin) {
        return blockCache.standsOnEntity(player, minX - xzMargin, minY - yOnGround - yMargin, minZ - xzMargin, maxX + xzMargin, minY + yMargin, maxZ + xzMargin);
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
     * Test if something solid/ground-like collides within the given margin
     * above the height of the player.
     * 
     * @param marginAboveHeight
     * @return
     */
    public boolean isHeadObstructed(double marginAboveHeight) {
        return BlockProperties.collides(blockCache, x - width, y + eyeHeight, z - width, x + width, y + eyeHeight + marginAboveHeight, z + width, BlockProperties.F_GROUND | BlockProperties.F_SOLID);
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
     * Sets the player location object.
     * 
     * @param location
     *            the location
     * @param player
     *            the player
     * @throws NullPointerException, if player.getLocation.getWorld() returns null.
     */
    public void set(final Location location, final Player player) {
        set(location, player, 0.001);
    }

    /**
     * Sets the player location object. Does not set or reset blockCache.
     * 
     * @param location
     *            the location
     * @param player
     *            the player
     * @throws NullPointerException, if Location.getWorld() returns null.
     */
    public void set(final Location location, final Player player, final double yOnGround)
    {

        // Entity reference.
        this.player = player;

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
        this.width = mcAccess.getWidth(player);
        this.height = mcAccess.getHeight(player);
        this.eyeHeight = player.getEyeHeight();
        final double dxz = Math.round(this.width * 500.0) / 1000.0; // this.width / 2; // 0.3;
        minX = x - dxz;
        minY = y;
        minZ = z - dxz;
        maxX = x + dxz;
        maxY = y + eyeHeight;
        maxZ = z + dxz;
        // TODO: With current bounding box the stance is never checked.

        // Set world / block access.
        world = location.getWorld();

        if (world == null) {
            throw new NullPointerException("World is null.");
        }

        // Reset cached values.
        typeId = typeIdBelow = data = null;
        aboveStairs = inLava = inWater = inWeb = onGround = onIce = onClimbable = passable = null;
        onGroundMinY = Double.MAX_VALUE;
        notOnGroundMaxY = Double.MIN_VALUE;
        blockFlags = null;

        this.yOnGround = yOnGround;
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
     * Set some references to null.
     */
    public void cleanup() {
        player = null;
        world = null;
        blockCache = null; // No reset here.
    }

    /**
     * Check absolute coordinates and stance for (typical) exploits.
     * 
     * @return
     * @deprecated Not used anymore (hasIllegalCoords and hasIllegalStance are used individually instead).
     */
    public boolean isIllegal() {
        if (hasIllegalCoords()) {
            return true;
        } else {
            return hasIllegalStance();
        }
    }

    /**
     * Check for bounding box properties that might crash the server (if available, not the absolute coordinates).
     * @return
     */
    public boolean hasIllegalStance() {
        return mcAccess.isIllegalBounds(player).decide(); // MAYBE = NO
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
     * Set cached info according to other.<br>
     * Minimal optimizations: take block flags directly, on-ground max/min bounds, only set stairs if not on ground and not reset-condition.
     * @param other
     */
    public void prepare(final PlayerLocation other) {
        this.onGround = other.isOnGround();
        this.inWater = other.isInWater();
        this.inLava = other.isInLava();
        this.inWeb = other.isInWeb();
        this.onClimbable = other.isOnClimbable();
        if (!onGround && !isResetCond()) {
            this.aboveStairs = other.isAboveStairs();
        }
        this.onIce = other.isOnIce();
        this.typeId = other.getTypeId();
        this.typeIdBelow = other.getTypeIdBelow();
        this.notOnGroundMaxY = other.notOnGroundMaxY;
        this.onGroundMinY = other.onGroundMinY;
        this.blockFlags = other.blockFlags; //  Assume set.
    }

}
