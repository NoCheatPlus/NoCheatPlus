package fr.neatmonster.nocheatplus.checks.access;

import org.bukkit.entity.Player;

/**
 * A factory for creating and accessing configurations.
 * 
 * @author asofold
 */
public interface CheckConfigFactory {

    /**
     * Gets the configuration for a specified player.
     * 
     * @param player
     *            the player
     * @return the configuration
     */
    public ICheckConfig getConfig(Player player);

    /**
     * Remove all stored configurations.
     */
    public void removeAllConfigs();

}
