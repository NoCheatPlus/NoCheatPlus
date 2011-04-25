package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.listeners.BedteleportPlayerListener;

/**
 * 
 * @author Evenprime
 *
 */

public class BedteleportCheck extends Check {

	public BedteleportCheck(NoCheat plugin) {
		super(plugin, "bedteleport",  PermissionData.PERMISSION_BEDTELEPORT);
	}

	public void check(PlayerMoveEvent event) {

		// Should we check at all?
		if(hasPermission(event.getPlayer())) 
			return;

		if(event.getPlayer().isSleeping())
			event.setCancelled(true);
	}

	@Override
	protected void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		// Register listeners for bedteleport check
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, new BedteleportPlayerListener(this), Priority.Lowest, plugin);

	}
}
