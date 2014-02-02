package fr.neatmonster.nocheatplus.compat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;


/**
 * Various bridge methods not enough for an own class.
 * @author mc_dev
 *
 */
public class BridgeMisc {
	
	/**
	 * Return a shooter of a projectile if we get an entity, null otherwise.
	 */
	public static Player getProjectileShooterPlayer(Projectile projectile) {
		Object source = projectile.getShooter();
		if (source instanceof Player) {
			return (Player) source;
		} else {
			return null;
		}
	}
	
	/**
	 * Retrieve a player from projectiles or cast to player, if possible.
	 * @param damager
	 * @return
	 */
	public static Player getAttackingPlayer(Entity damager) {
		if (damager instanceof Player) {
			return (Player) damager;
		} else if (damager instanceof Projectile) {
			return getProjectileShooterPlayer((Projectile) damager);
		} else {
			return null;
		}
	}
	
}
