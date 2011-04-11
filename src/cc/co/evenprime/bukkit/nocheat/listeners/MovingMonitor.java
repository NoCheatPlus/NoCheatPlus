package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;

/**
 * 
 * @author Evenprime
 *
 */
public class MovingMonitor extends PlayerListener {

	private MovingCheck check;

	public MovingMonitor(MovingCheck check) {
		this.check = check;
	}

	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		check.teleported(event);
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		check.updateVelocity(event.getPlayer());
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		check.updateVelocity(event.getPlayer());
	}
}
