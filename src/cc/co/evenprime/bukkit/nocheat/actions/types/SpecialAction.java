package cc.co.evenprime.bukkit.nocheat.actions.types;

import cc.co.evenprime.bukkit.nocheat.actions.Action;

/**
 * Do something check-specific. Usually that is to cancel the event, undo
 * something the player did, or do something the server should've done
 * 
 */
public class SpecialAction extends Action {

    public SpecialAction(String name, String parameters) {
        super(name, 0, 0);
    }

    public SpecialAction(String name, int delay, int repeat) {
        super(name, delay, repeat);
    }

    /**
     * Make a copy of the action, with some modifications
     * @param properties
     * @return
     */
    public Action cloneWithProperties(String properties) {
        String propertyFields[] = properties.split(":");

        int delay = Integer.parseInt(propertyFields[0]);
        int repeat = 5;
        if(propertyFields.length > 1)
            repeat = Integer.parseInt(propertyFields[1]);

        return new SpecialAction(name, delay, repeat);
    }
    
    @Override
    public String getProperties() {
        return delay + ":" + repeat;
    }
}
