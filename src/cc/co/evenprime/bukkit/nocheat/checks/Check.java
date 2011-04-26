package cc.co.evenprime.bukkit.nocheat.checks;


import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;

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

	public Check(NoCheat plugin, String name, int permission) {
		this.plugin = plugin;
		this.permission = permission;
		this.name = name;
	}

	public boolean hasPermission(Player player) {
		// Should we check at all?
		return !active || plugin.hasPermission(player, permission); 
	}

	protected abstract void registerListeners();

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
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
