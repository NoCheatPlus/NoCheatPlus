package fr.neatmonster.nocheatplus.checks.moving.velocity;

import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * Simple check-once velocity.
 * @author asofold
 *
 */
public class SimpleEntry {

    /** Tick at which velocity got added. */
    public final int tick;

    /** The amount of velocity. */
    public final double value;

    /** Initial value for the actCount. */
    public final int initialActCount;

    /** Count down for invalidation. */
    public int actCount;

    // TODO: Add more conditions (max tick, real time ?)

    public SimpleEntry(double value, int actCount){
        this(TickTask.getTick(), value, actCount);
    }

    public SimpleEntry(int tick, double value, int actCount){
        this.tick = tick;
        this.value = value;
        this.actCount = actCount;
        this.initialActCount = actCount;
    }

    public String toString(){
        return "SimpleEntry(tick=" + tick + " value=" + value + " activate=" + actCount + "/" + initialActCount + ")";
    }

}
