package fr.neatmonster.nocheatplus.components.registry.event;

/**
 * Somehow wrap an instance of a specified type.
 * 
 * @author asofold
 *
 * @param <T>
 */
public interface IHandle<T> {

    /**
     * Retrieve the currently stored instance.
     * 
     * @return
     */
    public T getHandle();

}
