package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import cc.co.evenprime.bukkit.nocheat.checks.SpeedhackCheck;

/**
 * 
 * @author Evenprime
 *
 */
public class SpeedhackPlayerListener extends PlayerListener {

	private SpeedhackCheck check;

	public SpeedhackPlayerListener(SpeedhackCheck check) {
		this.check = check;
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {

		if(!event.isCancelled()) check.check(event);
	}
	
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		
		if(!event.isCancelled()) check.teleported(event);
	}
}
