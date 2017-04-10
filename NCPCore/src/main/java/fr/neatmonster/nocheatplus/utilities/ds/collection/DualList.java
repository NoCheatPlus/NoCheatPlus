package fr.neatmonster.nocheatplus.utilities.ds.collection;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Use LinkedList internally.
 * 
 * @author asofold
 *
 * @param <T>
 */
public class DualList<T> extends DualCollection<T, List<T>> {

    public DualList() {
        super();
    }

    public DualList(Lock lock) {
        super(lock);
    }

    @Override
    protected List<T> newCollection() {
        return new LinkedList<T>();
    }

}
