package fr.neatmonster.nocheatplus.config;

import org.bukkit.configuration.MemorySection;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;

/*
 * MM'""""'YMM                   .8888b oo          MM""""""""`M oo dP          
 * M' .mmm. `M                   88   "             MM  mmmmmmmM    88          
 * M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. M'      MMMM dP 88 .d8888b. 
 * M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 MM  MMMMMMMM 88 88 88ooood8 
 * M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 MM  MMMMMMMM 88 88 88.  ... 
 * MM.     .dM `88888P' dP    dP dP     dP `8888P88 MM  MMMMMMMM dP dP `88888P' 
 * MMMMMMMMMMM                                  .88 MMMMMMMMMMMM                
 *                                          d8888P                              
 */
/**
 * A special configuration class created to handle the loading/saving of actions lists. This is for normal use with the plugin.
 */
public class ConfigFile extends ConfigFileWithActions<ViolationData, ActionList> {
    
    @Override
    public void regenerateActionLists() {
        factory = ConfigManager.getActionFactory(((MemorySection) this.get(ConfPaths.STRINGS)).getValues(false));
    }
    
}