package fr.neatmonster.nocheatplus.compat.blocks;

import fr.neatmonster.nocheatplus.config.WorldConfigProvider;

/**
 * Provide a setup method for additional BlockProperties initialization.<br>
 * Typically MCAccess can implement it. TODO: An extra factory for Bukkit level.
 * <hr>
 * NOTE: This might not be the final location for this class, in addition this might get split/changed into other classes with adding better mod support. 
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
