package fr.neatmonster.nocheatplus.event;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.components.ComponentWithName;
import fr.neatmonster.nocheatplus.event.GenericListener.MethodEntry;
import fr.neatmonster.nocheatplus.event.GenericListener.MethodEntry.MethodOrder;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/**
 * This class allows to register event-listeners which will all be called form within one event handler per event+priority combination.<br>
 * In future this will also allow to control registration order, to ensure receiving events before or after some other listeners or plugins.<br>
 * @author mc_dev
 *
 */
public class ListenerManager {
	
	protected Map<Class<? extends Event>, EnumMap<EventPriority, GenericListener<?>>> map = new HashMap<Class<? extends Event>, EnumMap<EventPriority,GenericListener<?>>>();
	private final Plugin plugin;
	private boolean registerDirectly;
	
	public ListenerManager(Plugin plugin){
		this(plugin, false);
	}
	
	public ListenerManager(Plugin plugin, boolean registerDirectly){
		this.plugin = plugin;
		this.registerDirectly = true;
	}
	
	/**
	 * Probably put to protected later.
	 * @param clazz
	 * @param priority
	 * @return
	 */
	public <E extends Event> GenericListener<E> getListener(Class<E> clazz, EventPriority priority){
		EnumMap<EventPriority, GenericListener<?>> prioMap = map.get(clazz);
		if (prioMap == null){
			prioMap = new EnumMap<EventPriority, GenericListener<?>>(EventPriority.class);
			map.put(clazz, prioMap);
		}
		@SuppressWarnings("unchecked")
		GenericListener<E> listener = (GenericListener<E>) prioMap.get(priority);
		if (listener == null){
			listener = new GenericListener<E>(clazz, priority);
			prioMap.put(priority, listener);
		}
		if (registerDirectly && !listener.isRegistered()) listener.register(plugin);
		return listener;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public boolean isRegisterDirectly() {
		return registerDirectly;
	}

	public void setRegisterDirectly(boolean registerDirectly) {
		this.registerDirectly = registerDirectly;
	}
	
	/**
	 * Register all yet unregistered generic listeners with the PluginManager.<br>
	 * NOTE: This does not set registerDirectly.
	 */
	public void registerAllWithBukkit(){
		for (final EnumMap<EventPriority, GenericListener<?>> prioMap : map.values()){
			for (final GenericListener<?> listener : prioMap.values()){
				if (!listener.isRegistered()) listener.register(plugin);
			}
		}
	}
	
	/**
	 * Clear internal mappings.
	 */
	public void clear(){
		map.clear();
	}
	
	/**
	 * This registers all declared methods that have the @EventHandler annotation.<br>
	 * Interfaces checked if arguments are not given: IHaveMethodOrder (order), ComponentWithName (tag)<br>
	 * NOTE: Does not do any super class checking.
	 * @param listener
	 * @param tag Identifier for the registering plugin / agent, null is not discouraged, but null entries are ignored concerning sortin order.
	 */
	public void registerAllEventHandlers(Listener listener, String tag){
		registerAllEventHandlers(listener, tag, null);
	}
	
	/**
	 * This registers all methods that have the @EventHandler annotation.<br>
	 * Interfaces checked if arguments are not given: IHaveMethodOrder (order), ComponentWithName (tag)<br>
	 * NOTE: Does not do any super class checking.
	 * @param listener
	 * @param tag Identifier for the registering plugin / agent, null is not discouraged, but null entries are ignored concerning sortin order.
	 * @param order Allows to register before other tags or just first. Expect MethodOrder to change in near future. The method order of already registered methods will not be compared to.
	 */
	public void registerAllEventHandlers(Listener listener, String tag, MethodOrder order){
		if (order == null && listener instanceof IHaveMethodOrder){
			order = ((IHaveMethodOrder) listener).getMethodOrder();
		}
		if (tag == null && listener instanceof ComponentWithName){
			// TODO: maybe change to an interface only defined here. 
			tag = ((ComponentWithName) listener).getComponentName();
		}
		Class<?> clazz = listener.getClass();
		Set<Method> allMethods = new HashSet<Method>();
		for (Method method : clazz.getMethods()){
			allMethods.add(method);
		}
		for (Method method : clazz.getDeclaredMethods()){
			allMethods.add(method);
		}
		for (Method method : allMethods){
			EventHandler anno = method.getAnnotation(EventHandler.class);
			if (anno == null) continue;
			if (!method.isAccessible()){
				// Try to make it accessible.
				try{
					method.setAccessible(true);
				} catch (SecurityException e){
					CheckUtils.logWarning("[ListenerManager]  Can not set method accessible: " + method.toGenericString() +" registered in " + clazz.getName()+ ", ignoring it!");
				}
			}
			Class<?>[] argTypes = method.getParameterTypes();
			if (argTypes.length != 1){
				CheckUtils.logWarning("[ListenerManager] Bad method signature (number of arguments not 1): " + method.toGenericString() +" registered in " + clazz.getName()+ ", ignoring it!");
				continue;
			}
			Class<?> eventType = argTypes[0];
			if (!Event.class.isAssignableFrom(eventType)){
				CheckUtils.logWarning("[ListenerManager] Bad method signature (argument does not extend Event): " + method.toGenericString() +" registered in " + clazz.getName()+ ", ignoring it!");
				continue;
			}
			Class<? extends Event> checkedEventType = eventType.asSubclass(Event.class);
			getListener(checkedEventType, anno.priority()).addMethodEntry(new MethodEntry(listener, method, anno.ignoreCancelled(), tag, order));
		}
	}

	/**
	 * TODO: more methods for tags ? (+ return something?).
	 * @param listener
	 */
	public void remove(Listener listener) {
		for (EnumMap<EventPriority, GenericListener<?>> prioMap : map.values()){
			for (GenericListener<?> gl : prioMap.values()){
				gl.remove(listener);
			}
		}
	}
	
}
