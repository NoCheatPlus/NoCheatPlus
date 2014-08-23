package fr.neatmonster.nocheatplus.checks.moving;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Velocity accounting for one axis (positive + negative direction).
 * @author dev1mc
 *
 */
public class AxisVelocity {
    
    private final List<Velocity> queued = new ArrayList<Velocity>();
    private final List<Velocity> active = new ArrayList<Velocity>();
    
    public void add(Velocity vel) {
        // TODO: Merging behavior?
        queued.add(vel);
    }
    
    public boolean hasActive() {
        return !active.isEmpty();
    }
    
    public boolean hasQueued() {
        return !queued.isEmpty();
    }
    
    /**
     * Called for moving events, increase age of velocity, decrease amounts, check which entries are invalid. Both horizontal and vertical.
     */
    public void tick() {
     // Decrease counts for active.
        // TODO: Actual friction. Could pass as an argument (special value for not to be used).
        // TODO: Consider removing already invalidated here.
        for (final Velocity vel : active) {
            vel.valCount --;
            vel.sum += vel.value;
            vel.value *= 0.93; // vel.frictionFactor;
            // (Altered entries should be kept, since they get used right away.)
        }
        // Decrease counts for queued.
        final Iterator<Velocity> it = queued.iterator();
        while (it.hasNext()) {
            it.next().actCount --;
        }
    }
    
    /**
     * Remove all velocity entries that are invalid. Checks both active and queued.
     * <br>(This does not catch invalidation by speed / direction changing.)
     * @param tick All velocity added before this tick gets removed.
     */
    public void removeInvalid(final int tick) {
        // TODO: Also merge entries here, or just on adding?
        Iterator<Velocity> it;
        // Active.
        it = active.iterator();
        while (it.hasNext()) {
            final Velocity vel = it.next();
            // TODO: 0.001 can be stretched somewhere else, most likely...
            // TODO: Somehow use tick here too (actCount, valCount)?
            if (vel.valCount <= 0 || vel.value <= 0.001) {
//              System.out.prsintln("Invalidate active: " + vel);
                it.remove();
            }
        }
        // Queued.
        it = queued.iterator();
        while (it.hasNext()) {
            final Velocity vel = it.next();
            if (vel.actCount <= 0 || vel.tick < tick) {
//              System.out.println("Invalidate queued: " + vel);
                it.remove();
            }
        }
    }
    
    /**
     * Get the sum of active velocity values.
     * @return
     */
    public double getFreedom() {
     // TODO: model/calculate it as accurate as possible...
        double f = 0;
        for (final Velocity vel : active) {
            f += vel.value;
        }
        return f;
    }
    
    /**
     * Use all queued velocity until at least amount is matched.
     * Amount is the horizontal distance that is to be covered by velocity (active has already been checked).
     * <br>
     * If the modeling changes (max instead of sum or similar), then this will be affected.
     * @param amount The amount used.
     * @return
     */
    public double use(final double amount) {
        final Iterator<Velocity> it = queued.iterator();
        double used = 0;
        while (it.hasNext()) {
            final Velocity vel = it.next();
            used += vel.value;
            active.add(vel);
            it.remove();
            if (used >= amount) {
                break;
            }
        }
        // TODO: Add to sum.
        return used;
    }
    
    public void clearActive() {
        active.clear();
    }
    
    /**
     * Clear active and queued velocity entries.
     */
    public void clear() {
        queued.clear();
        active.clear();
    }
    
    /**
     * Debugging.
     * @param builder
     */
    public void AddQueued(StringBuilder builder) {
        addVeloctiy(builder, queued);
    }
    
    /**
     * Debugging.
     * @param builder
     */
    public void addActive(StringBuilder builder) {
        addVeloctiy(builder, active);
    }
    
    /**
     * Debugging.
     * @param builder
     * @param entries
     */
    private void addVeloctiy(final StringBuilder builder, final List<Velocity> entries) {
        for (final Velocity vel: entries) {
             builder.append(" ");
             builder.append(vel);
        }
    }
    
}
