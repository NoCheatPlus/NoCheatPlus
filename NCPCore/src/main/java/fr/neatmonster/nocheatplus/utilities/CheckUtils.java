package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.checks.fight.FightData;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryData;
import fr.neatmonster.nocheatplus.logging.LogUtil;

/**
 * Random auxiliary gear, some might have general quality. Contents are likely to get moved to other classes.
 */
public class CheckUtils {
	
	/**
	 * Kick and log.
	 * @param player
	 */
	public static void kickIllegalMove(final Player player){
		player.kickPlayer("Illegal move.");
		LogUtil.logWarning("[NCP] Disconnect " + player.getName() + " due to illegal move!");
	}

	/**
	 * Guess some last-action time, likely to be replaced with centralized PlayerData use.
	 * @param player
	 * @param Timestamp of the moment of calling this.
	 * @param maxAge Maximum age in milliseconds.
	 * @return Return timestamp or Long.MIN_VALUE if not possible or beyond maxAge.
	 */
	public static final long guessKeepAliveTime(final Player player, final long now, final long maxAge){
		final int tick = TickTask.getTick();
		long ref = Long.MIN_VALUE;
		// Estimate last fight action time (important for gode modes).
		final FightData fData = FightData.getData(player); 
		ref = Math.max(ref, fData.speedBuckets.lastAccess());
		ref = Math.max(ref, now - 50L * (tick - fData.lastAttackTick)); // Ignore lag.
		// Health regain (not unimportant).
		ref = Math.max(ref, fData.regainHealthTime);
		// Move time.
		ref = Math.max(ref, CombinedData.getData(player).lastMoveTime);
		// Inventory.
		final InventoryData iData = InventoryData.getData(player);
		ref = Math.max(ref, iData.lastClickTime);
		ref = Math.max(ref, iData.instantEatInteract);
		// BlcokBreak/interact.
		final BlockBreakData bbData = BlockBreakData.getData(player);
		ref = Math.max(ref, bbData.frequencyBuckets.lastAccess());
		ref = Math.max(ref, bbData.fastBreakfirstDamage);
		// TODO: More, less ...
		if (ref > now || ref < now - maxAge){
			return Long.MIN_VALUE;
		}
		return ref;
	}

	/**
	 * Check getPassenger recursively until a player is found, return that one or null.
	 * @param entity
	 * @return
	 */
	public static Player getFirstPlayerPassenger(final Entity entity) {
		Entity passenger = entity.getPassenger();
		while (passenger != null){
			if (passenger instanceof Player){
				return (Player) passenger;
			}
			passenger = passenger.getPassenger();
		}
		return null;
	}

	/**
	 * Check recursively for vehicles, returns null if players are vehicles, otherwise the lowest vehicle (that has no vehicle).
	 * @param entity
	 * @return
	 */
	public static Entity getLastNonPlayerVehicle(final Entity entity) {
		Entity vehicle = entity.getVehicle();
		while (vehicle != null){
			if (vehicle instanceof Player){
				return null;
			}
			vehicle = vehicle.getVehicle();
		}
		return vehicle;
	}
}
