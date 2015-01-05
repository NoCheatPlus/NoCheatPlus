package fr.neatmonster.nocheatplus.config;

import java.util.Collection;

/**
 * This is to bridge the gap between ConfigFile which needs Action and RawConfigFile which has to be available in NCPCompat. <br>
 * Aim is not speed of execution but providing a way of accessing all configs to set properties from within compatibility modules.
 * <br>
 * This might be seen as a refactoring/structuring stage, leading to putting actions to NCPCompat as well. RawConfigFile might get changed.
 * @author mc_dev
 *
 */
public interface WorldConfigProvider <C extends RawConfigFile>{

    /**
     * Get the default configuration.
     * @return
     */
    public C getDefaultConfig();

    /**
     * Get the world configuration. 
     * @param worldName The default config has null as world. The default config is returned, if the world is not known.
     * @return
     */
    public C getConfig(String worldName);

    /**
     * Get a Collection-view of all worlds config files, including the default configuration.
     * @return
     */
    public Collection<C> getAllConfigs();

    // TODO: Add operations for all configs, like setForAllConfigs, get(Max|min)NumberForAllConfigs, ....
}
