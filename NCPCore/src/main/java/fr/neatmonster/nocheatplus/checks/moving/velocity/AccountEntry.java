package fr.neatmonster.nocheatplus.checks.moving.velocity;

import fr.neatmonster.nocheatplus.utilities.TickTask;


/**
 * One entry of velocity for a player, including fields for maintaining margins
 * and effects per entry.
 * 
 * @author mc_dev
 *
 */
public class AccountEntry {

    /** Tick at which velocity got added. */
    public final int tick;

    /** The amount of velocity, decreasing with use. */
    public double value;

    /**
     * "Some sum" for general purpose. <li>For vertical entries this is used to
     * alter the allowed y-distance to the set-back point.</li>
     */
    public double sum = 0.0;

    ///////////////////////////
    // Activation conditions.
    ///////////////////////////

    // TODO: Add more conditions (max tick, real time?)

    /**
     * Maximum count before activation.
     */
    public int actCount;

    ///////////////////////////
    // Invalidation conditions.
    ///////////////////////////

    // TODO: Add more conditions (max tick, real time?)

    /**
     * Count .
     */
    public int valCount;

    public AccountEntry(double value, int actCount, int valCount){
        this.tick = TickTask.getTick();
        this.value = value;
        this.actCount = actCount;
        this.valCount = valCount;
    }

    public AccountEntry(int tick, double value, int actCount, int valCount){
        this.tick = tick;
        this.value = value;
        this.actCount = actCount;
        this.valCount = valCount;
    }

    public String toString(){
        return "AccountEntry(tick=" + tick + " sum=" + sum + " value=" + value + " valid=" + valCount + " activate=" + actCount + ")";
    }

}
