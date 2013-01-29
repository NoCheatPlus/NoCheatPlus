package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.logging.LogUtil;

/**
 * Random auxiliary gear, some might have general quality. Contents are likely to get moved to other classes.
 */
public class CheckUtils {
	
    /**
	 * Check if a player looks at a target of a specific size, with a specific
	 * precision value (roughly).
	 * 
	 * @param player
	 *            the player
	 * @param targetX
	 *            the target x
	 * @param targetY
	 *            the target y
	 * @param targetZ
	 *            the target z
	 * @param targetWidth
	 *            the target width
	 * @param targetHeight
	 *            the target height
	 * @param precision
	 *            the precision
	 * @return the double
	 */
	public static double directionCheck(final Player player, final double targetX, final double targetY, final double targetZ, final double targetWidth, final double targetHeight, final double precision)
	{

		// Get the eye location of the player.
		final Location eyes = player.getEyeLocation();

		final double factor = Math.sqrt(Math.pow(eyes.getX() - targetX, 2) + Math.pow(eyes.getY() - targetY, 2) + Math.pow(eyes.getZ() - targetZ, 2));

		// Get the view direction of the player.
		final Vector direction = eyes.getDirection();

		final double x = targetX - eyes.getX();
		final double y = targetY - eyes.getY();
		final double z = targetZ - eyes.getZ();

		final double xPrediction = factor * direction.getX();
		final double yPrediction = factor * direction.getY();
		final double zPrediction = factor * direction.getZ();

		double off = 0.0D;

		off += Math.max(Math.abs(x - xPrediction) - (targetWidth / 2 + precision), 0.0D);
		off += Math.max(Math.abs(z - zPrediction) - (targetWidth / 2 + precision), 0.0D);
		off += Math.max(Math.abs(y - yPrediction) - (targetHeight / 2 + precision), 0.0D);

		if (off > 1) off = Math.sqrt(off);

		return off;
	}

