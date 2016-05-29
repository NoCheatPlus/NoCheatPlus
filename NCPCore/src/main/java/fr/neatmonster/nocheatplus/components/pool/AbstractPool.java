package fr.neatmonster.nocheatplus.components.pool;

import java.util.ArrayList;

public abstract class AbstractPool <O> implements GenericPool <O> {

    private final int maxPoolSize;
    private final ArrayList<O> pool;

    /**
     * 
     * @param maxPoolSize
     *            A value <= 0 means that all elements are pooled always.
     */
    protected AbstractPool(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        pool = new ArrayList<O>(Math.max(10, maxPoolSize));
    }

    @Override
    public O getInstance() {
        if (!pool.isEmpty()) {
            return pool.remove(pool.size() - 1); // Prevent re-size.
        }
        else {
            return newInstance();
        }
    }

    @Override
    public void returnInstance(O instance) {
        if (instance == null) {
            throw new NullPointerException("The passed instance must not be null.");
        }
        if (maxPoolSize <= 0 || pool.size() < maxPoolSize) {
            pool.add(instance);
        }
        else {
            // GC.
        }
    }

    /**
     * Get a new instance to return directly.
     * @return
     */
    protected abstract O newInstance();

}
