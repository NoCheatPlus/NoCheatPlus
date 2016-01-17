package fr.neatmonster.nocheatplus.workaround;

import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.IAcceptDenyCounter;

/**
 * Workaround allowing for resetting pre/side-conditions, with an extra counter
 * lasting until reset. Parent counters may not be supported.
 * 
 * @author asofold
 *
 */
public interface IStagedWorkaround {

    // TODO: resetConditions: Consider to have it return false by default (x.use() || x.resetConditions() with pre-conditions).
    /**
     * Generic reset to the initial conditions. This does not reset the all time
     * counters, other effects depend on the implementation.
     */
    public void resetConditions();

    /**
     * Get the counter that will reset with the next call to resetConditions.
     * Parent counters may not be supported.
     * 
     * @return
     */
    public IAcceptDenyCounter getStageCounter();

}
