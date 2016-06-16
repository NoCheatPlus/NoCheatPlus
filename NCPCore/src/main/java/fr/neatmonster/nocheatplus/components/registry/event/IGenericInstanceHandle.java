package fr.neatmonster.nocheatplus.components.registry.event;

/**
 * Convenience to retrieve the currently registered instance. Note that
 * registrations by other plugins might be problematic, thus removing
 * registrations and stored IGenericInstanceHandle instances is within the
 * responsibility of the hooking plugin.
 * 
 * @author asofold
 *
 * @param <T>
 *            The type instances are registered for.
 */
public interface IGenericInstanceHandle<T> {

    // TODO: <? extends T> ?

    /**
     * Get the currently registered instance.
     * 
     * @return
     * @throws RuntimeException,
     *             if disableHandle has been called.
     */
    public T getHandle();

    /**
     * Unlink from the registry. Subsequent calls to getHandle will yield a
     * RuntimeException, while disableHandle can still be called without effect.
     * This may not be necessary, if the registration lasts during an entire
     * runtime, however if an object that holds IGenericInstanceHandle instances
     * gets overridden on reloading the configuration of the plugin, keeping
     * handles may leak a little bit of memory and increase CPU load with each
     * time such happens. Often changing registration is not a typical use-case.
     * This can not be undone.
     */
    public void disableHandle();

}
