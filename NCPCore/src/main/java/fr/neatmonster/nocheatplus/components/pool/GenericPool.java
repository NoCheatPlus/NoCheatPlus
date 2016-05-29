package fr.neatmonster.nocheatplus.components.pool;

/**
 * A generic pool allowing to get instances and return to be pooled for
 * efficiency. These are meant fail-safe, unless stated otherwise. So extra
 * conditions like maximum number of instances in use must be specified by the
 * implementation.
 * 
 * @author asofold
 *
 * @param <O>
 */
public interface GenericPool <O> {

    /**
     * Get an instance.
     * @return
     */
    public O getInstance();

    /**
     * Return an instance to be returned on getInstance later on.
     * 
     * @param instance
     * 
     * @throws NullPointerException
     *             if instance is null.
     */
    public void returnInstance(O instance);

}
