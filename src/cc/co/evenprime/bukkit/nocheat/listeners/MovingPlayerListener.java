package cc.co.evenprime.bukkit.nocheat.listeners;


import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;

/**
 * Handle events for Player related events
 * 
 * @author Evenprime
 */

public class MovingPlayerListener extends PlayerListener {

	private MovingCheck check;

	public MovingPlayerListener(MovingCheck check) {
		this.check = check;
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {

		if(!event.isCancelled()) check.check(event);
	}
}
