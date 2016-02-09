package fr.neatmonster.nocheatplus.actions.types.penalty;

/**
 * Penalty for one type of input.
 * @author asofold
 *
 * @param <RI>
 */
public interface GenericPenalty<RI> extends InputSpecificPenalty {

    // TODO: Might directly put this into AbstractGenericPenalty.

    /**
     * Get the class that determines the accepted input type.
     * 
     * @return
     */
    public Class<RI> getRegisteredInput();

}
