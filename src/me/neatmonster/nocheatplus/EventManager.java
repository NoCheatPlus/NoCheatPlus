package me.neatmonster.nocheatplus;

import java.util.List;

import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;

import org.bukkit.event.Listener;

public interface EventManager extends Listener {

    /**
     * Used for debug output, if checks are activated for the world-specific
     * config that is given as a parameter
     * 
     * @param cc
     *            The config
     * @return A list of active/enabled checks
     */
    public List<String> getActiveChecks(ConfigurationCacheStore cc);
}
