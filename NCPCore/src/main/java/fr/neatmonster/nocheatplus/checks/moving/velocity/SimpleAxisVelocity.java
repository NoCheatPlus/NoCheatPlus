package fr.neatmonster.nocheatplus.checks.moving.velocity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple per-axis velocity (positive + negative), only accounting for queuing
 * and invalidation. Since entries just store values for one time use, no extra
 * ticking is done, invalidation mechanics and activation count decreasing takes
 * place in removeInvalid.
 * 
 * @author asofold
 *
 */
public class SimpleAxisVelocity {

    private final List<SimpleEntry> queued = new LinkedList<SimpleEntry>();

    public void add(SimpleEntry entry) {
        queued.add(entry);
    }

    public boolean hasQueued() {
        return !queued.isEmpty();
    }

    /**
     * The value of the first matching entry. Returns 0.0 if no entry
     * is available. This will directly invalidate leading entries with the
     * wrong sign.
     * 
     * @param amount
     * @return
     */
    public double use(final double amount) {
        final Iterator<SimpleEntry> it = queued.iterator();
        final double absAmount = Math.abs(amount);
        while (it.hasNext()) {
            final SimpleEntry entry = it.next();
            it.remove();
            if (absAmount <= Math.abs(entry.value)) {
                if (amount > 0.0 && entry.value > 0.0 || amount < 0.0 && entry.value < 0.0) {
                    // Success. Note that 0.0 Entries are ignored (should not exist anyway).
                    return entry.value;
                } // else: Wrong sign.
            } // else: Value too small.
            // (Entry can not be used.)
            // TODO: Note unused velocity.
        }
        // None found.
        return 0.0;
    }

    /**
     * 
     * @return The value of the first matching positive entry. Returns 0.0 if no
     *         entry is available. This will directly invalidate leading entries
     *         with the wrong sign.
     */
    public double usePositive() {
        return use(Double.MIN_VALUE);
    }

    /**
     * Use any negative amount.
     * 
     * @return The value of the first matching negative entry. Returns 0.0 if no
     *         entry is available. This will directly invalidate leading entries
     *         with the wrong sign.
     */
    public double useNegative() {
        return use(-Double.MIN_VALUE);
    }

    /**
     * Remove all entries that have been added before the given tick, or for which the activation count has reached 0.
     * @param tick
     */
    public void removeInvalid(final int tick) {
        final Iterator<SimpleEntry> it = queued.iterator();
        while (it.hasNext()) {
            final SimpleEntry entry = it.next();
            entry.actCount --; // Let others optimize this.
            if (entry.actCount <= 0 || entry.tick < tick) {
                it.remove();
            }
        }
    }

    public void clear() {
        queued.clear();
    }

}
