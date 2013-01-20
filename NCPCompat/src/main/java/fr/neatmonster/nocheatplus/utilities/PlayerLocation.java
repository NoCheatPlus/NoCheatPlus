package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.MCAccess;

/*
 * MM"""""""`YM dP                                     
 * MM  mmmmm  M 88                                     
 * M'        .M 88 .d8888b. dP    dP .d8888b. 88d888b. 
 * MM  MMMMMMMM 88 88'  `88 88    88 88ooood8 88'  `88 
 * MM  MMMMMMMM 88 88.  .88 88.  .88 88.  ... 88       
 * MM  MMMMMMMM dP `88888P8 `8888P88 `88888P' dP       
 * MMMMMMMMMMMM                  .88                   
 *                           d8888P                    
 *                           
 * M""MMMMMMMM                              dP   oo                   
 * M  MMMMMMMM                              88                        
 * M  MMMMMMMM .d8888b. .d8888b. .d8888b. d8888P dP .d8888b. 88d888b. 
 * M  MMMMMMMM 88'  `88 88'  `"" 88'  `88   88   88 88'  `88 88'  `88 
 * M  MMMMMMMM 88.  .88 88.  ... 88.  .88   88   88 88.  .88 88    88 
 * M         M `88888P' `88888P' `88888P8   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMM                                                        
 */
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

	private double width;
	
	/** Bounding box of the player. */
	private double minX, maxX, minY, maxY, minZ, maxZ;
	
	// Object members (reset to null) //

	/** Type id of the block at the position. */
	private Integer typeId = null;

	/** Type id of the block below. */
	private Integer typeIdBelow = null;

	/** Data value of the block this position is on. */
	private Integer data = null;

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

	/** Is the player on ice? */
	private Boolean onIce = null;

	/** Is the player on ladder? */
	private Boolean onClimbable = null;

	/** Simple test if the exact position is passable. */
	private Boolean passable = null;
	
	/** All block flags collected for maximum used bounds. */
	private Long blockFlags = null;

	// "Heavy" members (should be reset to null or cleaned up at some point) //

	/** The player ! */
	private Player player = null;
	
	/** Bukkit world. */
	private World world = null;

	/** Optional block property cache. */
	private BlockCache blockCache = null;
	
	
	public PlayerLocation(final MCAccess mcAccess, final BlockCache blockCache){
		this.mcAccess = mcAccess;
		this.blockCache = blockCache;
	}

	/**
	 * Gets the location.
	 * 
	 * @return the location
	 */
	public Location getLocation() {
		return new Location(world, x, y, z);
	}
	
	/**
	 * Get the world!
	 * @return
	 */
	public World getWorld(){
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
		return x == loc.getBlockX() && z == loc.getBlockZ() && y == loc.getBlockY();
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
	 * Checks if the player is above stairs.
	 * 
	 * @return true, if the player above on stairs
	 */
	public boolean isAboveStairs() {
		if (aboveStairs == null) {
			if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_STAIRS) == 0 ){
				aboveStairs = false;
				return false;
			}
			// TODO: Distinguish based on actual height off .0 ?
			final double diff = 0.001;
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
			if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_LAVA) == 0 ){
				inLava = false;
				return false;
			}
			final double dX = -0.10000000149011612D;
			final double dY = -0.40000000596046448D;
			final double dZ = dX;
			inLava = BlockProperties.collides(blockCache, minX - dX, minY - dY, minZ - dZ, maxX + dX, maxY + dY, maxZ + dZ, BlockProperties.F_LAVA);
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
			if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_WATER) == 0 ){
				inWater = false;
				return false;
			}
			final double dX = -0.001D;
			final double dY = -0.40000000596046448D - 0.001D;
			final double dZ = -0.001D;
			inWater = BlockProperties.collides(blockCache, minX - dX, minY - dY, minZ - dZ, maxX + dX, maxY + dY, maxZ + dZ, BlockProperties.F_WATER);
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
		return isInLava() || isInWater();
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
			if (player.isSneaking() || player.isBlocking()) onIce = getTypeId(blockX, Location.locToBlock(minY - 0.1D), blockZ) == Material.ICE.getId();
			else onIce = getTypeIdBelow().intValue() == Material.ICE.getId();
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
			if (blockFlags != null && (blockFlags.longValue() & BlockProperties.F_CLIMBABLE) == 0 ){
				onClimbable = false;
				return false;
			}
			onClimbable = (BlockProperties.getBLockFlags(getTypeId()) & BlockProperties.F_CLIMBABLE) != 0;
			// TODO: maybe use specialized bounding box.
