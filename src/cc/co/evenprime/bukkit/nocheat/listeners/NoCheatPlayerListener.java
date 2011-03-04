package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;
import cc.co.evenprime.bukkit.nocheat.checks.BedteleportCheck;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.checks.SpeedhackCheck;

/**
 * Handle events for all Player related events
 * 
 * @author Evenprime
 */

public class NoCheatPlayerListener extends PlayerListener {

	public NoCheatPlayerListener() {  }

	@Override
	public void onPlayerQuit(PlayerEvent event) {
		NoCheatPlugin.playerData.remove(event.getPlayer());
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {

		// Get the player-specific data
		NoCheatData data = NoCheatPlugin.getPlayerData(event.getPlayer());

		if(!event.isCancelled() && NoCheatConfiguration.speedhackCheckActive)
			SpeedhackCheck.check(data, event);

		if(!event.isCancelled() && NoCheatConfiguration.movingCheckActive)
			MovingCheck.check(data, event);

	}

	@Override
	public void onPlayerTeleport(PlayerMoveEvent event) {

		if(!event.isCancelled() && NoCheatConfiguration.bedteleportCheckActive) {
			BedteleportCheck.check(event);
		}
	}
}
