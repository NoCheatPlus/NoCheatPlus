package fr.neatmonster.nocheatplus.workaround;

/**
 * Provide a means of controlling when workarounds should be able to apply,
 * enabling preconditions, mid-to-long-term side-conditions, as well as
 * statistics for how often a workaround has been used.
 * <hr>
 * The method use() must be called after all other preconditions have been met
 * for that (stage of) workaround, so that success means that the (stage of)
 * workaround does apply. The method canUse can be used to test if the
 * workaround would apply, aimed at cases where that is better performance-wise.
 * 
 * @author asofold
 *
 */
public interface Workaround {

    // TODO: getDiscardCount() ? 
    // TODO: Add setEnabled() ? Allow to configure workarounds.

    public String getId();

    /** The all-time use count. */
    public int getUseCount();

    /**
     * Attempt to use the workaround, considering all preconditions and
     * side-conditions set. This will increase the use count in case of
     * returning true, it might also alter/use other counters based on the value
     * to be returned.
     * 
     * @return If actually can be used.
     */
    public boolean use();

    /**
     * Test if this (stage of) workaround would apply, excluding checking for
     * parent state. This must not have any side effect nor change any data.
     * This should also not call parent.canUse, as that would lead to quadratic
     * time checking with a deeper hierarchy (likely it's just 1 deep).
     * 
     * @return
     */
    public boolean canUse();

    /**
     * Generic reset to the initial conditions. This does not reset the use
     * count, other effects depend on the implementation.
     */
    public void resetConditions();

    /**
     * Serving as factory, retrieve a new instance of the same kind, in the
     * default state (not clone).
     * 
     * @return
     */
    public Workaround getNewInstance();

}
