package fr.neatmonster.nocheatplus.config;

import org.bukkit.configuration.MemorySection;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;

/**
 * A special configuration class created to handle the loading/saving of actions lists. This is for normal use with the plugin.
 */
public class ConfigFile extends ConfigFileWithActions<ViolationData, ActionList> {
    
    @Override
    public void setActionFactory() {
        factory = ConfigManager.getActionFactory(((MemorySection) this.get(ConfPaths.STRINGS)).getValues(false));
    }
    
}