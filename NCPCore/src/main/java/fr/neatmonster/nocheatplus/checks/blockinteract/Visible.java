package fr.neatmonster.nocheatplus.checks.blockinteract;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.InteractRayTracing;

public class Visible extends Check {
	
	/** Offset from bounds to estimate some reference end position for ray-tracing. */
	private static final double offset = 0.0001;
	
	private BlockCache blockCache;
	
	private final InteractRayTracing rayTracing = new InteractRayTracing(false);

	public Visible() {
		super(CheckType.BLOCKINTERACT_VISIBLE);
		blockCache = mcAccess.getBlockCache(null);
		rayTracing.setMaxSteps(60); // TODO: Configurable ?
	}
	
	/* (non-Javadoc)
	 * @see fr.neatmonster.nocheatplus.checks.Check#setMCAccess(fr.neatmonster.nocheatplus.compat.MCAccess)
	 */
	@Override
	public void setMCAccess(MCAccess mcAccess) {
		super.setMCAccess(mcAccess);
		// Renew the BlockCache instance.
		blockCache = mcAccess.getBlockCache(null);
	}
	
	private static final double getEnd(final double[] bounds, final int index, final int mod){
		if (bounds == null){
			return 0.5 + 0.5 * mod + offset;
		}
		if (mod == 0){
			// Middle.
			return (bounds[index] + bounds[index + 3]) / 2.0;
		}
		else if (mod == 1){
			// TODO: Slightly outside or dependent on exact position (inside, exact edge, outside)?
			return Math.min(1.0, bounds[index + 3]) + offset;
		}
		else if (mod == -1){
			// TODO: Slightly outside or dependent on exact position (inside, exact edge, outside)?
			return Math.max(0.0, bounds[index]) - offset;
		}
		else{
			throw new IllegalArgumentException("BlockFace.getModX|Y|Z must be 0, 1 or -1.");
		}
	}

	public boolean check(final Player player, final Location loc, final Block block, final BlockFace face, final Action action, final BlockInteractData data, final BlockInteractConfig cc) {
		// TODO: This check might make parts of interact/blockbreak/... + direction (+?) obsolete.
		// TODO: Might confine what to check for (left/right-click, target blocks depending on item in hand, container blocks).
		final boolean collides;
		final int blockX = block.getX();
		final int blockY = block.getY();
		final int blockZ = block.getZ();
		final double eyeX = loc.getX();
		final double eyeY = loc.getY() + player.getEyeHeight();
		final double eyeZ = loc.getZ();
		
		// TODO: Add tags for fail_passable, fail_raytracing, (fail_face).
		// TODO: Reachable face check ?
		
		if (blockX == Location.locToBlock(eyeX) && blockZ == Location.locToBlock(eyeZ) && block.getY() == Location.locToBlock(eyeY)){
			// Player is interacting with the block his head is in.
			// TODO: Should the reachable-face-check be done here too (if it is added at all)?
			collides = false;
		}
		else{
			// Initialize.
			blockCache.setAccess(loc.getWorld());
			rayTracing.setBlockCache(blockCache);
			
			collides = checkRayTracing(eyeX, eyeY, eyeZ, blockX, blockY, blockZ, face);
			
			// Cleanup.
			rayTracing.cleanup();
	    	blockCache.cleanup();
		}
    	
		if (cc.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
			// TODO: Tags
        	player.sendMessage("Interact visible: " + (action == Action.RIGHT_CLICK_BLOCK ? "right" : "left") + " collide=" + rayTracing.collides());
        }
		
		// Actions ?
		boolean cancel = false;
		if (collides){
			data.visibleVL += 1;
			if (executeActions(player, data.visibleVL, 1, cc.visibleActions)){
				cancel = true;
			}
		}
		else{
			data.visibleVL *= 0.99;
		}
		
		return cancel;
	}
	
