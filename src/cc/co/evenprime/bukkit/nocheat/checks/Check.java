package cc.co.evenprime.bukkit.nocheat.checks;

import cc.co.evenprime.bukkit.nocheat.NoCheat;

/**
 *
 * @author Evenprime
 *
 */
public abstract class Check {

	public Check(NoCheat plugin) {
		this.plugin = plugin;
	}

	private boolean active = true;
	protected NoCheat plugin;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean a) {
		active = a;
	}

	public abstract String getName();
}
