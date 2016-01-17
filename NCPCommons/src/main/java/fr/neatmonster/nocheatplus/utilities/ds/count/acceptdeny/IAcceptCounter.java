package fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny;

/**
 * Count the number of times of accepting something.
 * 
 * @author asofold
 *
 */
public interface IAcceptCounter {

    /**
     * Increase the accept count. Propagate to parent (if any).
     */
    public void accept();

    /**
     * Get the number of times, that accept() has been called (since last reset,
     * if resettable).
     * 
     * @return
     */
    public int getAcceptCount();

}
