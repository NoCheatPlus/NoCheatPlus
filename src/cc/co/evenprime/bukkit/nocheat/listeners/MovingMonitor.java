package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;
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
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		check.respawned(event);
	}
	
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		check.teleported(event);
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		MovingCheck.updateVelocity(event.getPlayer().getVelocity(), NoCheatData.getPlayerData(event.getPlayer()));
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		NoCheatData data = NoCheatData.getPlayerData(event.getPlayer());
		data.movingLastLocation = event.getTo();
		MovingCheck.updateVelocity(event.getPlayer().getVelocity(), data);
	}
}
