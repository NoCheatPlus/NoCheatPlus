package fr.neatmonster.nocheatplus.compat;

/**
 * Provide a setup method for additional BlockProperties initialization.<br>
 * Typically MCAccess can implement it. TODO: An extra factory for Bukkit level.
 * @author mc_dev
 *
 */
public interface BlockPropertiesSetup {
	/**
	 * Additional initialization.
	 */
	public void setupBlockProperties();
}
