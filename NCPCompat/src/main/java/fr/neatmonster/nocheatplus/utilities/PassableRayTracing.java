package fr.neatmonster.nocheatplus.utilities;

public class PassableRayTracing extends RayTracing{

	protected BlockCache blockCache = null;
	
	protected boolean collides = false;
	
	/**
	 * Empty constructor for setting other properties later.
	 */
	public PassableRayTracing() {
		super(0, 0, 0, 0, 0, 0);
	}
	

	public BlockCache getBlockCache() {
		return blockCache;
	}


	public void setBlockCache(BlockCache blockCache) {
		this.blockCache = blockCache;
	}
	
	/**
	 * Set from PlayerLocation instances. Currently takes BlockCache from the from-location.
	 * @param from
	 * @param to
	 */
	public void set(final PlayerLocation from, final PlayerLocation to){
		set(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
		setBlockCache(from.getBlockCache()); // TODO: This might better be done extra.
	}
	
	/* (non-Javadoc)
	 * @see fr.neatmonster.nocheatplus.utilities.RayTracing#set(double, double, double, double, double, double)
	 */
	@Override
	public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
		super.set(x0, y0, z0, x1, y1, z1);
		collides = false;
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
	
	@Override
	protected boolean step(final int blockX, final int blockY, final int blockZ, final double oX, final double oY, final double oZ, final double dT) {
		final int id = blockCache.getTypeId(blockX, blockY, blockZ);
		if (BlockProperties.isPassable(id)) return true;
		double[] bounds = blockCache.getBounds(blockX, blockY, blockZ);
		if (bounds == null) return true;
		
		// TODO: Other problem (forgot)...
		
		// Check if is already inside.
		// TODO: This might be superfluous since below method used.
		if (oX >= bounds[0] && oX < bounds[3] && oY >= bounds[1] && oY < bounds[4] && oZ >= bounds[2] && oZ < bounds[5]){
			if (!BlockProperties.isPassableWorkaround(blockCache, blockX, blockY, blockZ, oX, oY, oZ, id, 0, 0, 0, 0)){
				collides = true;
				return true;
			}
		}
		// Check extrapolation [all three intervals must be hit].
		if (dX < 0){
			if (oX < bounds[0]) return true;
			else if (oX + dX * dT >= bounds[3]) return true;
		}
		else{
			if (oX >= bounds[3]) return true;
			else if (oX + dX * dT < bounds[0]) return true;
		}
		if (dY < 0){
			if (oY < bounds[1]) return true;
			else if (oY + dY * dT >= bounds[4]) return true;
		}
		else{
			if (oY >= bounds[4]) return true;
			else if (oY + dY * dT < bounds[1]) return true;
		}
		if (dZ < 0){
			if (oZ < bounds[2]) return true;
			else if (oZ + dZ * dT >= bounds[5]) return true;
		}
		else{
			if (oZ >= bounds[5]) return true;
			else if (oZ + dZ * dT < bounds[2]) return true;
		}
		// Check for workarounds.
		// TODO: check f_itchy once exists.
		if (BlockProperties.isPassableWorkaround(blockCache, blockX, blockY, blockZ, oX, oY, oZ, id, dX, dY, dZ, dT)){
			return true;
		}
		// Does collide (most likely).
		// TODO: This is not entirely accurate, needs further exclusion for smaller solid blocks.
		// (Could allow start-end if passable + check first collision time or some estimate.)
		collides = true;
		return true;
	}

}
