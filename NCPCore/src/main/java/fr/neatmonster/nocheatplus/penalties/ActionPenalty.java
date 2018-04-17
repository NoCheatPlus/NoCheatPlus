package fr.neatmonster.nocheatplus.penalties;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;

public class ActionPenalty extends AbstractPenalty<ViolationData> {

    private final Action<ViolationData, ActionList> action;

    public ActionPenalty(Action<ViolationData, ActionList> action) {
        super(ViolationData.class);
        this.action = action;
    }

    @Override
    public boolean apply(ViolationData input) {
        input.executeAction(action);
        return true;
    }

}
