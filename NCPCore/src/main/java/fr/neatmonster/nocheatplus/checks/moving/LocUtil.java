package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.Location;

import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

public class LocUtil {

	/**
	 * Simple get a copy (not actually using cloning).
	 * @param loc
	 * @return
	 */
	static final Location clone(final Location loc){
		return new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}

	/**
	 * Clone with given yaw and pitch. 
	 * @param loc
	 * @param yaw
	 * @param pitch
	 * @return
	 */
	static final Location clone(final Location loc, final float yaw, final float pitch){
		return new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), yaw, pitch);
	}

	/**
	 * Clone with yaw and pitch of ref, use ref if setBack is null.
	 * @param setBack
	 * @param ref
	 * @return
	 */
	static final Location clone(final Location setBack, final Location ref) {
		if (setBack == null){
			return clone(ref);
		}
		else{
			return clone(setBack, ref.getYaw(), ref.getPitch());
		}
	}

	static final Location clone(final Location setBack, final PlayerLocation ref) {
		if (setBack == null) return ref.getLocation();
		else{
			return clone(setBack, ref.getYaw(), ref.getPitch());
		}
	}

	/**
	 * SA
	 * @param setBack
	 * @param loc
	 */
	static final void set(final Location setBack, final Location loc) {
		setBack.setWorld(loc.getWorld());
		setBack.setX(loc.getX());
		setBack.setY(loc.getY());
		setBack.setZ(loc.getZ());
		setBack.setYaw(loc.getYaw());
		setBack.setPitch(loc.getPitch());
	}

	static final void set(final Location setBack, final PlayerLocation loc) {
		setBack.setWorld(loc.getWorld());
		setBack.setX(loc.getX());
		setBack.setY(loc.getY());
		setBack.setZ(loc.getZ());
		setBack.setYaw(loc.getYaw());
		setBack.setPitch(loc.getPitch());
	}

}
