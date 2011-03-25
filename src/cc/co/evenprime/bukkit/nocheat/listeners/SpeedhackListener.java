package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.checks.SpeedhackCheck;

public class SpeedhackListener extends PlayerListener {

	private SpeedhackCheck check;

	public SpeedhackListener(SpeedhackCheck check) {
		this.check = check;
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {

		if(!event.isCancelled() && check.isActive())
			check.check(event);
	}
}
