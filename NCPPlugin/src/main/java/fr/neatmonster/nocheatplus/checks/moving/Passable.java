package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PassableRayTracing;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

public class Passable extends Check {
	
	private final PassableRayTracing rayTracing = new PassableRayTracing();

	public Passable() {
		super(CheckType.MOVING_PASSABLE);
		rayTracing.setMaxSteps(60); // TODO: Configurable ?
	}

	public Location check(final Player player, Location loc, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc)
	{
		// Simple check (only from, to, player.getLocation).
		
		// TODO: account for actual bounding box.
		
		boolean toPassable = to.isPassable();
		// TODO: Config settings, extra flag for further processing.
		if (toPassable && cc.passableRayTracingCheck && (!cc.passableRayTracingVclipOnly || from.getY() > to.getY()) && (!cc.passableRayTracingBlockChangeOnly || from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())){
			rayTracing.set(from, to);
			rayTracing.loop();
			if (rayTracing.collides() || rayTracing.getStepsDone() >= rayTracing.getMaxSteps()){
				toPassable = false;
			}
			// TODO: If accuracy is set, also check the head position (or bounding box right away).
			rayTracing.cleanup();
		}
		
		if (toPassable){
			// Quick return.
			// (Might consider if vl>=1: only decrease if from and loc are passable too, though micro...)
			data.passableVL *= 0.99;
			return null;
		}
		
		// Moving into a block, possibly a violation.

		// Check the players location if different from others.
		// (It provides a better set-back for some exploits.)
//		Location loc = player.getLocation();
		final int lbX = loc.getBlockX();
		final int lbY = loc.getBlockY();
		final int lbZ = loc.getBlockZ();
		// First check if the player is moving from a passable location.
		// If not, the move might still be allowed, if moving inside of the same block, or from and to have head position passable.
		if (from.isPassable()){
			// From should be the set-back.
			loc = null;
		} else if (BlockProperties.isPassable(from.getBlockCache(), loc.getX(), loc.getY(), loc.getZ(), from.getTypeId(lbX, lbY, lbZ))){
//		} else if (BlockProperties.isPassableExact(from.getBlockCache(), loc.getX(), loc.getY(), loc.getZ(), from.getTypeId(lbX, lbY, lbZ))){
			// (Mind that this can be the case on the same block theoretically.)
			// Keep loc as set-back.
		}
		else if (!from.isSameBlock(lbX, lbY, lbZ)){
			// Otherwise keep loc as set-back.
		}
		else if (to.isBlockAbove(from) && BlockProperties.isPassable(from.getBlockCache(), from.getX(), from.getY() + player.getEyeHeight(), from.getZ(), from.getTypeId(from.getBlockX(), Location.locToBlock(from.getY() + player.getEyeHeight()), from.getBlockZ()))){
//		else if (to.isBlockAbove(from) && BlockProperties.isPassableExact(from.getBlockCache(), from.getX(), from.getY() + player.getEyeHeight(), from.getZ(), from.getTypeId(from.getBlockX(), Location.locToBlock(from.getY() + player.getEyeHeight()), from.getBlockZ()))){
			// Allow the move up if the head is free.
			return null;
		}
		else if (!from.isSameBlock(to)){
			// Otherwise keep from as set-back.
			loc = null;
		}
		else{
			// All blocks are the same, allow the move.
			return null;
		}
		
		// Prefer the set-back location from the data.
		if (data.hasSetBack()){
			final Location ref = data.getSetBack(to);
			if (BlockProperties.isPassable(from.getBlockCache(), ref)){
//			if (BlockProperties.isPassableExact(from.getBlockCache(), ref)){
				loc = ref;
			}
		}

		// TODO: set data.set-back ? or something: still some aji here.
		
		// Return the reset position.
		data.passableVL += 1d;
		final ViolationData vd = new ViolationData(this, player, data.passableVL, 1, cc.passableActions);
		if (cc.debug || vd.needsParameters()) vd.setParameter(ParameterName.BLOCK_ID, "" + to.getTypeId());
		if (executeActions(vd)) {
			// TODO: Consider another set back position for this, also keeping track of players moving around in blocks.
			final Location newTo;
			if (loc != null) newTo = loc;
			else newTo = from.getLocation();
			newTo.setYaw(to.getYaw());
			newTo.setPitch(to.getPitch());
			return newTo;
		}
		else{
			// No cancel action set.
			return null;
		}
	}

	@Override
	protected Map<ParameterName, String> getParameterMap(final ViolationData violationData)
	{
		return super.getParameterMap(violationData);
	}

}
