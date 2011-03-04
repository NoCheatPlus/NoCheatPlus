package cc.co.evenprime.bukkit.nocheat.listeners;


import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

import cc.co.evenprime.bukkit.nocheat.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.checks.DupebydeathCheck;

/**
 * 
 * @author Evenprime
 *
 */
public class NoCheatEntityListener extends EntityListener {

	@Override
	public void onEntityDeath(EntityDeathEvent event) {

		if(NoCheatConfiguration.dupebydeathCheckActive) {
			DupebydeathCheck.playerDeath(event);
		}
	}
}
