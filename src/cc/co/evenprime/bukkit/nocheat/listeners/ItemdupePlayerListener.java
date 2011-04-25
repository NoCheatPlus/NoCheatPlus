package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import cc.co.evenprime.bukkit.nocheat.checks.ItemdupeCheck;

public class ItemdupePlayerListener extends PlayerListener {

	ItemdupeCheck check;

	public ItemdupePlayerListener(ItemdupeCheck itemdupeCheck) {
		check = itemdupeCheck;
	}

	@Override
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {

		if(!event.isCancelled()) check.check(event);
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {

		if(!event.isCancelled()) check.check(event);
	}
}
