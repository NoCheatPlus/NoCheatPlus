package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

import cc.co.evenprime.bukkit.nocheat.checks.ItemdupeCheck;

public class ItemdupeEntityListener extends EntityListener {

	ItemdupeCheck check;

	public ItemdupeEntityListener(ItemdupeCheck itemdupeCheck) {
		check = itemdupeCheck;
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent event) {

		check.check(event);
	}
}
