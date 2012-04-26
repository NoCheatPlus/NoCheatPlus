package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.Action;

/**
 * Do something check-specific. Usually that is to cancel the event, undo
 * something the player did, or do something the server should've done
 * 
 */
public class SpecialAction extends Action {

    public SpecialAction() {
        super("cancel", 0, 0);
    }

    @Override
    public String toString() {
        return "cancel";
    }
}
