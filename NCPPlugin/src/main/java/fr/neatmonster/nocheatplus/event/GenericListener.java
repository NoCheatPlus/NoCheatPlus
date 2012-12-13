package fr.neatmonster.nocheatplus.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.utilities.LogUtil;

/**
 * listener registered for one event only. Allows to delegate to other registered listeners.
 * @author mc_dev
 *
 * @param <E>
 */
public class GenericListener<E extends Event> implements Listener, EventExecutor {
	
	public static class MethodEntry{
		/**
		 * beforeTag overrides afterTag
		 * @author mc_dev
		 *
		 */
		public static class MethodOrder{
			public static final MethodOrder getMethodOrder(fr.neatmonster.nocheatplus.event.MethodOrder anno){
				return anno.beforeTag().isEmpty() ? null : new MethodOrder(anno.beforeTag());
			}
			public final String beforeTag;
			/**
			 * You can use regular expressions for beforeTag, so it will be checked from position 0 on.<br>
			 * You can specify "*" for "all other tags" to add it in front.
			 * @param beforeTag
			 */
			public MethodOrder(String beforeTag){
				this.beforeTag = beforeTag;
			}
		}
		// One method
		
		// TODO
		public final Object listener; 
		public final Method method;
		public final boolean ignoreCancelled;
		
		/** 
		 * This allows setting some information about where this listener comes from,
		 * also allowing to register before listeners with other tags. The default plugin should be null.
		 */ 
		public final String tag;
		public final  MethodOrder order;
		
		public MethodEntry(Object listener, Method method, boolean ignoreCancelled, String tag, MethodOrder order){
			this.listener = listener;
			this.method = method;
			this.ignoreCancelled = ignoreCancelled;
			this.tag = tag;
			this.order = order;
		}
	}
	
	////////////////
	// Instance
	////////////////
	
	protected final Class<E> clazz;
	
	protected MethodEntry[] entries = new MethodEntry[0];
	
	/** If the event type implements Cancellable, is set once only, null means unknsown (TODO) */
	protected final boolean isCancellable;

	/** Event priority, for debugging purposes. Note that the factories seem to have to use another variable name. */
	protected final EventPriority priority;
	
	private boolean registered = false;
	
	public GenericListener(final Class<E> clazz, final EventPriority priority) {
		this.clazz = clazz;
		this.priority = priority;
		isCancellable = clazz.isInstance(Cancellable.class);
	}
	
	@Override
	public void execute(final Listener listener, final Event event){
		if (!clazz.isAssignableFrom(event.getClass())){
			// Strange but true.
			return;
		}
		// TODO: profiling option !
		final Cancellable cancellable = isCancellable ? (Cancellable) event : null;

		final MethodEntry[] entries = this.entries;
		for (int i = 0; i < entries.length ; i++){
			final MethodEntry entry = entries[i];
			try {
				if (!isCancellable || !entry.ignoreCancelled || !cancellable.isCancelled()) entry.method.invoke(entry.listener, event);
			} catch (Throwable t) {
				// IllegalArgumentException IllegalAccessException InvocationTargetException
				onError(entry, event, t);
			}
		}
	}


	private void onError(final MethodEntry entry, final Event event, final Throwable t) {
		final String descr = "GenericListener<" + clazz.getName() +"> @" + priority +" encountered an exception for " + entry.listener.getClass().getName() + " with method " + entry.method.toGenericString();
		try{
			final EventException e = new EventException(t, descr);
			// TODO: log it / more details!
			if (event.isAsynchronous()) LogUtil.scheduleLogSevere(e);
			else LogUtil.logSevere(e);
		}
		catch (Throwable t2){
			LogUtil.scheduleLogSevere("Could not log exception: " + descr);
		}
	}

	public void register(Plugin plugin) {
		if (registered) return;
		Bukkit.getPluginManager().registerEvent(clazz, this, priority, this, plugin, false);
		registered = true;
	}

	public boolean isRegistered() {
		return registered;
	}
	
	public void addMethodEntry(final MethodEntry entry){
		// TODO: maybe optimize later.
		// MethodOrder: the new enties order will be compared versus the old entries tags, not the other way round !).
		final MethodEntry[] entries = this.entries;
		int insertion = -1;
		if (entry.order != null){
			if (entry.order.beforeTag != null){
				if ("*".equals(entry.order.beforeTag)){
					insertion = 0;
				}
				else{
					for (int i = 0; i < entries.length; i++){
						MethodEntry other = entries[i];
						if (other.order != null){
							if (other.tag.matches(entry.order.beforeTag)){
								insertion = i;
								break;
							}
						}
					}
				}
			}
		}
		final MethodEntry[] newEntries;
		if (insertion == entries.length || insertion == -1){
			newEntries = Arrays.copyOf(entries, entries.length + 1);
			newEntries[newEntries.length - 1] = entry;
		}
		else{
			newEntries = new MethodEntry[entries.length + 1];
			for (int i = 0; i < newEntries.length; i ++ ){
				if (i < insertion) newEntries[i] = entries[i];
				else if (i == insertion) newEntries[i] = entry;
				else{
					// i > insertion
					newEntries[i] = entries[i - 1];
				}
			}
		}
		Arrays.fill(entries, null);
		this.entries = newEntries;
	}

	/**
	 * TODO: more methods for tags ? (....return type ?)
	 * @param listener
	 */
	public void remove(Listener listener) {
		final MethodEntry[] entries = this.entries;
		final List<MethodEntry> keep = new ArrayList<MethodEntry>(entries.length);
		for (MethodEntry entry : entries){
			if (entry.listener != listener) keep.add(entry);
		}
		if (keep.size() != entries.length){
			final MethodEntry[] newEntries = new MethodEntry[keep.size()];
			keep.toArray(newEntries);
			Arrays.fill(entries, null);
			this.entries = newEntries;
		}
	}

	/**
	 * Remove all MethodEntry references.
	 */
	public void clear() {
		MethodEntry[] oldEntries = this.entries;
		this.entries = new MethodEntry[0];
		for (int i = 0; i < oldEntries.length; i++){
			oldEntries[i] = null;
		}
	}

	/**
	 * If any methods are actually registered.
	 * @return
	 */
	public boolean hasListenerMethods() {
		return entries.length > 0;
	}
}
