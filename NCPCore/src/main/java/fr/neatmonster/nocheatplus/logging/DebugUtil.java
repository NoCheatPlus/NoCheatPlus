package fr.neatmonster.nocheatplus.logging;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;

/**
 * Some auxiliary static-access methods.
 * @author mc_dev
 *
 */
public class DebugUtil {
	
	public static boolean isSamePos(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2){
		return x1 == x2 && y1 == y2 && z1 == z2;
	}
	
	public static boolean isSamePos(final Location loc1, final Location loc2){
		return isSamePos(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
	}
	
	public static void addLocation(final double x, final double y, final double z, final StringBuilder builder){
		builder.append(x + ", " + y + ", " + z);
	}
	
	public static void addLocation(final Location loc, final StringBuilder builder){
		addLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
	}
	
	public static void addLocation(final PlayerLocation loc, final StringBuilder builder){
		addLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
	}
	
	public static void addFormattedLocation(final double x, final double y, final double z, final StringBuilder builder){
		builder.append(StringUtil.fdec3.format(x) + ", " + StringUtil.fdec3.format(y) + ", " + StringUtil.fdec3.format(z));
	}
	
	public static void addFormattedLocation(final Location loc, final StringBuilder builder){
		addFormattedLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
	}
	
	public static void addFormattedLocation(final PlayerLocation loc, final StringBuilder builder){
		addFormattedLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
	}

	
	/**
	 * With line break between from and to.
	 * @param fromX
	 * @param fromY
	 * @param fromZ
	 * @param toX
	 * @param toY
	 * @param toZ
	 * @param builder
	 */
	public static void addMove(final double fromX, final double fromY, final double fromZ, final double toX, final double toY, final double toZ, final StringBuilder builder){
		builder.append("from: ");
		addLocation(fromX, fromY, fromZ, builder);
		builder.append("\nto: ");
		addLocation(toX, toY, toZ, builder);
	}
	
	/**
	 * No line breaks, max. 3 digits after comma.
	 * @param fromX
	 * @param fromY
	 * @param fromZ
	 * @param toX
	 * @param toY
	 * @param toZ
	 * @param builder
	 */
	public static void addFormattedMove(final double fromX, final double fromY, final double fromZ, final double toX, final double toY, final double toZ, final StringBuilder builder){
		addFormattedLocation(fromX, fromY, fromZ, builder);
		builder.append(" -> ");
		addFormattedLocation(toX, toY, toZ, builder);
	}

	/**
	 * 3 decimal digits after comma (StringUtil.fdec3). No leading new line.
	 * @param from
	 * @param to
	 * @param loc Reference location for from, usually Player.getLocation(). May be null.
	 * @param builder
	 * @return
	 */
	public static void addFormattedMove(final PlayerLocation from, final PlayerLocation to, final Location loc, final StringBuilder builder){
		if (loc != null && !from.isSamePos(loc)){
			builder.append("(");
			addFormattedLocation(loc, builder);
			builder.append(") ");
		}
		addFormattedMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);	
	}
	
