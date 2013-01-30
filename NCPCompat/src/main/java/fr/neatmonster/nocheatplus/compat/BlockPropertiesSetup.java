package fr.neatmonster.nocheatplus.compat;

import fr.neatmonster.nocheatplus.config.WorldConfigProvider;

/**
 * Provide a setup method for additional BlockProperties initialization.<br>
 * Typically MCAccess can implement it. TODO: An extra factory for Bukkit level.
 * @author mc_dev
 *
 */
public interface BlockPropertiesSetup {
	/**
	 * Additional initialization.
	 * @param worldConfigProvider Configuration provider if needed.
	 */
	public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider);
}
