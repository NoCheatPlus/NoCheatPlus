package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.ActionData;
import fr.neatmonster.nocheatplus.actions.types.penalty.CancelPenalty;
import fr.neatmonster.nocheatplus.actions.types.penalty.Penalty;
import fr.neatmonster.nocheatplus.actions.types.penalty.PenaltyAction;
import fr.neatmonster.nocheatplus.actions.types.penalty.PenaltyNode;

/**
 * Sole purpose is to indicate that an action is to be cancelled 100% (as
 * opposed to a penalty with 30%cancel). This effects A cancel action might
 * mean:
 * <ul>
 * <li>Cancel an event.</li>
 * <li>Undo actions by players in some other way.</li>
 * <li>Do something that should've been done (enforce).</li>
 * </ul>
 */
public class CancelAction<D extends ActionData, L extends AbstractActionList<D, L>> extends PenaltyAction<D, L> {
    
    // TODO: Deprecate this (let it extend penalty.CancelAction)?
    
    private static final Penalty cancelPenalty = new CancelPenalty();
    private static final PenaltyNode node = new PenaltyNode(null, cancelPenalty);

    /**
     * Default cancel action.
     */
    public CancelAction() {
        super(null, node);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.actions.Action#execute(fr.neatmonster.nocheatplus.checks.ViolationData)
     */
    @Override
    public void execute(final D data) {
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "cancel";
    }
}
