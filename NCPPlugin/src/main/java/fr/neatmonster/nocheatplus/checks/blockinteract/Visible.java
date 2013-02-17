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
import fr.neatmonster.nocheatplus.utilities.InteractRayTracing;

public class Visible extends Check {
	
	private BlockCache blockCache;
	
	private final InteractRayTracing rayTracing = new InteractRayTracing(false);

	public Visible() {
		super(CheckType.BLOCKINTERACT_VISIBLE);
		blockCache = mcAccess.getBlockCache(null);
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

	public boolean check(final Player player, final Location loc, final Block block, final BlockFace face, final Action action, final BlockInteractData data, final BlockInteractConfig cc) {
		
		// TODO: Might confine what to check for (left/right, target blocks depending on item in hand, container blocks).
		
		blockCache.setAccess(loc.getWorld());
		rayTracing.setBlockCache(blockCache);
		rayTracing.set(loc.getX(), loc.getY() + player.getEyeHeight(), loc.getZ(), 0.5 + block.getX() + 0.6 * face.getModX(), 0.5 + block.getY() + 0.6 * face.getModY(), 0.5 + block.getZ() + 0.6 * face.getModZ());
		rayTracing.loop();
		final boolean collides = rayTracing.collides();
    	blockCache.cleanup();
    	rayTracing.cleanup();
    	
		if (cc.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
        	player.sendMessage("Interact: " + (action == Action.RIGHT_CLICK_BLOCK ? "right" : "left") + " collide=" + rayTracing.collides());
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
