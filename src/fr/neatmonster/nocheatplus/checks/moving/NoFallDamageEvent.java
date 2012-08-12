package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;


/**
 * Very simple version to allow canceling.
 * @author mc_dev
 *
 */
public class NoFallDamageEvent extends EntityDamageEvent {

	public NoFallDamageEvent(Entity damagee, DamageCause cause, int damage) {
		super(damagee, cause, damage);
	}

}
