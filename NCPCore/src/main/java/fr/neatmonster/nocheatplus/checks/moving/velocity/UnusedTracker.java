package fr.neatmonster.nocheatplus.checks.moving.velocity;

/**
 * Track unused velocity for one direction of one axis (absolute amounts only!).
 * 
 * @author asofold
 *
 */
public class UnusedTracker {

    // TODO: Using ticks for timing of these entries is problematic, should there be an extra sequence counter?
    // TODO: If the player is moving roughly at the speed of stored velocity anyway, things get foggy by default.

    /** Tick of the last time the direction had been blocked. */
    private int lastBlocked = 0;
    /**
     * Tick of the last time the direction had not been blocked, after it had
     * been blocked just before (start of phase).
     */
    private int lastNotBlockedStart = 0;

    // TODO: Some random / minimal data.
    private int resultUpdateCount = 0;
    private int resultViolationCount = 0;
    /** Absolute amount. */
    private double resultViolationAmount = 0.0;

    public void updateState(final int tick, final boolean blocked) {
        if (blocked) {
            lastBlocked = tick;
            // Blocked overrides not blocked.
            if (tick == lastNotBlockedStart) {
                lastNotBlockedStart--;
            }
        }
        else {
            // Only update not blocked tick, if this means a state change.
            // (Blocked overrides not blocked.)
            if (lastNotBlockedStart <= lastBlocked) {
                lastNotBlockedStart = tick;
            }
        }
        resultUpdateCount ++;
    }

    /**
     * 
     * @param tick Tick when the value had been added (not invalidated).
     * @param amount Absolute amount.
     */
    public void addValue(final int tick, final double amount) {
        if (amount < 0.0) {
            throw new IllegalArgumentException("The added amount must be greater than zero.");
        }
        // Test if really applicable.
        if (
                // Direction has been blocked since adding the entry.
                lastBlocked >= lastNotBlockedStart
                // Entry had been added too long ago, the direction might have been blocked between.
                || tick <= lastNotBlockedStart
                ) {
            return;
        }
        // Add to internals.
        addViolation(amount);
    }

    private void addViolation(final double amount) {
        resultViolationCount ++;
        resultViolationAmount += amount;
    }

    /**
     * Reset result counters, including the update count.
     */
    public void resetResults() {
        resultUpdateCount = 0;
        resultViolationCount = 0;
        resultViolationAmount = 0.0;
    }

    public int getResultUpdateCount() {
        return resultUpdateCount;
    }

    public int getResultViolationCount() {
        return resultViolationCount;
    }

    public double getResultViolationAmount() {
        return resultViolationAmount;
    }

}
