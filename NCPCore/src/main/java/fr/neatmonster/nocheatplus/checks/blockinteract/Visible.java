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
	
	/**
	 * Factor for the block-face mod.
	 */
	private static final double fModFull = 0.6;
	
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
			return 0.5 + fModFull * mod;
		}
		if (mod == 0){
			// TODO: Middle or middle of block ?
			return (bounds[index] + bounds[index + 3]) / 2.0;
		}
		else if (mod == 1){
			// TODO: Slightly outside or dependent on exact position (inside, exact edge, outside)?
			return bounds[index + 3];
		}
		else if (mod == -1){
			// TODO: Slightly outside or dependent on exact position (inside, exact edge, outside)?
			return bounds[index];
		}
		else{
			throw new IllegalArgumentException("BlockFace.getModX|Y|Z must be 0, 1 or -1.");
		}
	}

	public boolean check(final Player player, final Location loc, final Block block, final BlockFace face, final Action action, final BlockInteractData data, final BlockInteractConfig cc) {
		
		// TODO: Might confine what to check for (left/right, target blocks depending on item in hand, container blocks).
		final boolean collides;
		final double eyeHeight = player.getEyeHeight();
		if (block.getX() == loc.getBlockX() && block.getZ() == loc.getBlockZ() && block.getY() == Location.locToBlock(loc.getY() + eyeHeight)){
			// Player is interacting with the block his head is in.
			collides = false;
		}
		else{
			blockCache.setAccess(loc.getWorld());
			
			// Guess some end-coordinates.
			@SuppressWarnings("deprecation")
			final double[] bounds = BlockProperties.getCorrectedBounds(blockCache, block.getX(), block.getY(), block.getZ());
			final int modX = face.getModX();
			final int modY = face.getModY();
			final int modZ = face.getModZ();
			final double eX = (double) block.getX() + getEnd(bounds, 0, modX);
			final double eY = (double) block.getY() + getEnd(bounds, 1, modY);
			final double eZ = (double) block.getZ() + getEnd(bounds, 2, modZ);
			
			if (BlockProperties.isPassable(blockCache, eX, eY, eZ, blockCache.getTypeId(eX, eY, eZ))){
				// Perform ray-tracing.
				rayTracing.setBlockCache(blockCache);
				rayTracing.set(loc.getX(), loc.getY() + eyeHeight, loc.getZ(), eX, eY, eZ);
				rayTracing.loop();
				collides = rayTracing.collides() || rayTracing.getStepsDone() >= rayTracing.getMaxSteps();
		    	rayTracing.cleanup();
			}
			else{
				// Not passable = not possible.
				collides = true;
			}
	    	blockCache.cleanup();
		}
    	
		if (cc.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
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

}
