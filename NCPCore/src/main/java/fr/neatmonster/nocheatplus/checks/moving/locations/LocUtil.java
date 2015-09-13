package fr.neatmonster.nocheatplus.checks.moving.locations;

import org.bukkit.Location;
import org.bukkit.World;

import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/**
 * Auxiliary methods for Location handling, mainly intended for use with set-back locations.
 * @author mc_dev
 *
 */
public class LocUtil {

	/**
	 * Get a copy of a location (not actually using cloning).
	 * @param loc
	 * @return A new Location instance.
	 * @throws NullPointerException if World is null.
	 */
	public static final Location clone(final Location loc){
		return new Location(testWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}

	/**
	 * Get a copy of a location (not actually using cloning), override yaw and pitch with given values. 
	 * @param loc
	 * @param yaw
	 * @param pitch
	 * @return A new Location instance.
	 * @throws NullPointerException if the resulting world is null.
	 */
	public static final Location clone(final Location loc, final float yaw, final float pitch){
		return new Location(testWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ(), yaw, pitch);
	}

	/**
	 * Clone setBack, with yaw and pitch taken from ref, if setBack is null, ref is cloned fully.
	 * @param setBack Can be null.
	 * @param ref Must not be null.
	 * @return A new Location instance.
	 * @throws NullPointerException if the resulting world is null.
	 */
	public static final Location clone(final Location setBack, final Location ref) {
		if (setBack == null){
			return clone(ref);
		}
		else{
			return clone(setBack, ref.getYaw(), ref.getPitch());
		}
	}

	public static final Location clone(final Location setBack, final PlayerLocation ref) {
		if (setBack == null) return ref.getLocation();
		else{
			return clone(setBack, ref.getYaw(), ref.getPitch());
		}
	}

	/**
	 * Update setBack by loc.
	 * @param setBack
	 * @param loc
	 * @throws NullPointerException if loc.getWorld() is null.
	 */
	public static final void set(final Location setBack, final Location loc) {
		setBack.setWorld(testWorld(loc.getWorld()));
		setBack.setX(loc.getX());
		setBack.setY(loc.getY());
		setBack.setZ(loc.getZ());
		setBack.setYaw(loc.getYaw());
		setBack.setPitch(loc.getPitch());
	}
	
	/**
	 * Update setBack by loc.
	 * @param setBack
	 * @param loc
	 * @throws NullPointerException if loc.getWorld() is null.
	 */
	public static final void set(final Location setBack, final PlayerLocation loc) {
		setBack.setWorld(testWorld(loc.getWorld()));
		setBack.setX(loc.getX());
		setBack.setY(loc.getY());
		setBack.setZ(loc.getZ());
		setBack.setYaw(loc.getYaw());
		setBack.setPitch(loc.getPitch());
	}
	
	/**
	 * Throw a NullPointerException if world is null.
	 * @param world
	 * @return
	 */
	private static World testWorld(final World world) {
		if (world == null) {
			throw new NullPointerException("World must not be null.");
		} else {
			return world;
		}
	}
	
	/**
	 * Quick out of bounds check for yaw.
	 * @param yaw
	 * @return
	 */
	public static final boolean needsYawCorrection(final float yaw) {
		return yaw == Float.NaN || yaw < 0f || yaw >= 360f;
	}
	
	/**
	 * Quick out of bounds check for pitch.
	 * @param pitch
	 * @return
	 */
	public static final boolean needsPitchCorrection(final float pitch) {
		return pitch == Float.NaN || pitch < -90f || pitch > 90f;
	}
	
	/**
	 * Quick out of bounds check for yaw and pitch.
	 * @param yaw
	 * @param pitch
	 * @return
	 */
	public static final boolean needsDirectionCorrection(final float yaw, final float pitch) {
		return needsYawCorrection(yaw) || needsPitchCorrection(pitch);
	}
	
	/**
	 * Ensure 0 <= yaw < 360.
	 * @param yaw
	 * @return
	 */
	public static final float correctYaw(float yaw) {
		if (yaw == Float.NaN) {
			return 0f;
		}
		if (yaw >= 360f) {
			if (yaw > 10000f) {
				yaw = 0f;
			} else {
				while (yaw > 360f) {
					yaw -= 360f;
				}
			}
		}
		if (yaw < 0f) {
			if (yaw < -10000f) {
				yaw = 0f;
			} else {
				while (yaw < 0f) {
					yaw += 360f;
				}
			}
		}
		return yaw;
	}
	
	/**
	 * Ensure -90 <= pitch <= 90.
	 * @param pitch
	 * @return
	 */
	public static final float correctPitch(float pitch) {
		if (pitch == Float.NaN) {
			return 0f;
		} else if (pitch < -90f) {
			return -90f;
		} else if (pitch > 90f) {
			return 90f;
		} else {
			return pitch;
		}
	}

}
