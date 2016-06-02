/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import fr.neatmonster.nocheatplus.logging.StaticLog;

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
	 * Probably put to protected later.<br>
	 * NOTE: Not thread-safe.
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
		for (final Map<EventPriority, GenericListener<?>> prioMap : map.values()){
			for (final GenericListener<?> listener : prioMap.values()){
				listener.clear();
			}
		}
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
	 * NOTES: <br>
	 * - Does not do any super class checking.<br>
	 * - Given MethodOrder overridden by implementing IHaveMethodOrder overridden by per method @MethodOrder annotations.<br>
	 * - Given tag overridden by Listener implementing ComponentWithName overridden by per method @MEthodOrder annotations.<br> 
	 * @param listener
	 * @param tag Identifier for the registering plugin / agent, null is not discouraged, but null entries are ignored concerning sorting order.
	 * @param order Allows to register before other tags or just first. Expect MethodOrder to change in near future. The method order of already registered methods will not be compared to.
	 */
	public void registerAllEventHandlers(Listener listener, String tag, MethodOrder order){
		if (listener instanceof IHaveMethodOrder){
			order = ((IHaveMethodOrder) listener).getMethodOrder();
		}
		if (listener instanceof ComponentWithName){
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
					StaticLog.logWarning("[ListenerManager]  Can not set method accessible: " + method.toGenericString() +" registered in " + clazz.getName()+ ", ignoring it!");
				}
			}
			Class<?>[] argTypes = method.getParameterTypes();
			if (argTypes.length != 1){
				StaticLog.logWarning("[ListenerManager] Bad method signature (number of arguments not 1): " + method.toGenericString() +" registered in " + clazz.getName()+ ", ignoring it!");
				continue;
			}
			Class<?> eventType = argTypes[0];
			if (!Event.class.isAssignableFrom(eventType)){
				StaticLog.logWarning("[ListenerManager] Bad method signature (argument does not extend Event): " + method.toGenericString() +" registered in " + clazz.getName()+ ", ignoring it!");
				continue;
			}
			Class<? extends Event> checkedEventType = eventType.asSubclass(Event.class);
			MethodOrder tempOrder = order;
			String tempTag = tag;
			fr.neatmonster.nocheatplus.event.MethodOrder orderAnno = method.getAnnotation(fr.neatmonster.nocheatplus.event.MethodOrder.class);
			if (orderAnno != null){
				MethodOrder veryTempOrder = tempOrder = MethodOrder.getMethodOrder(orderAnno);
				if (veryTempOrder != null) tempOrder = veryTempOrder;
				if (!orderAnno.tag().isEmpty()) tempTag = orderAnno.tag();
			}
			getListener(checkedEventType, anno.priority()).addMethodEntry(new MethodEntry(listener, method, anno.ignoreCancelled(), tempTag, tempOrder));
		}
	}

	/**
	 * TODO: more methods for tags ? (+ return something?).
	 * @param listener
	 */
	public void remove(Listener listener) {
		for (Map<EventPriority, GenericListener<?>> prioMap : map.values()){
			for (GenericListener<?> gl : prioMap.values()){
				gl.remove(listener);
			}
		}
	}
	
	/**
	 * Check if any GenericListeners are registered with Bukkit. <br>(To check if actually any listener methods are registered use: hasListenerMethods) 
	 * @return
	 */
	public boolean hasListeners(){
		return !map.isEmpty();
	}
	
	/**
	 * Check if any GenericListeners are present that are registered.
	 * @return
	 */
	public boolean hasRegisteredListeners(){
		for (Map<EventPriority, GenericListener<?>> prioMap : map.values()){
			for (GenericListener<?> gl : prioMap.values()){
				if (gl.isRegistered()) return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if any GenericListeners are present that are not yet registered.
	 * @return
	 */
	public boolean hasPendingListeners(){
		for (Map<EventPriority, GenericListener<?>> prioMap : map.values()){
			for (GenericListener<?> gl : prioMap.values()){
				if (!gl.isRegistered()) return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if any methods are registered for listening.
	 * @return
	 */
	public boolean hasListenerMethods(){
		for (Map<EventPriority, GenericListener<?>> prioMap : map.values()){
			for (GenericListener<?> gl : prioMap.values()){
				if (gl.hasListenerMethods()) return true;
			}
		}
		return false;
	}
	
}