	/**
	 * Add exact coordinates, multiple lines. No leading new line.
	 * @param from
	 * @param to
	 * @param loc Reference location for from, usually Player.getLocation().
	 * @param builder
	 */
	public static void addMove(final PlayerLocation from, final PlayerLocation to, final Location loc, final StringBuilder builder){
		if (loc != null && !from.isSamePos(loc)){
			builder.append("Location: ");
			addLocation(loc, builder);
			builder.append("\n");
		}
		addMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);	
	}
	
	/**
	 * 3 decimal digits after comma (StringUtil.fdec3). No leading new line.
	 * @param from
	 * @param to
	 * @param loc Reference location for from, usually Player.getLocation().
	 * @param builder
	 * @return
	 */
	public static void addFormattedMove(final Location from, final Location to, final Location loc, final StringBuilder builder){
		if (loc != null && !isSamePos(from, loc)){
			builder.append("(");
			addFormattedLocation(loc, builder);
			builder.append(") ");
		}
		addFormattedMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);		}
	
	/**
	 * Add exact coordinates, multiple lines. No leading new line.
	 * @param from
	 * @param to
	 * @param loc Reference location for from, usually Player.getLocation().
	 * @param builder
	 */
	public static void addMove(final Location from, final Location to, final Location loc, final StringBuilder builder){
		if (loc != null && !isSamePos(from, loc)){
			builder.append("Location: ");
			addLocation(loc, builder);
			builder.append("\n");
		}
		addMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);
	}

	/**
	 * Output information specific to player-move events.
	 * @param player
	 * @param from
	 * @param to
	 * @param mcAccess
	 */
	public static void outputMoveDebug(final Player player, final PlayerLocation from, final PlayerLocation to, final double maxYOnGround, final MCAccess mcAccess) {
	    	final StringBuilder builder = new StringBuilder(250);
			final Location loc = player.getLocation();
			// TODO: Differentiate debug levels (needs setting up some policy + document in BuildParamteres)?
			if (BuildParameters.debugLevel > 0) {
				builder.append("\n-------------- MOVE --------------\n");
				builder.append(player.getName() + " " + from.getWorld().getName() + ":\n");
				addMove(from, to, loc, builder);
			}
			else {
				builder.append(player.getName() + " " + from.getWorld().getName() + " ");
				addFormattedMove(from, to, loc, builder);
			}
			final double jump = mcAccess.getJumpAmplifier(player);
			final double speed = mcAccess.getFasterMovementAmplifier(player);
			if (BuildParameters.debugLevel > 0){
				try{
					// TODO: Check backwards compatibility (1.4.2). Remove try-catch
					builder.append("\n(walkspeed=" + player.getWalkSpeed() + " flyspeed=" + player.getFlySpeed() + ")");
				} catch (Throwable t){}
				if (player.isSprinting()){
					builder.append("(sprinting)");
				}
				if (player.isSneaking()){
					builder.append("(sneaking)");
				}
			}
			if (speed != Double.NEGATIVE_INFINITY || jump != Double.NEGATIVE_INFINITY){
				builder.append(" (" + (speed != Double.NEGATIVE_INFINITY ? ("e_speed=" + (speed + 1)) : "") + (jump != Double.NEGATIVE_INFINITY ? ("e_jump=" + (jump + 1)) : "") + ")");
			}
			// Print basic info first in order
			System.out.print(builder.toString());
			// Extended info.
			if (BuildParameters.debugLevel > 0){
				builder.setLength(0);
				// Note: the block flags are for normal on-ground checking, not with yOnGrond set to 0.5.
				from.collectBlockFlags(maxYOnGround);
				if (from.getBlockFlags() != 0) builder.append("\nfrom flags: " + StringUtil.join(BlockProperties.getFlagNames(from.getBlockFlags()), "+"));
				if (from.getTypeId() != 0) addBlockInfo(builder, from, "\nfrom");
				if (from.getTypeIdBelow() != 0) addBlockBelowInfo(builder, from, "\nfrom");
				if (!from.isOnGround() && from.isOnGround(0.5)) builder.append(" (ground within 0.5)");
				to.collectBlockFlags(maxYOnGround);
				if (to.getBlockFlags() != 0) builder.append("\nto flags: " + StringUtil.join(BlockProperties.getFlagNames(to.getBlockFlags()), "+"));
				if (to.getTypeId() != 0) addBlockInfo(builder, to, "\nto");
				if (to.getTypeIdBelow() != 0) addBlockBelowInfo(builder, to, "\nto");
				if (!to.isOnGround() && to.isOnGround(0.5)) builder.append(" (ground within 0.5)");
				System.out.print(builder.toString());
			}
			
		}

	public static  void addBlockBelowInfo(final StringBuilder builder, final PlayerLocation loc, final String tag) {
		builder.append(tag + " below id=" + loc.getTypeIdBelow() + " data=" + loc.getData(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()) + " shape=" + Arrays.toString(loc.getBlockCache().getBounds(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ())));
	}

	public static  void addBlockInfo(final StringBuilder builder, final PlayerLocation loc, final String tag) {
		builder.append(tag + " id=" + loc.getTypeId() + " data=" + loc.getData() + " shape=" + Arrays.toString(loc.getBlockCache().getBounds(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())));
	}

	/**
	 * Intended for vehicle-move events.
	 * 
	 * @param player
	 * @param vehicle
	 * @param from
	 * @param to
	 * @param fake true if the event was not fired by an external source (just gets noted).
	 */
	public static void outputDebugVehicleMove(final Player player, final Entity vehicle, final Location from, final Location to, final boolean fake) {
		final StringBuilder builder = new StringBuilder(250);
		final Location vLoc = vehicle.getLocation();
		final Location loc = player.getLocation();
		// TODO: Differentiate debug levels (needs setting up some policy + document in BuildParamteres)?
		final Entity actualVehicle = player.getVehicle();
		final boolean wrongVehicle = actualVehicle == null || actualVehicle.getEntityId() != vehicle.getEntityId();
		if (BuildParameters.debugLevel > 0) {
			builder.append("\n-------------- VEHICLE MOVE " + (fake ? "(fake)" : "") + "--------------\n");
			builder.append(player.getName() + " " + from.getWorld().getName() + ":\n");
			addMove(from, to, null, builder);
			builder.append("\n Vehicle: ");
			addLocation(vLoc, builder);
			builder.append("\n Player: ");
			addLocation(loc, builder);
		}
		else {
			builder.append(player.getName() + " " + from.getWorld().getName() + "veh." + (fake ? "(fake)" : "") + " ");
			addFormattedMove(from, to, null, builder);
			builder.append("\n Vehicle: ");
			addFormattedLocation(vLoc, builder);
			builder.append(" Player: ");
			addFormattedLocation(loc, builder);
		}
		builder.append("\n Vehicle type: " + vehicle.getType() + (wrongVehicle ? (actualVehicle == null ? " (exited?)" : " actual: " + actualVehicle.getType()) : ""));
		System.out.print(builder.toString());
	}

}
