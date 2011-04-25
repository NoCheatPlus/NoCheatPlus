package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerTeleportEvent;

import cc.co.evenprime.bukkit.nocheat.checks.BedteleportCheck;

/**
 *
 * @author Evenprime
 *
 */
public class BedteleportPlayerListener extends PlayerListener {

	private BedteleportCheck check;

	public BedteleportPlayerListener(BedteleportCheck check) {
		this.check = check;
	}

	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {

		if(!event.isCancelled()) check.check(event);
	}
}
