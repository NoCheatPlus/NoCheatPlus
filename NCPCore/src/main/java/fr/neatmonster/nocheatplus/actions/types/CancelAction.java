package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionData;

/**
 * Do something check-specific. Usually that is to cancel the event, undo something the player did, or do something the
 * server should've done.
 */
public class CancelAction<D extends ActionData, L extends AbstractActionList<D, L>> extends Action<D, L> {

    /**
     * Default cancel action.
     */
    public CancelAction() {
        this("cancel");
    }

    /**
     * Sub-class constructor.
     * @param name
     */
    public CancelAction(String name) {
        super(name, 0, 0);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.actions.Action#execute(fr.neatmonster.nocheatplus.checks.ViolationData)
     */
    @Override
    public boolean execute(final D data) {
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "cancel";
    }
}
