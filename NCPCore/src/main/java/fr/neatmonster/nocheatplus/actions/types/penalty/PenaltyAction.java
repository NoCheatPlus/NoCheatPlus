package fr.neatmonster.nocheatplus.actions.types.penalty;

import java.util.Collection;

import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionData;

/**
 * Penalty action, referencing a certain penalty defined elsewhere. <br>
 * Contained penalties are executed if passing the probability test, possibly in
 * an uncertain order, concerning:
 * <ul>
 * <li>Cancel penalties are always directly evaluated and applied to the context
 * of checking, e.g. the event that is being processed.</li>
 * <li>
 * Player-specific penalties are always executed within
 * ViolationData.executeActions in the primary thread. Note that for
 * off-primary-thread checks, this will be within the the TickTask.</li>
 * <li>Penalties that need specific extra input might be processed in different
 * places, e.g. the event listener, likely after executeActions has been called,
 * no guarantee.</li>
 * </ul>
 * 
 * @author asofold
 *
 * @param <D>
 * @param <L>
 */
public class PenaltyAction<D extends ActionData, L extends AbstractActionList<D, L>> extends Action<D, L> {

    private final PenaltyNode rootNode;
    private final String penaltyId;

    /**
     * 
     * @param penaltyId
     *            If null, toString should be overridden.
     * @param rootNode
     */
    public PenaltyAction(String penaltyId, PenaltyNode rootNode) {
        // TODO: name vs toString (!).
        super("penalty", 0, 0);
        this.rootNode = rootNode;
        this.penaltyId = penaltyId;

    }

    /**
     * Fill in applicable penalties to results.
     * 
     * @param results
     */
    public void evaluate(final Collection<Penalty> results) {
        if (this.rootNode != null) {
            this.rootNode.evaluate(results);
        }
    }

    @Override
    public void execute(D violationData) {
        // Does nothing (!).
    }

    @Override
    public String toString() {
        return "penalty:" + penaltyId;
    }

}
