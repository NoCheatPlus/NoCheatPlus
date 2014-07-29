package fr.neatmonster.nocheatplus.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Allow to sort by multiple criteria, first come first serve.
 * @author dev1mc
 *
 */
public class FCFSComparator <T> implements Comparator<T> {
    
    private final List<Comparator<T>> comparators;
    private final boolean reverse;
    
    public FCFSComparator(Collection<Comparator<T>> comparators) {
        this(comparators, false);
    }
    
    public FCFSComparator(Collection<Comparator<T>> comparators, boolean reverse) {
        this.comparators = new ArrayList<Comparator<T>>(comparators);
        this.reverse = reverse;
    }

    @Override
    public int compare(T o1, T o2) {
        for (int i = 0; i < comparators.size(); i++) {
            final int res = comparators.get(i).compare(o1, o2);
            if (res != 0) {
                return reverse ? -res : res;
            }
        }
        return 0;
    }

}
