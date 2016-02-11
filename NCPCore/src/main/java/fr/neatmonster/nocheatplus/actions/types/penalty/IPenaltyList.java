package fr.neatmonster.nocheatplus.actions.types.penalty;

/**
 * Contain applicable penalty types that need to be handled outside of ViolationData.executeActions, for access by ViolationData.
 * 
 * @author asofold
 *
 */
public interface IPenaltyList {
    /**
     * Add an input-specific penalty.
     * @param penalty
     */
    public void addInputSpecificPenalty(InputSpecificPenalty penalty);
}
