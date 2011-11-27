package cc.co.evenprime.bukkit.nocheat.actions.types;

/**
 * This is only used to not lose config entries in case an action isn't defined correctly
 *
 */
public class DummyAction extends Action {

    public DummyAction(String name, int delay, int repeat) {
        super(name, delay, repeat);
    }

}
