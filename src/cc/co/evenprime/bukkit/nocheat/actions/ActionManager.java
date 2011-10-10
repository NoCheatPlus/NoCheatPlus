package cc.co.evenprime.bukkit.nocheat.actions;

import java.util.HashMap;
import java.util.Map;

import cc.co.evenprime.bukkit.nocheat.actions.types.Action;

/**
 * @author Evenprime
 * 
 */
public class ActionManager {

    private final Map<String, Action> actions;

    public ActionManager() {
        this.actions = new HashMap<String, Action>();
    }

    public void addAction(Action action) {
        
        this.actions.put(action.name.toLowerCase(), action);
    }

    public Action getAction(String actionName) {

        return this.actions.get(actionName.toLowerCase());
    }

    public Action[] getActions(String[] actionNames) {
        Action[] result = new Action[actionNames.length];
        
        for(int i = 0; i < actionNames.length; i++) {
            result[i] = getAction(actionNames[i]);
        }
        
        return result;
    }
}
