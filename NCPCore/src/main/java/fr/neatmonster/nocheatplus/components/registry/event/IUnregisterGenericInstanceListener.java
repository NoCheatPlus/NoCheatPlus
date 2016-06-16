package fr.neatmonster.nocheatplus.components.registry.event;

/**
 * Rather an internal interface.
 * 
 * @author asofold
 *
 */
public interface IUnregisterGenericInstanceListener {

    public <T> void unregisterGenericInstanceListener(Class<T> registeredFor, IGenericInstanceHandle<T> listener);

}
