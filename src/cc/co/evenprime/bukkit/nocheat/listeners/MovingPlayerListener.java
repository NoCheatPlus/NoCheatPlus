package cc.co.evenprime.bukkit.nocheat.listeners;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.DataManager;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * Handle events for Player related events
 * 
 * @author Evenprime
 */

public class MovingPlayerListener extends PlayerListener {

	private final MovingCheck check;
	private final DataManager dataManager;

	public MovingPlayerListener(DataManager dataManager, MovingCheck check) {
		this.dataManager = dataManager;
		this.check = check;
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {

		if(event.isCancelled()) return;

		final Player player = event.getPlayer();

		// Is there something to do at all?
		if(!check.skipCheck(player)) {

			final MovingData data = dataManager.getMovingData(player);
			final Location from = player.getLocation();
			final Location to = event.getTo();

			Location newTo = null;

			if(check.shouldBeApplied(player, data, from, to)) {
				// Check it
				newTo = check.check(player, from, to, data);
			}

			// Did the checks decide we need a new To-Location?
			if(newTo != null) {
				event.setTo(new Location(newTo.getWorld(), newTo.getX(), newTo.getY(), newTo.getZ(), event.getTo().getYaw(), event.getTo().getPitch()));
			}
		}
	}
}
