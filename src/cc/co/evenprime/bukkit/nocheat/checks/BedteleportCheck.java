package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatData;

/**
 * 
 * @author Evenprime
 *
 */

public class BedteleportCheck extends Check {

	public BedteleportCheck(NoCheat plugin) {
		super(plugin);
		setActive(true);
	}

	public void check(PlayerMoveEvent event) {

		// Should we check at all?
		if(plugin.hasPermission(event.getPlayer(), NoCheatData.PERMISSION_BEDTELEPORT)) 
			return;

		if(event.getPlayer().isSleeping())
			event.setCancelled(true);
	}

	@Override
	public String getName() {
		return "bedteleport";
	}
}
