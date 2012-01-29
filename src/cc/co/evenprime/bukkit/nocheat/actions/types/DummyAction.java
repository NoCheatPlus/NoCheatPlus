package cc.co.evenprime.bukkit.nocheat.actions.types;

import cc.co.evenprime.bukkit.nocheat.actions.Action;

/**
 * If an action can't be parsed correctly, at least keep it
 * stored in this form
 *
 */
public class DummyAction extends Action {

    private String def;

    public DummyAction(String def) {
        super("dummyAction", 10000, 10000);
        this.def = def;
    }

    public String toString() {
        return def;
    }
}
