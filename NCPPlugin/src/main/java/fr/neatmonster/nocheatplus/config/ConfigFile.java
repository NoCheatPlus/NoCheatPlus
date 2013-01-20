package fr.neatmonster.nocheatplus.config;

import org.bukkit.configuration.MemorySection;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionFactory;
import fr.neatmonster.nocheatplus.actions.ActionList;

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
public class ConfigFile extends RawConfigFile {

    /** The factory. */
    private ActionFactory factory;

	/**
	 * A convenience method to get an optimized action list from the configuration.
	 * 
	 * @param path
	 *            the path
	 * @param permission
	 *            the permission
	 * @return the action list
	 */
	public ActionList getOptimizedActionList(final String path, final String permission)
	{
		final String value = this.getString(path);
		return factory.createActionList(value, permission).getOptimizedCopy(this);
	}

	/**
	 * A convenience method to get default action lists from the configuration, without
	 * applying any optimization.
	 * 
	 * @param path
	 *            the path
	 * @param permission
	 *            the permission
	 * @return the action list
	 */
	public ActionList getDefaultActionList(final String path, final String permission)
	{
		final String value = this.getString(path);
		return factory.createActionList(value, permission);
	}

    /**
     * Do this after reading new data.
     */
    public void regenerateActionLists() {
        factory = ConfigManager.getActionFactory(((MemorySection) this.get(ConfPaths.STRINGS)).getValues(false));
    }

    /**
     * Safely store ActionLists back into the yml file.
     * 
     * @param path
     *            the path
     * @param list
     *            the list
     */
    public void set(final String path, final ActionList list) {
        final StringBuffer string = new StringBuffer();

        for (final int threshold : list.getThresholds()) {
            if (threshold > 0)
                string.append(" vl>").append(threshold);
            for (final Action action : list.getActions(threshold))
                string.append(" ").append(action);
        }

        set(path, string.toString().trim());
    }
}