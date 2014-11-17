package fr.neatmonster.nocheatplus.compat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.inventory.FastConsume;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.net.protocollib.ProtocolLibComponent;

/**
 * Default factory for add-in components which might only be available under certain circumstances.
 * 
 * @author mc_dev
 */
public class DefaultComponentFactory {
	
	/**
	 * This will be called from within the plugin in onEnable, after registration of all core listeners and components. After each components addition processQueuedSubComponentHolders() will be called to allow registries for further optional components.
	 * @param plugin 
	 * @return
	 */
	public Collection<Object> getAvailableComponentsOnEnable(NoCheatPlus plugin){
		final List<Object> available = new LinkedList<Object>();
		
		// Add components (try-catch).
		// Check: inventory.fastconsume.
		try{
			// TODO: Static test methods !?
			FastConsume.testAvailability();
			available.add(new FastConsume());
		}
		catch (Throwable t){
			StaticLog.logInfo("[NoCheatPlus] Inventory checks: FastConsume is not available.");
		}
		
		// ProtocolLib dependencies.
		try {
			available.add(new ProtocolLibComponent(plugin));
		} catch (Throwable t){
			StaticLog.logInfo("[NoCheatPlus] Packet level access: ProtocolLib is not available.");
		}
		
		return available;
	}
}
