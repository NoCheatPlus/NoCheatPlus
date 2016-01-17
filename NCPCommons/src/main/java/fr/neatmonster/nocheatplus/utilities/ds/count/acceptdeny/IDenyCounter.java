package fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny;

/**
 * Count the number of times of denying something.
 * 
 * @author asofold
 *
 */
public interface IDenyCounter {

    /**
     * Increase the deny count. Propagate to parent, if any.
     */
    public void deny();

    /**
     * Get the number of times, that deny() has been called (since last reset,
     * if resettable).
     * 
     * @return
     */
    public int getDenyCount();

}
