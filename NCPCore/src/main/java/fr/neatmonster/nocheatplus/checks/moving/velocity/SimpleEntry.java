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

    /** Count down for invalidation. */
    public int actCount;

    // TODO: Add more conditions (max tick, real time ?)

    public SimpleEntry(double value, int actCount, int valCount){
        this.tick = TickTask.getTick();
        this.value = value;
        this.actCount = actCount;
    }

    public SimpleEntry(int tick, double value, int actCount, int valCount){
        this.tick = tick;
        this.value = value;
        this.actCount = actCount;
    }

    public String toString(){
        return "SimpleEntry(tick=" + tick + " value=" + value + " activate=" + actCount + ")";
    }

}
