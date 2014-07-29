package fr.neatmonster.nocheatplus.utilities;

import java.util.Collection;
import java.util.Comparator;

/**
 * Allow to sort by multiple criteria, first come first serve.
 * @author dev1mc
 *
 */
public class FCFSComparator <T> implements Comparator<T> {
    
    private final Comparator<T>[] comparators;
    private final boolean reverse;
    
    public FCFSComparator(Collection<Comparator<T>> comparators) {
        this(comparators, false);
    }
    
    @SuppressWarnings("unchecked")
    public FCFSComparator(Collection<Comparator<T>> comparators, boolean reverse) {
        this.comparators = (Comparator<T>[]) comparators.toArray();
        this.reverse = reverse;
    }

    @Override
    public int compare(T o1, T o2) {
        for (int i = 0; i < comparators.length; i++) {
            final int res = comparators[i].compare(o1, o2);
            if (res != 0) {
                return reverse ? -res : res;
            }
        }
        return 0;
    }

}
