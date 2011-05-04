package cc.co.evenprime.bukkit.nocheat.checks;


import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;

/**
 *
 * @author Evenprime
 *
 */
public abstract class Check {

	private boolean active = false;
	private boolean listenersRegistered = false;
	private int permission;
	private String name;
	protected NoCheat plugin;

	protected Check(NoCheat plugin, String name, int permission, NoCheatConfiguration config) {
		this.plugin = plugin;
		this.permission = permission;
		this.name = name;
		
		configure(config);
	}

	public boolean skipCheck(Player player) {
		// Should we check at all?
		return !active || plugin.hasPermission(player, permission); 
	}
	
	protected abstract void configure(NoCheatConfiguration config);

	protected abstract void registerListeners();

	public boolean isActive() {
		return active;
	}

	protected void setActive(boolean active) {
		synchronized(this) {
			if(active && !listenersRegistered) {
				listenersRegistered = true;
				registerListeners();
			}
		}

		// There is no way to unregister listeners ...
		this.active = active;
	}

	public String getName() {
		return name;
	}


}
