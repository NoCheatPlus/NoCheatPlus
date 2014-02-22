package fr.neatmonster.nocheatplus.compat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.inventory.FastConsume;
import fr.neatmonster.nocheatplus.logging.LogUtil;

/**
 * Default factory for add-in components which might only be available under certain circumstances.
 * 
 * @author mc_dev
 */
public class DefaultComponentFactory {
	
	/**
	 * This will be called from within the plugin in onEnable, after registration of all core listeners and components. After each components addition processQueuedSubComponentHolders() will be called to allow registries for further optional components.
	 * @return
	 */
	public Collection<Object> getAvailableComponentsOnEnable(){
		final List<Object> available = new LinkedList<Object>();
		
		// Add components (try-catch).
		// Check: inventory.fastconsume.
		try{
			FastConsume.testAvailability();
			available.add(new FastConsume());
		}
		catch (Throwable t){
			LogUtil.logInfo("[NoCheatPlus] Inventory checks: FastConsume is not available.");
		}
		
		return available;
	}
}
