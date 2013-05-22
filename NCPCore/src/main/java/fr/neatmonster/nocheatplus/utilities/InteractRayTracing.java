package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;

/**
 * Rough ray-tracing for interaction with something.
 * @author mc_dev
 *
 */
public class InteractRayTracing extends RayTracing {
	
	private static final int[][] incr = new int[][]{
		{1, 0, 0},
		{0, 1, 0},
		{0, 0, 1},
		{-1, 0, 0},
		{0, -1, 0},
		{0, 0, -1},
	};
	
	protected BlockCache blockCache = null;
	
	protected boolean collides = false;
	
	protected boolean strict = false;
	
	protected int lastBx, lastBy, lastBz;
	
	protected int targetBx, targetBy, targetBz;
	
	public InteractRayTracing(){
		super();
	}
	
	public InteractRayTracing(boolean strict){
		super();
		this.strict = strict;
	}
	
	public BlockCache getBlockCache() {
		return blockCache;
	}

	public void setBlockCache(BlockCache blockCache) {
		this.blockCache = blockCache;
	}
	
	/* (non-Javadoc)
	 * @see fr.neatmonster.nocheatplus.utilities.RayTracing#set(double, double, double, double, double, double)
	 */
	@Override
	public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
		super.set(x0, y0, z0, x1, y1, z1);
		collides = false;
		lastBx = blockX;
		lastBy = blockY;
		lastBz = blockZ;
		targetBx = Location.locToBlock(x1);
		targetBy = Location.locToBlock(y1);
		targetBz = Location.locToBlock(z1);
	}
	
	public boolean collides(){
		return collides;
	}

	/**
	 * Remove reference to BlockCache.
	 */
	public void cleanup(){
		if (blockCache != null){
			blockCache = null;
		}
	}
	
	/**
	 * Simplistic collision check (can interact through this block).
	 * @param blockX
	 * @param blockY
	 * @param blockZ
	 * @return
	 */
	private final boolean doesCollide(int blockX, int blockY, int blockZ){
		final int id = blockCache.getTypeId(blockX, blockY, blockZ);
		final long flags = BlockProperties.getBlockFlags(id);
		if ((flags & BlockProperties.F_SOLID) == 0){
			// Ignore non solid blocks anyway.
			return false;
		}
		if ((flags & (BlockProperties.F_LIQUID | BlockProperties.F_IGN_PASSABLE | BlockProperties.F_STAIRS | BlockProperties.F_VARIABLE)) != 0){
			// Special cases.
			// TODO: F_VARIABLE: Bounding boxes are roughly right ?
			return false;
		}
		if (!blockCache.isFullBounds(blockX, blockY, blockZ)) return false;
		return true;
	}
	
	/**
	 * Check if the block may be interacted through by use of some workaround.
	 * @param blockX
	 * @param blockY
	 * @param blockZ
	 * @return
	 */
	private final boolean allowsWorkaround(final int blockX, final int blockY, final int blockZ) {
		// TODO: This allows some bypasses for "strange" setups.
		// TODO: Consider using distance to target as heuristic ? [should not get smaller !?]
		final int dX = blockX - lastBx;
		final int dY = blockY - lastBy;
		final int dZ = blockZ - lastBz;
		final double dSq = dX * dX + dY * dY + dZ * dZ;
		for (int i = 0; i < 6; i++){
			final int[] dir = incr[i];
			final int rX = blockX + dir[0];
			if (Math.abs(lastBx - rX) > 1) continue;
			final int rY = blockY + dir[1];
			if (Math.abs(lastBy - rY) > 1) continue;
			final int rZ = blockZ + dir[2];
			if (Math.abs(lastBz - rZ) > 1) continue;
			final int dRx = rX - lastBx;
			final int dRy = rY - lastBy;
			final int dRz = rZ - lastBz;
			if (dRx * dRx + dRy * dRy + dRz * dRz <= dSq) continue;
			if (!doesCollide(rX, rY, rZ)){
				// NOTE: Don't check "rX == targetBx && rZ == targetBz && rY == targetBy".
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected boolean step(int blockX, int blockY, int blockZ, double oX, double oY, double oZ, double dT) {
		// TODO: Make an optional, more precise check (like passable) ?
		if (blockX == targetBx && blockZ == targetBz && blockY == targetBy || !doesCollide(blockX, blockY, blockZ)){
			lastBx = blockX;
			lastBy = blockY;
			lastBz = blockZ;
			return true;
		}
		if (strict || blockX == lastBx && blockZ == lastBz && blockY == lastBy){
			collides = true;
			return false;
		}
		// Check workarounds...
		if (allowsWorkaround(blockX, blockY, blockZ)){
			lastBx = blockX;
			lastBy = blockY;
			lastBz = blockZ;
			return true;
		}
		// No workaround found.
		collides = true;
		return false;
	}

}
