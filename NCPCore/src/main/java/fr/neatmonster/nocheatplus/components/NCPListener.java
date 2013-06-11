package fr.neatmonster.nocheatplus.components;

import org.bukkit.event.Listener;

/**
 * Registers with default name "NoCheatPlus_Listener".
 * @author mc_dev
 *
 */
public abstract class NCPListener implements Listener, ComponentWithName {

	@Override
	public String getComponentName() {
		return "NoCheatPlus_Listener";
	}

}
