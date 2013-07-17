package fr.neatmonster.nocheatplus.components;

/**
 * Interface for a component that needs to be notified about a reload. Use the annotation SetupOrder to influence when to get executed.
 * <hr>
 * Priorities used by NCP with SetupOrder:
 * <li>Core: -100</li>
 * <li>DataManager: -80</li>
 * <li>Rest (checks): 0</li>
 * @author mc_dev
 *
 */
public interface INotifyReload {
	public void onReload();
}
