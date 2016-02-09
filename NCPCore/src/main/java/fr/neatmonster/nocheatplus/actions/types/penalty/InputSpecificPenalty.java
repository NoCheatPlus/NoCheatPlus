package fr.neatmonster.nocheatplus.actions.types.penalty;

public interface InputSpecificPenalty extends Penalty {

    /**
     * Apply the input-specific effects of a penalty, for other input than
     * Player.
     * 
     * @param input
     *            May be of unexpected type.
     * @param registeredInput
     */
    // TODO: Consider boolean result for "the input type was accepted", in order to detect if an input is not accepted by any generic penalty.
    public void apply(Object input);

}
