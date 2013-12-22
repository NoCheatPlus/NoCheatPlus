package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PassableRayTracing;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

public class Passable extends Check {
	
	private final PassableRayTracing rayTracing = new PassableRayTracing();

	public Passable() {
		super(CheckType.MOVING_PASSABLE);
		rayTracing.setMaxSteps(60); // TODO: Configurable ?
	}

	public Location check(final Player player, Location loc, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc)
	{
		// TODO: if (!from.isSameCoords(loc)) {...check passable for loc -> from !?... + sf etc too?}
		// TODO: Future: Account for the players bounding box? [test very-strict setting for at least the end points...]
		String tags = "";
		// Block distances (sum, max) for from-to (not for loc!).
		final int manhattan = from.manhattan(to);
		// Skip moves inside of ignored blocks right away.
		if (manhattan == 0 && (BlockProperties.getBlockFlags(from.getTypeId()) & BlockProperties.F_IGN_PASSABLE) != 0) {
			return null;
		}
		boolean toPassable = to.isPassable();
		// General condition check for using ray-tracing.
		if (toPassable && cc.passableRayTracingCheck && (!cc.passableRayTracingVclipOnly || from.getY() != to.getY()) && (!cc.passableRayTracingBlockChangeOnly || manhattan > 0)) {
			rayTracing.set(from, to);
			rayTracing.loop();
			if (rayTracing.collides() || rayTracing.getStepsDone() >= rayTracing.getMaxSteps()) {
				final int maxBlockDist = manhattan <= 1 ? manhattan : from.maxBlockDist(to);
				if (maxBlockDist <= 1 && rayTracing.getStepsDone() == 1 && !from.isPassable()) {
					// Redo ray-tracing for moving out of blocks.
					if (collidesIgnoreFirst(from, to)) {
						toPassable = false;
						tags = "raytracing_2x_";
					}
					else if (cc.debug) {
						System.out.println(player.getName() + " passable: allow moving out of a block.");
					}
				}
				else{
					if (!allowsSplitMove(from, to, manhattan)) {
						toPassable = false;
						tags = "raytracing_";
					}
				}
			}
			// TODO: Future: If accuracy is demanded, also check the head position (or bounding box right away).
			rayTracing.cleanup();
		}
		
		// TODO: Checking order: If loc is not the same as from, a quick return here might not be wanted.
		if (toPassable) {
			// Quick return.
			// (Might consider if vl>=1: only decrease if from and loc are passable too, though micro...)
			data.passableVL *= 0.99;
			return null;
		} else {
			return potentialViolation(player, loc, from, to, manhattan, tags, data, cc);
		}
		
	}
	
