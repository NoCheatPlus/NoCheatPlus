package fr.neatmonster.nocheatplus.components;


/**
 * ComponentRegistry: 
 * <li>Supported components: Listener, TickListener, PermStateReceiver, INotifyReload, INeedConfig, IRemoveData</li>
 * <li>Registering components should be done during onEnable or any time while the plugin is enabled, all components will be unregistered in onDisable.</li>
 * <li>References to all components will be held until onDisable is finished.</li> 
 * @author mc_dev
 *
 */
public interface NoCheatPlusAPI extends ComponentRegistry{
	
}