//			final double d = 0.1d;
//			onClimbable = BlockProperties.collides(getBlockAccess(), minX - d, minY - d, minZ - d, maxX + d, minY + 1.0, maxZ + d, BlockProperties.F_CLIMBABLE);
		}
		return onClimbable;
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
		return (BlockProperties.getBLockFlags(getTypeIdBelow()) & BlockProperties.F_CLIMBABLE) != 0;
	}

	/**
	 * Checks if the player is in web.
	 * 
	 * @return true, if the player is in web
	 */
	public boolean isInWeb() {
		if (inWeb == null) {
			final double inset = 0.001d;
			inWeb = BlockProperties.collidesId(blockCache, minX + inset, minY + inset, minZ + inset, maxX - inset, maxY - inset, maxZ - inset, Material.WEB.getId());
		}
		return inWeb;
	}

	/**
	 * Checks if the player is on ground.
	 * 
	 * @return true, if the player is on ground
	 */
	public boolean isOnGround() {
		if (onGround == null) {
			final double d0 = 0; //0.001D;
			if (blockFlags == null || (blockFlags.longValue() & BlockProperties.F_GROUND) != 0){
				if (BlockProperties.collidesBlock(blockCache, x, minY - yOnGround, z, x, minY + 0.25, z, blockX, blockY, blockZ, getTypeId())){
					onGround = true;
				}
				else onGround = BlockProperties.isOnGround(blockCache, minX - d0, minY - yOnGround, minZ - d0, maxX + d0, minY + 0.25, maxZ + d0);
			}
			else onGround = false;
			if (!onGround) {
				final double d1 = 0.25D;
				onGround = blockCache.standsOnEntity(player, minX - d1, minY - getyOnGround() - d1, minZ - d1, maxX + d1, minY + 0.25 + d1, maxZ + d1);
			}
		}
		return onGround;
	}
	
	/**
	 * Reset condition for flying checks (sf + nofall): fluids, web, ladder (not on-ground, though).
	 * @return
	 */
	public boolean isResetCond(){
		// NOTE: if optimizing, setYOnGround has to be kept in mind. 
		return isInLiquid()  || isOnClimbable() || isInWeb();
	}

	public double getyOnGround() {
		return yOnGround;
	}

	public void setyOnGround(final double yOnGround) {
		this.yOnGround = yOnGround;
		this.onGround = null;
	}

	/**
	 * Simple at the spot passability test, no bounding boxes.
	 * 
	 * @return
	 */
	public boolean isPassable() {
		if (passable == null) passable = BlockProperties.isPassable(blockCache, x, y, z, getTypeId());
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
	 */
	public void set(final Location location, final Player player, final double yFreedom)
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
//		final double dX = x - entityPlayer.locX;
//		final double dY = y - entityPlayer.locY;
//		final double dZ = z - entityPlayer.locZ;
//		minX = x - entityPlayer.boundingBox.a + dX;
//		minY = y - entityPlayer.boundingBox.b + dY;
//		minZ = z - entityPlayer.boundingBox.c + dZ;
//		maxX = x + entityPlayer.boundingBox.d + dX;
//		maxY = y + entityPlayer.boundingBox.e + dY;
//		maxZ = z + entityPlayer.boundingBox.f + dZ;
//		// TODO: inset, outset ?
		this.width = mcAccess.getWidth(player);
		final double dxz = this.width / 2;
//		final double dX = (entityPlayer.boundingBox.d - entityPlayer.boundingBox.a) / 2D;
//		final double dY = entityPlayer.boundingBox.e - entityPlayer.boundingBox.b;
//		final double dZ = (entityPlayer.boundingBox.f - entityPlayer.boundingBox.c) / 2D;
		minX = x - dxz;
		minY = y;
		minZ = z - dxz;
		maxX = x + dxz;
		maxY = y + player.getEyeHeight();
		maxZ = z + dxz;
		// TODO: With current bounding box the stance is never checked.

		// Set world / block access.
		world = location.getWorld();

		// Reset cached values.
		typeId = typeIdBelow = data = null;
		aboveStairs = inLava = inWater = inWeb = onGround = onIce = onClimbable = passable = null;
		blockFlags = null;

		// TODO: Consider blockCache.setAccess? <- currently rather not, because
		// it might be anything.

		this.setyOnGround(yFreedom);
	}
	
	/**
	 * Check the maximally used bounds for the block checking,
	 * to have flags ready for faster denial.
	 * @param maxYonGround
	 */
	public void collectBlockFlags(double maxYonGround){
		blockFlags = BlockProperties.collectFlagsSimple(blockCache, minX - 0.001, minY - Math.max(Math.max(1.0, yOnGround), maxYonGround), minZ - 0.001, maxX + 0.001, maxY + .25, maxZ + .001);
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
	 * Attempt to check for some exploits (!).
	 * 
	 * @return
	 */
	public boolean isIllegal() {
		final AlmostBoolean spec = mcAccess.isIllegalBounds(player);
		if (spec != AlmostBoolean.MAYBE) return spec.decide();
		else if (Math.abs(minX) > 3.2E7D || Math.abs(maxX) > 3.2E7D || Math.abs(minY) > 3.2E7D || Math.abs(maxY) > 3.2E7D || Math.abs(minZ) > 3.2E7D || Math.abs(maxZ) > 3.2E7D) return true;
		// if (Math.abs(box.a) > 3.2E7D || Math.abs(box.b) > 3.2E7D || Math.abs(box.c) > 3.2E7D || Math.abs(box.d) > 3.2E7D || Math.abs(box.e) > 3.2E7D || Math.abs(box.f) > 3.2E7D) return true;
		else return false;
	}

}
