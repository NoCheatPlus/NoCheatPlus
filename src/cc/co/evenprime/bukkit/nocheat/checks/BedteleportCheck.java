package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.ConfigurationException;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.listeners.BedteleportPlayerListener;

/**
 * 
 * @author Evenprime
 *
 */

public class BedteleportCheck extends Check {

	public BedteleportCheck(NoCheat plugin, NoCheatConfiguration config) {
		super(plugin, "bedteleport",  PermissionData.PERMISSION_BEDTELEPORT, config);
	}

	public void check(PlayerMoveEvent event) {

		// Should we check at all?
		if(skipCheck(event.getPlayer())) 
			return;

		if(event.getPlayer().isSleeping())
			event.setCancelled(true);
	}
	
	@Override
	public void configure(NoCheatConfiguration config) {
		
		try {
			setActive(config.getBooleanValue("active.bedteleport"));
		} catch (ConfigurationException e) {
			setActive(false);
			e.printStackTrace();
		}
	}
	
	@Override
	protected void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		// Register listeners for bedteleport check
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, new BedteleportPlayerListener(this), Priority.Lowest, plugin);

	}
}
