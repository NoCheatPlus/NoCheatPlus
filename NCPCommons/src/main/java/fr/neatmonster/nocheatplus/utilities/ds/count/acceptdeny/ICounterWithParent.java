package fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny;

public interface ICounterWithParent {

    /**
     * Set a parent counter.
     * 
     * @param parent
     * @return This (counter) instance for chaining (not the previous parent).
     */
    public IAcceptDenyCounter setParentCounter(IAcceptDenyCounter parent);

    /**
     * Retrieve the parent counter.
     * 
     * @return
     */
    public IAcceptDenyCounter getParentCounter();

}
