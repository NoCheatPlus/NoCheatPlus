package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * 
 * @author Evenprime
 *
 */
public class MovingPlayerMonitor extends PlayerListener {

	private MovingCheck check;

	public MovingPlayerMonitor(MovingCheck check) {
		this.check = check;
	}

	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		check.teleported(event);
	}
	
	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		this.check.respawned(event);
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		check.updateVelocity(event.getPlayer().getVelocity(), MovingData.get(event.getPlayer()));
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!event.isCancelled()) {
			MovingData data = MovingData.get(event.getPlayer());
			data.lastSeenInWorld = event.getPlayer().getLocation().getWorld();
			if( event.getPlayer().isInsideVehicle()) {
				data.setBackPoint = event.getTo();	
			}
			else {
				data.insideVehicle = false;
			}

			check.updateVelocity(event.getPlayer().getVelocity(), data);
		}
	}
}