	/**
	 * 3D-distance of two locations. This is obsolete, since it has been fixed. To ignore world checks it might be "useful".
	 * 
	 * @param location1
	 *            the location1
	 * @param location2
	 *            the location2
	 * @return the double
	 */
	public static final double distance(final Location location1, final Location location2)
	{
		return distance(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
	}
	
	/**
	 * 3D-distance.
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return
	 */
	public static final double distance(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
		final double dx = Math.abs(x1 - x2);
		final double dy = Math.abs(y1 - y2);
		final double dz = Math.abs(z1 - z2);
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * 2D-distance in x-z plane.
	 * @param location1
	 * @param location2
	 * @return
	 */
	public static final double xzDistance(final Location location1, final Location location2)
	{
		return distance(location1.getX(), location1.getZ(), location2.getX(), location2.getZ());
	}
	
	/**
	 * 2D-distance.
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 * @return
	 */
	public static final double distance(final double x1, final double z1, final double x2, final double z2) {
		final double dx = Math.abs(x1 - x2);
		final double dz = Math.abs(z1 - z2);
		return Math.sqrt(dx * dx + dz * dz);
	}

	/**
	 * Return if the two Strings are similar based on the given threshold.
	 * 
	 * @param s
	 *            the first String, must not be null
	 * @param t
	 *            the second String, must not be null
	 * @param threshold
	 *            the minimum value of the correlation coefficient
	 * @return result true if the two Strings are similar, false otherwise
	 */
	public static boolean isSimilar(final String s, final String t, final float threshold)
	{
		return 1.0f - (float) levenshteinDistance(s, t) / Math.max(1.0, Math.max(s.length(), t.length())) > threshold;
	}

	/**
	 * Find the Levenshtein distance between two Strings.
	 * 
	 * This is the number of changes needed to change one String into another,
	 * where each change is a single character modification (deletion, insertion or substitution).
	 * 
	 * @param s
	 *            the first String, must not be null
	 * @param t
	 *            the second String, must not be null
	 * @return result distance
	 */
	public static int levenshteinDistance(CharSequence s, CharSequence t) {
		if (s == null || t == null) throw new IllegalArgumentException("Strings must not be null");

		int n = s.length();
		int m = t.length();

		if (n == 0) return m;
		else if (m == 0) return n;

		if (n > m) {
			final CharSequence tmp = s;
			s = t;
			t = tmp;
			n = m;
			m = t.length();
		}

		int p[] = new int[n + 1];
		int d[] = new int[n + 1];
		int _d[];

		int i;
		int j;

		char t_j;

		int cost;

		for (i = 0; i <= n; i++)
			p[i] = i;

		for (j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			_d = p;
			p = d;
			d = _d;
		}

		return p[n];
	}
	
	/**
	 * Angle of a 2d vector, x being the side at the angle. (radians).
	 * @param x
	 * @param z
	 * @return
	 */
	public static final double angle(final double x, final double z){
		final double a;
		if (x > 0.0) a = Math.atan(z / x);
		else if  (x < 0.0) a = Math.atan(z / x) + Math.PI;
		else{
	        if (z < 0.0) a=3.0 * Math.PI / 2.0;
	        else if (z > 0.0) a = Math.PI / 2.0;
	        else return Double.NaN;
		}
	    if (a < 0.0) return a + 2.0 * Math.PI;
	    else return a;
	}
	
	/**
	 * Get the difference of angles (radians) as given from angle(x,z), from a1 to a2, i.e. rather a2 - a1 in principle.
	 * @param a1
	 * @param a2
	 * @return Difference of angle from -pi to pi
	 */
	public static final double angleDiff(final double a1, final double a2){
		if (Double.isNaN(a1) || Double.isNaN(a1)) return Double.NaN;
		final double diff = a2 - a1;
		if (diff < -Math.PI) return diff + 2.0 * Math.PI;
		else if (diff > Math.PI) return diff - 2.0 * Math.PI;
		else return diff;
	}
	
	/**
	 * Yaw (angle in grad) difference. This ensures inputs are interpreted correctly (for 360 degree offsets).
	 * @param fromYaw
	 * @param toYaw
	 * @return Angle difference to get from fromYaw to toYaw. Result is in [-180, 180].
	 */
	public static final float yawDiff(float fromYaw, float toYaw){
		if (fromYaw <= -360f) fromYaw = -((-fromYaw) % 360f);
		else if (fromYaw >= 360f) fromYaw = fromYaw % 360f;
		if (toYaw <= -360f) toYaw = -((-toYaw) % 360f);
		else if (toYaw >= 360f) toYaw = toYaw % 360f;
		float yawDiff = toYaw - fromYaw;
		if (yawDiff < -180f) yawDiff += 360f;
		else if (yawDiff > 180f) yawDiff -= 360f;
		return yawDiff;
	}
	
	public static void onIllegalMove(final Player player){
		player.kickPlayer("Illegal move.");
		LogUtil.logWarning("[NCP] Disconnect " + player.getName() + " due to illegal move!");
	}

	/**
	 * Teleport the player with vehicle, temporarily eject the passenger and set teleported in MovingData.
	 * @param vehicle
	 * @param player
	 * @param location
	 */
	public static void teleport(final Vehicle vehicle, final Player player, final Location location) {
		// TODO: This handling could conflict with WorldGuard region flags.
		final Entity passenger = vehicle.getPassenger();
		final boolean vehicleTeleported;
		final boolean playerIsPassenger = player.equals(passenger);
		if (playerIsPassenger && !vehicle.isDead()){ // && vehicle.equals(player.getVehicle).
			vehicle.eject();
			vehicleTeleported = vehicle.teleport(location, TeleportCause.PLUGIN);
			
		}
		else if (passenger == null && !vehicle.isDead()){
			vehicleTeleported = vehicle.teleport(location, TeleportCause.PLUGIN);
		}
		else vehicleTeleported = false;
		final MovingData data = MovingData.getData(player);
		data.setTeleported(location);
		final boolean playerTeleported = player.teleport(location);
		if (playerIsPassenger && playerTeleported && vehicleTeleported && player.getLocation().distance(vehicle.getLocation()) < 1.0){
			// Somewhat check against tp showing something wrong (< 1.0).
			vehicle.setPassenger(player);
		}
		if (MovingConfig.getConfig(player).debug){
			System.out.println(player.getName() + " vehicle set back: " + location);
		}
	}
}
