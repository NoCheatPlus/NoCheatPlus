package fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny;

/**
 * Allow resetting counters.
 * 
 * @author asofold
 *
 */
public interface IResettableCounter {

    /**
     * Reset all contained counters. Not propagated to parent.
     * 
     */
    public void resetCounter();

}
