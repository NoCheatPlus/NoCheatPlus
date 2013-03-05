package fr.neatmonster.nocheatplus.logging;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;

/**
 * Some auxiliary specialized static-access methods.
 * @author mc_dev
 *
 */
public class DebugUtil {

	/**
	 * 3 decimal digits after comma (StringUtil.fdec3). No leading new line.
	 * @param from
	 * @param to
	 * @param loc Reference location for from, usually Player.getLocation().
	 * @param builder
	 * @return
	 */
	public static void addFormattedMove(final PlayerLocation from, final PlayerLocation to, final Location loc, final StringBuilder builder){
		builder.append(StringUtil.fdec3.format(from.getX()) + (from.getX() == loc.getX() ? "" : ("(" + StringUtil.fdec3.format(loc.getX()) + ")")));
		builder.append(", " + StringUtil.fdec3.format(from.getY()) + (from.getY() == loc.getY() ? "" : ("(" + StringUtil.fdec3.format(loc.getY()) + ")")));
		builder.append(", " + StringUtil.fdec3.format(from.getZ()) + (from.getZ() == loc.getZ() ? "" : ("(" + StringUtil.fdec3.format(loc.getZ()) + ")")));
		builder.append(" -> " + StringUtil.fdec3.format(to.getX()) + ", " + StringUtil.fdec3.format(to.getY()) + ", " + StringUtil.fdec3.format(to.getZ()));
	}
	
	/**
	 * Add exact coordinates, multiple lines. No leading new line.
	 * @param from
	 * @param to
	 * @param loc Reference location for from, usually Player.getLocation().
	 * @param builder
	 */
	public static void addMove(final PlayerLocation from, final PlayerLocation to, final Location loc, final StringBuilder builder){
		builder.append("from: " + from.getX() + (from.getX() == loc.getX() ? "" : ("(" + loc.getX() + ")")));
		builder.append(", " + from.getY() + (from.getY() == loc.getY() ? "" : ("(" + loc.getY() + ")")));
		builder.append(", " + from.getZ() + (from.getZ() == loc.getZ() ? "" : ("(" + loc.getZ() + ")")));
		builder.append("\nto: " + to.getX() + ", " + to.getY() + ", " + to.getZ());
	}

	/**
	 * 
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
			if (speed != Double.NEGATIVE_INFINITY || jump != Double.NEGATIVE_INFINITY){
				builder.append(" (" + (speed != Double.NEGATIVE_INFINITY ? ("speed=" + speed) : "") + (jump != Double.NEGATIVE_INFINITY ? ("jump=" + jump) : "") + ")");
			}
			
			if (BuildParameters.debugLevel > 0){
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
			}
			System.out.print(builder.toString());
		}

	public static  void addBlockBelowInfo(final StringBuilder builder, final PlayerLocation loc, final String tag) {
		builder.append(tag + " below id=" + loc.getTypeIdBelow() + " data=" + loc.getData(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()) + " shape=" + Arrays.toString(loc.getBlockCache().getBounds(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ())));
	}

	public static  void addBlockInfo(final StringBuilder builder, final PlayerLocation loc, final String tag) {
		builder.append(tag + " id=" + loc.getTypeId() + " data=" + loc.getData() + " shape=" + Arrays.toString(loc.getBlockCache().getBounds(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())));
	}

}
