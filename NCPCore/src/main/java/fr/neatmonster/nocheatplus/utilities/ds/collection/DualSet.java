package fr.neatmonster.nocheatplus.utilities.ds.collection;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * Use LinkedHashSet internally.
 * 
 * @author asofold
 *
 * @param <T>
 */
public class DualSet<T> extends DualCollection<T, Set<T>>{

    public DualSet() {
        super();
    }

    public DualSet(Lock lock) {
        super(lock);
    }

    @Override
    protected Set<T> newCollection() {
        return new LinkedHashSet<T>();
    }

}
