package cc.co.evenprime.bukkit.nocheat.checks;

import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;

/**
 *
 * @author Evenprime
 *
 */
public abstract class Check {

	public Check(NoCheatPlugin plugin) {
		this.plugin = plugin;
	}

	private boolean active = true;
	protected NoCheatPlugin plugin;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean a) {
		active = a;
	}

	public abstract String getName();
}