	private boolean checkRayTracing(final double eyeX, final double eyeY, final double eyeZ, final int blockX, final int blockY, final int blockZ, final BlockFace face){
		
		// Estimated target-middle-position (does it for most cases).
		@SuppressWarnings("deprecation")
		final double[] bounds = BlockProperties.getCorrectedBounds(blockCache, blockX, blockY, blockZ);
		final int modX = face.getModX();
		final int modY = face.getModY();
		final int modZ = face.getModZ();
		final double estX = (double) blockX + getEnd(bounds, 0, modX);
		final double estY = (double) blockY + getEnd(bounds, 1, modY);
	    final double estZ = (double) blockZ + getEnd(bounds, 2, modZ);
	    final int bEstX = Location.locToBlock(estX);
	    final int bEstY = Location.locToBlock(estY);
	    final int bEstZ = Location.locToBlock(estZ);
	    final int estId = blockCache.getTypeId(bEstX, bEstY, bEstZ);
	    
		// Ignore passable if the estimate is on the clicked block.
		final boolean skipPassable = blockX == bEstX && blockY == bEstY && blockZ == bEstZ;
		
		// TODO: Might also use looking direction (test how accurate).
		return checkCollision(eyeX, eyeY, eyeZ, estX, estY, estZ, estId, bounds, modX, modY, modZ, skipPassable);
		
	}

	/**
	 * Recursively check alternate positions.
	 * @param eyeX
	 * @param eyeY
	 * @param eyeZ
	 * @param estX
	 * @param estY
	 * @param estZ
	 * @param estId
	 * @param modX
	 * @param modY
	 * @param modZ
	 * @param skipPassable
	 * @return
	 */
	private boolean checkCollision(final double eyeX, final double eyeY, final double eyeZ, final double estX, final double estY, final double estZ, final int estId, final double[] bounds, final int modX, final int modY, final int modZ, final boolean skipPassable) {
		// Check current position.
		if (skipPassable || BlockProperties.isPassable(blockCache, estX, estY, estZ, estId)){
			// Perform ray-tracing.
			rayTracing.set(eyeX, eyeY, eyeZ, estX, estY, estZ);
			rayTracing.loop();
			if (!rayTracing.collides() && rayTracing.getStepsDone() < rayTracing.getMaxSteps()){
				return false;
			}
		}
		// Note: Center of bounds is used for mod == 0.
		// TODO: Could "sort" positions by setting signum of d by which is closer to the player.
		// TODO: Could consider slightly in-set positions.
		if (modX == 0){
			// TODO: Might ensure to check if it is the same block?
			final double d = (bounds[3] - bounds[0]) / 2.0;
			if (d >= 0.05){
				// Recursion with adapted x position (if differs enough from bounds.
				if (!checkCollision(eyeX, eyeY, eyeZ, estX - d, estY, estZ, estId, bounds, 1, modY, modZ, skipPassable)){
					return false;
				}
				if (!checkCollision(eyeX, eyeY, eyeZ, estX + d, estY, estZ, estId, bounds, 1, modY, modZ, skipPassable)){
					return false;
				}
			}
		}
		if (modZ == 0){
			// TODO: Might ensure to check if it is the same block?
			final double d = (bounds[5] - bounds[2]) / 2.0;
			if (d >= 0.05){
				// Recursion with adapted x position (if differs enough from bounds.
				if (!checkCollision(eyeX, eyeY, eyeZ, estX, estY, estZ - d, estId, bounds, 1, modY, 1, skipPassable)){
					return false;
				}
				if (!checkCollision(eyeX, eyeY, eyeZ, estX, estY, estZ + d, estId, bounds, 1, modY, 1, skipPassable)){
					return false;
				}
			}
		}
		if (modY == 0){
			// TODO: Might ensure to check if it is the same block?
			final double d = (bounds[4] - bounds[1]) / 2.0;
			if (d >= 0.05){
				// Recursion with adapted x position (if differs enough from bounds.
				if (!checkCollision(eyeX, eyeY, eyeZ, estX, estY - d, estZ, estId, bounds, 1, 1, 1, skipPassable)){
					return false;
				}
				if (!checkCollision(eyeX, eyeY, eyeZ, estX, estY + d, estZ, estId, bounds, 1, 1, 1, skipPassable)){
					return false;
				}
			}
		}
		
		return true;
	}

}
