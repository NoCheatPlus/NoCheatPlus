package fr.neatmonster.nocheatplus.components.registry.event;

import fr.neatmonster.nocheatplus.components.registry.GenericInstanceRegistry;

/**
 * Default implementation for retrieving a IGenericInstanceHandle from a
 * registry.
 * 
 * @author asofold
 *
 * @param <T>
 */
public class GenericInstanceHandle<T> implements IGenericInstanceRegistryListener<T>, IGenericInstanceHandle<T> {

    // TODO: <? extends T> ?
    // TODO: Might move to NCPPlugin, or later split (mostly) interface based api from default implementations.

    private GenericInstanceRegistry registry;
    private IUnregisterGenericInstanceListener unregister;
    private Class<T> registeredFor;
    private T handle = null;
    private boolean initialized = false;
    private boolean disabled = false;

    // TODO: Remove method?

    /**
     * Note that this doesn't register with the registry, as the registry may
     * return unique handles on request rather.
     * 
     * @param registeredFor
     * @param registry
     * @param unregister
     */
    public GenericInstanceHandle(Class<T> registeredFor, GenericInstanceRegistry registry, IUnregisterGenericInstanceListener unregister) {
        this.registry = registry;
        this.unregister = unregister;
        this.registeredFor = registeredFor; 
    }

    private T fetchHandle() {
        return registry.getGenericInstance(registeredFor);
    }

    @Override
    public void onGenericInstanceRegister(Class<T> registerFor, T instance) {
        this.handle = instance;
        initialized = true;
    }

    @Override
    public void onGenericInstanceOverride(Class<T> registerFor, T newInstance, T oldInstance) {
        this.handle = newInstance;
        initialized = true;
    }

    @Override
    public void onGenericInstanceRemove(Class<T> registerFor, T oldInstance) {
        this.handle = null;
        initialized = true;
    }

    @Override
    public T getHandle() {
        if (initialized) {
            return handle;
        }
        else if (disabled) {
            throw new RuntimeException("Already disabled.");
        }
        else {
            return fetchHandle();
        }
    }

    @Override
    public void disableHandle() {
        if (unregister != null) {
            disabled = true;
            initialized = false;
            handle = null;
            registeredFor = null;
            registry = null;
            unregister.unregisterGenericInstanceListener(registeredFor, this);
            unregister = null;
        }
    }

}
