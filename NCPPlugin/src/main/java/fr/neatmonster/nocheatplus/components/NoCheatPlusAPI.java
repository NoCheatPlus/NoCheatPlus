package fr.neatmonster.nocheatplus.components;


/**
 * ComponentRegistry: 
 * <li>Supported components: Listener, TickListener, PermStateReceiver, INotifyReload, INeedConfig, IRemoveData, MCAccessHolder, ConsistencyChecker, JoinLeaveListener</li>
 * <li>ComponentRegistry instances will be registered as sub registries unless you use the addComponent(Object, boolean) method appropriately. </li>
 * <li>IHoldSubComponents instances will be registered in the next tick (scheduled task), those added within onEnable will get registered after looping in onEnable.</li>
 * <li>Registering components should be done during onEnable or any time while the plugin is enabled, all components will be unregistered in onDisable.</li>
 * <li>References to all components will be held until onDisable is finished.</li> 
 * <li>Interfaces checked for managed listeners: IHaveMethodOrder (method), ComponentWithName (tag)</li>
 * @author mc_dev
 *
 */
public interface NoCheatPlusAPI extends ComponentRegistry<Object>, ComponentRegistryProvider{
	
	/**
	 * By default addComponent(Object) will register ComponentFactories as well.
	 * @param obj
	 * @param allowComponentRegistry If to allow registering ComponentFactories.
	 * @return
	 */
	public boolean addComponent(Object obj, boolean allowComponentRegistry);
	
}
