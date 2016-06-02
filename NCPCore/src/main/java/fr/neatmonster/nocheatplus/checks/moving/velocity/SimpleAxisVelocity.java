package fr.neatmonster.nocheatplus.checks.moving.velocity;

import java.util.Iterator;
import java.util.LinkedList;

import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * Simple per-axis velocity (positive + negative), only accounting for queuing
 * and invalidation. Since entries just wrap values for one time use, no extra
 * ticking is done, invalidation mechanics and activation count decreasing takes
 * place in removeInvalid.
 * 
 * @author asofold
 *
 */
public class SimpleAxisVelocity {

    /** Margin for accepting a demanded 0.0 amount, regardless sign. */
    private static final double marginAcceptZero = 0.005;

    /** Size of queued for which to force cleanup on add. */
    private static final double thresholdCleanup = 10;

    private final LinkedList<SimpleEntry> queued = new LinkedList<SimpleEntry>();

    /** Activation flag for tracking unused velocity. */
    private boolean unusedActive = true;
    /**
     * Sensitivity for tracking unused velocity (absolute amount, counts for
     * positive and negative).
     */
    // TODO: Ignoring 0-dist velocity allows 'moving on', though.
    private double unusedSensitivity = 0.1;
    // TODO: Visibility of trackers, concept, etc.
    public final UnusedTracker unusedTrackerPos = new UnusedTracker();
    // TODO: Might do without tracking negative velocity.
    public final UnusedTracker unusedTrackerNeg = new UnusedTracker();

    /**
     * Add to the front of the queue.
     * @param entry
     */
    public void addToFront(SimpleEntry entry) {
        queued.addFirst(entry);
    }

    /**
     * Add to the end of the queue.
     * @param entry
     */
    public void add(SimpleEntry entry) {
        queued.add(entry);
        if (queued.size() > thresholdCleanup) {
            removeInvalid(TickTask.getTick());
        }
    }

    public boolean hasQueued() {
        return !queued.isEmpty();
    }

    /**
     * Use the next matching entry.
     * 
     * @param amount
     * @param tolerance
     *            Allow using entries with less amount (still sign-specific).
     *            Must be equal or greater than 0.0.
     * @return The first matching entry. Returns null if no entry is available.
     *         This will directly invalidate leading entries with the wrong
     *         sign.
     */
    public SimpleEntry use(final double amount, final double tolerance) {
        final Iterator<SimpleEntry> it = queued.iterator();
        while (it.hasNext()) {
            final SimpleEntry entry = it.next();
            it.remove();
            if (matchesEntry(entry, amount, tolerance)) {
                // Success.
                return entry;
            }
            else {
                // Track unused velocity.
                if (unusedActive) {
                    addUnused(entry);
                }
            }
        }
        // None found.
        return null;
    }

    /**
     * Check if the demanded amount can be covered by this velocity entry. Might
     * return an entry with a small value with a different sign, if amount is
     * set to 0.0. Needed also for testing stored entries.
     * 
     * @param entry
     * @param amount
     * @param tolerance
     *            Allow using entries with less amount (still sign-specific).
     *            Must be equal or greater than 0.0.
     * @return
     */
    public boolean matchesEntry(final SimpleEntry entry, final double amount, final double tolerance) {
        return Math.abs(amount) <= Math.abs(entry.value) + tolerance && 
                (amount > 0.0 && entry.value > 0.0 && amount <= entry.value + tolerance 
                || amount < 0.0 && entry.value < 0.0 && entry.value - tolerance <= amount 
                || amount == 0.0 && Math.abs(entry.value) <= marginAcceptZero);
    }

    /**
     * Remove all entries that have been added before the given tick, or for
     * which the activation count has reached 0.
     * 
     * @param tick
     */
    public void removeInvalid(final int tick) {
        // Note: clear invalidated here, append unused to invalidated.
        final Iterator<SimpleEntry> it = queued.iterator();
        while (it.hasNext()) {
            final SimpleEntry entry = it.next();
            entry.actCount --; // Let others optimize this.
            if (entry.actCount <= 0 || entry.tick < tick) {
                it.remove();
                // Track unused velocity.
                if (unusedActive) {
                    addUnused(entry);
                }
            }
        }
    }

    public void clear() {
        if (unusedActive && !queued.isEmpty()) {
            removeInvalid(TickTask.getTick());
        }
        queued.clear();
    }

    /**
     * Debugging.
     * 
     * @param builder
     */
    public void addQueued(final StringBuilder builder) {
        for (final SimpleEntry vel: queued) {
            builder.append(" ");
            builder.append(vel);
        }
    }

    // TODO: Might add the yDistance for a move here (and if to use it for external calls), but that needs a more complex modeling anyway!?
    public void updateBlockedState(final int tick, final boolean posBlocked, final boolean negBlocked) {
        // Store state, ignoring the activation flag.
        unusedTrackerPos.updateState(tick, posBlocked);
        unusedTrackerNeg.updateState(tick, negBlocked);
    }

    /**
     * Having checked the activation flag, call this with velocity entries, that
     * have just been invalidated.
     * 
     * @param entry
     */
    private void addUnused(final SimpleEntry entry) {
        // Global pre-conditions (sensitivity, skip entries that are supposed to be even more fake than default).
        if (Math.abs(entry.value) < unusedSensitivity || entry.initialActCount < 5) {
            return;
        }
        // Add to the tracker for the given direction.
        // TODO: Consider evaluating activation count + have a flag to ignore an entry.
        if (entry.value < 0.0) {
            // Negative value.
            unusedTrackerNeg.addValue(entry.tick, -entry.value); // Add absolute amount.
        }
        else {
            // Positive value.
            unusedTrackerPos.addValue(entry.tick, entry.value);
        }
    }

    public boolean isUnusedActive() {
        return unusedActive;
    }

    public void setUnusedActive(final boolean unusedActive) {
        this.unusedActive = unusedActive;
    }

}
