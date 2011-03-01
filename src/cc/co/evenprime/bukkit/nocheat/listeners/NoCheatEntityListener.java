package cc.co.evenprime.bukkit.nocheat.listeners;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import cc.co.evenprime.bukkit.nocheat.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.checks.DupebydeathCheck;

public class NoCheatEntityListener extends EntityListener {

	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		
		if(NoCheatConfiguration.dupebydeathCheckActive) {
			DupebydeathCheck.playerDeath(event);
		}
	}
}
