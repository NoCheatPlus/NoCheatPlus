package fr.neatmonster.nocheatplus.actions.types.penalty;

/**
 * Minimal abstract implementation for player-specific effects.
 * @author asofold
 *
 */
public abstract class AbstractPlayerPenalty implements Penalty {

    /**
     * Always has player-specific effects.
     */
    @Override
    public boolean hasPlayerEffects() {
        return true;
    }

    /**
     * (Override to implement input-specific effects. Should prefer
     * AbstractGenericPenalty instead, though.)
     */
    @Override
    public boolean hasInputSpecificEffects() {
        return false;
    }

}
