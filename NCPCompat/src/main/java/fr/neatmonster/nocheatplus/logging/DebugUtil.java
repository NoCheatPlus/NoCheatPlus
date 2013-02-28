package fr.neatmonster.nocheatplus.logging;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.MCAccess;
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
	 * 
	 * @param player
	 * @param from
	 * @param to
	 * @param mcAccess
	 */
	public static void outputMoveDebug(final Player player, final PlayerLocation from, final PlayerLocation to, final MCAccess mcAccess) {
	    	final StringBuilder builder = new StringBuilder(250);
			final Location loc = player.getLocation();
			builder.append(player.getName());
			builder.append(" " + from.getWorld().getName() + " " + StringUtil.fdec3.format(from.getX()) + (from.getX() == loc.getX() ? "" : ("(" + StringUtil.fdec3.format(loc.getX()) + ")")));
			builder.append(", " + StringUtil.fdec3.format(from.getY()) + (from.getY() == loc.getY() ? "" : ("(" + StringUtil.fdec3.format(loc.getY()) + ")")));
			builder.append(", " + StringUtil.fdec3.format(from.getZ()) + (from.getZ() == loc.getZ() ? "" : ("(" + StringUtil.fdec3.format(loc.getZ()) + ")")));
			builder.append(" -> " + StringUtil.fdec3.format(to.getX()) + ", " + StringUtil.fdec3.format(to.getY()) + ", " + StringUtil.fdec3.format(to.getZ()));
			final double jump = mcAccess.getJumpAmplifier(player);
			final double speed = mcAccess.getFasterMovementAmplifier(player);
			if (speed != Double.NEGATIVE_INFINITY || jump != Double.NEGATIVE_INFINITY){
				builder.append(" (" + (speed != Double.NEGATIVE_INFINITY ? ("speed=" + speed) : "") + (jump != Double.NEGATIVE_INFINITY ? ("jump=" + jump) : "") + ")");
			}
			
			if (BuildParameters.debugLevel > 0){
				if (from.getTypeId() != 0) addBlockInfo(builder, from, "\nfrom");
				if (from.getTypeIdBelow() != 0) addBlockBelowInfo(builder, from, "\nfrom");
				if (!from.isOnGround() && from.isOnGround(0.5)) builder.append(" (ground within 0.5)");
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
