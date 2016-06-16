package fr.neatmonster.nocheatplus.components.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import fr.neatmonster.nocheatplus.components.registry.event.GenericInstanceHandle.ReferenceCountHandle;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceRegistryListener;
import fr.neatmonster.nocheatplus.components.registry.event.IUnregisterGenericInstanceListener;
import fr.neatmonster.nocheatplus.logging.details.IGetStreamId;
import fr.neatmonster.nocheatplus.logging.details.ILogString;

public class DefaultGenericInstanceRegistry implements GenericInstanceRegistry, IUnregisterGenericInstanceListener {

    // TODO: Test cases.

    /** Storage for generic instances registration. */
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

    /** Listeners for registry events. */
    private final Map<Class<?>, Collection<IGenericInstanceRegistryListener<?>>> listeners = new HashMap<Class<?>, Collection<IGenericInstanceRegistryListener<?>>>();

    /** Owned handles by the class they have been registered for. */
    private final Map<Class<?>, IGenericInstanceHandle<?>> uniqueHandles = new LinkedHashMap<Class<?>, IGenericInstanceHandle<?>>();

    /** Handles created within this class, that have to be detached. */
    private final Set<IGenericInstanceHandle<?>> ownedHandles = new LinkedHashSet<IGenericInstanceHandle<?>>();

    private ILogString logger = null;

    private IGetStreamId selectStream;

    private String logPrefix;

    public void setLogger(ILogString logger, IGetStreamId selectStream, String logPrefix) {
        this.logger = logger;
        this.selectStream = selectStream;
        this.logPrefix = logPrefix;
    }

    @Override
    public <T> void unregisterGenericInstanceListener(Class<T> registeredFor, IGenericInstanceHandle<T> listener) {
        Collection<IGenericInstanceRegistryListener<?>> registered = listeners.get(registeredFor);
        if (registered != null) {
            registered.remove(listener);
            if (registered.isEmpty()) {
                listeners.remove(registeredFor);
            }
        }
        if ((listener instanceof ReferenceCountHandle<?>) && ownedHandles.contains(listener)) {
            ownedHandles.remove(listener);
            uniqueHandles.remove(registeredFor);
            ((IGenericInstanceHandle<?>) listener).disableHandle();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T registerGenericInstance(T instance) {
        return registerGenericInstance((Class<T>) instance.getClass(), instance);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, TI extends T> T registerGenericInstance(Class<T> registerFor, TI instance) {
        T registered = getGenericInstance(registerFor);
        final boolean had = instances.containsKey(registerFor);
        instances.put(registerFor, instance);
        Collection<IGenericInstanceRegistryListener<?>> registeredListeners =  listeners.get(registerFor);
        if (registeredListeners != null) {
            for (IGenericInstanceRegistryListener<?> rawListener : registeredListeners) {
                if (had) {
                    ((IGenericInstanceRegistryListener<T>) rawListener).onGenericInstanceOverride(registerFor, instance, registered);
                }
                else {
                    ((IGenericInstanceRegistryListener<T>) rawListener).onGenericInstanceRegister(registerFor, instance);
                }
            }
        }
        if (had) {
            logRegistryEvent("Registered (override) for " + registerFor.getName() + ": " + instance.getClass().getName());
        }
        else {
            logRegistryEvent("Registered for " + registerFor.getName() + ": " + instance.getClass().getName());
        }
        return registered;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getGenericInstance(Class<T> registeredFor) {
        return (T) instances.get(registeredFor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unregisterGenericInstance(Class<T> registeredFor) {
        T registered = getGenericInstance(registeredFor); // Convenience.
        final boolean had = instances.containsKey(registeredFor);
        instances.remove(registeredFor);
        Collection<IGenericInstanceRegistryListener<?>> registeredListeners =  listeners.get(registeredFor);
        if (registeredListeners != null) {
            for (IGenericInstanceRegistryListener<?> rawListener : registeredListeners) {
                ((IGenericInstanceRegistryListener<T>) rawListener).onGenericInstanceRemove(registeredFor, registered);
            }
        }
        if (had) {
            logRegistryEvent("Unregister, remove mapping for: " + registeredFor.getName());
        }
        else {
            logRegistryEvent("Unregister, no mapping present for: " + registeredFor.getName());
        }
        return registered;
    }

    @Override
    public <T> IGenericInstanceHandle<T> getGenericInstanceHandle(Class<T> registeredFor) {
        @SuppressWarnings("unchecked")
        ReferenceCountHandle<T> handle = (ReferenceCountHandle<T>) uniqueHandles.get(registeredFor);
        if (handle == null) {
            handle = new ReferenceCountHandle<T>(registeredFor, this, this);
            ownedHandles.add(handle);
            uniqueHandles.put(registeredFor, handle);
            Collection<IGenericInstanceRegistryListener<?>> registered = listeners.get(registeredFor);
            if (registered == null) {
                registered = new HashSet<IGenericInstanceRegistryListener<?>>();
                listeners.put(registeredFor, registered);
            }
            registered.add((IGenericInstanceRegistryListener<?>) handle);
        }
        // else: no need to register.
        return handle.getNewHandle();
    }

    public void clear() {
        instances.clear();
        listeners.clear();
        // TODO: consider fire unregister or add a removal method ?
        // Force detach all handles.
        for (IGenericInstanceHandle<?> handle : new ArrayList<IGenericInstanceHandle<?>>(ownedHandles)) {
            handle.disableHandle();
        }
        ownedHandles.clear();
        logRegistryEvent("Registry cleared.");
    }

    protected void logRegistryEvent(String message) {
        if (logger != null) {
            logger.info(selectStream.getStreamId(), logPrefix == null ? message : logPrefix + message);
        }
    }

}
