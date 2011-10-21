package cc.co.evenprime.bukkit.nocheat.actions.types;

/**
 * Do something check-specific. Usually that is to cancel the event, undo
 * something the player did, or do something the server should've done
 * 
 */
public class SpecialAction extends Action {

    public SpecialAction(String name, int delay, int repeat) {
        super(name, delay, repeat);
    }
}
