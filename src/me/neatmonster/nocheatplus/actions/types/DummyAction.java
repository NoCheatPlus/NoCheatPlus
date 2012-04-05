package me.neatmonster.nocheatplus.actions.types;

import me.neatmonster.nocheatplus.actions.Action;

/**
 * If an action can't be parsed correctly, at least keep it
 * stored in this form to not lose it when loading/storing the config file
 * 
 */
public class DummyAction extends Action {

    // The original string used for this action definition
    private final String def;

    public DummyAction(final String def) {
        super("dummyAction", 10000, 10000);
        this.def = def;
    }

    @Override
    public String toString() {
        return def;
    }
}