	private Location potentialViolation(final Player player, Location loc, final PlayerLocation from, final PlayerLocation to, final int manhattan, String tags, final MovingData data, final MovingConfig cc) {
		// Moving into a block, possibly a violation.

		// Check the players location if different from others.
		// (It provides a better set-back for some exploits.)
		final int lbX = loc.getBlockX();
		final int lbY = loc.getBlockY();
		final int lbZ = loc.getBlockZ();
		// First check if the player is moving from a passable location.
		// If not, the move might still be allowed, if moving inside of the same block, or from and to have head position passable.
		if (from.isPassable()) {
			// Put one workaround for 1.5 high blocks here:
			if (from.isBlockAbove(to) && (BlockProperties.getBlockFlags(to.getTypeId()) & BlockProperties.F_HEIGHT150) != 0) {
				// Check if the move went from inside of the block.
				if (BlockProperties.collidesBlock(to.getBlockCache(), from.getX(), from.getY(), from.getZ(), from.getX(), from.getY(), from.getZ(), to.getBlockX(), to.getBlockY(), to.getBlockZ(), to.getTypeId())) {
					// Allow moving inside of 1.5 high blocks.
					return null;
				}
			}
			// From should be the set-back.
			loc = null;
			tags += "into";
		} else if (BlockProperties.isPassable(from.getBlockCache(), loc.getX(), loc.getY(), loc.getZ(), from.getTypeId(lbX, lbY, lbZ))) {
			tags += "into_shift";
		}
//				} else if (BlockProperties.isPassableExact(from.getBlockCache(), loc.getX(), loc.getY(), loc.getZ(), from.getTypeId(lbX, lbY, lbZ))) {
			// (Mind that this can be the case on the same block theoretically.)
			// Keep loc as set-back.
//				}
		else if (!from.isSameBlock(lbX, lbY, lbZ)) {
			// Otherwise keep loc as set-back.
			tags += "cross_shift";
		}
		else if (manhattan == 1 && to.isBlockAbove(from) && BlockProperties.isPassable(from.getBlockCache(), from.getX(), from.getY() + player.getEyeHeight(), from.getZ(), from.getTypeId(from.getBlockX(), Location.locToBlock(from.getY() + player.getEyeHeight()), from.getBlockZ()))) {
//				else if (to.isBlockAbove(from) && BlockProperties.isPassableExact(from.getBlockCache(), from.getX(), from.getY() + player.getEyeHeight(), from.getZ(), from.getTypeId(from.getBlockX(), Location.locToBlock(from.getY() + player.getEyeHeight()), from.getBlockZ()))) {
			// Allow the move up if the head is free.
			return null;
		}
		else if (manhattan > 0) {
			// Otherwise keep from as set-back.
			loc = null;
			tags += "cross";
		}
		else{
			// All blocks are the same, allow the move.
			return null;
		}
		
		// Discard inconsistent locations.
		// TODO: Might get rid of using the in-between loc - needs use-case checking.
		if (loc != null && (TrigUtil.distance(from,  to) > 0.75 || TrigUtil.distance(from, loc) > 0.125)) {
			loc = null;
		}
		
		// Prefer the set-back location from the data.
		if (data.hasSetBack()) {
			final Location ref = data.getSetBack(to);
			if (BlockProperties.isPassable(from.getBlockCache(), ref) || loc == null || TrigUtil.distance(from, loc) > 0.13) {
//					if (BlockProperties.isPassableExact(from.getBlockCache(), ref)) {
				loc = ref;
				if (cc.debug) {
					System.out.println(player.getName() + " Using set-back location for passable.");
				}
			} else if (cc.debug) {
				System.out.println(player.getName() + " Ignorng set-back for passable.");
			}
		}

		// TODO: set data.set-back ? or something: still some aji here.
		
		// Return the reset position.
		data.passableVL += 1d;
		final ViolationData vd = new ViolationData(this, player, data.passableVL, 1, cc.passableActions);
		if (cc.debug || vd.needsParameters()) {
			vd.setParameter(ParameterName.BLOCK_ID, "" + to.getTypeId());
			if (!tags.isEmpty()) {
				vd.setParameter(ParameterName.TAGS, tags);
			}
		}
		if (executeActions(vd)) {
			// TODO: Consider another set back position for this, also keeping track of players moving around in blocks.
			final Location newTo;
			if (loc != null) {
				newTo = loc;
			} else {
				newTo = from.getLocation();
				if (cc.debug) {
					System.out.println(player.getName() + " Using from location for passable.");
				}
			}
			newTo.setYaw(to.getYaw());
			newTo.setPitch(to.getPitch());
			return newTo;
		}
		else{
			// No cancel action set.
			return null;
		}
	}
	
	/**
	 * Test collision with ignoring the first block.
	 * @param from
	 * @param to
	 * @return
	 */
	private boolean collidesIgnoreFirst(PlayerLocation from, PlayerLocation to) {
		rayTracing.set(from, to);
		rayTracing.setIgnorefirst();
		rayTracing.loop();
		return rayTracing.collides() || rayTracing.getStepsDone() >= rayTracing.getMaxSteps();
	}

	/**
	 * Test the move split into y-move and horizontal move, provided some pre-conditions are met.
	 * @param from
	 * @param to
	 * @param manhattan
	 * @return
	 */
	private boolean allowsSplitMove(final PlayerLocation from, final PlayerLocation to, int manhattan) {
		final double yDiff = to.getY() - from.getY() ;
		if (manhattan <= 3 && yDiff > 0.0 && Math.abs(yDiff)  < 1.0) {
			// Workaround for client-side calculations not being possible (y vs. horizontal move).
			// TODO: Alternative: Test if "real" ray-tracing would fix it (might not!).
			if (yDiff > 0.0) {
				// y first.
				rayTracing.set(from.getX(), from.getY(), from.getZ(), from.getX(), to.getY(), from.getZ());
				rayTracing.loop();
				if (!rayTracing.collides()) {
					// horizontal second.
					rayTracing.set(from.getX(), to.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
					rayTracing.loop();
					if (!rayTracing.collides()) {
						return true;
					}
				}
			} else {
				// horizontal first.
				rayTracing.set(from.getX(), from.getY(), from.getZ(), to.getX(), from.getY(), to.getZ());
				rayTracing.loop();
				if (!rayTracing.collides()) {
					// y second.
					rayTracing.set(to.getX(), from.getY(), to.getZ(), to.getX(), to.getY(), to.getZ());
					rayTracing.loop();
					if (!rayTracing.collides()) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
