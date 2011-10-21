package cc.co.evenprime.bukkit.nocheat.config.util;

import java.util.HashMap;
import java.util.Map;

import cc.co.evenprime.bukkit.nocheat.actions.types.Action;

/**
 * 
 */
public class ActionMapper {

    private final Map<String, Action> actions;

    public ActionMapper() {
        this.actions = new HashMap<String, Action>();
    }

    public void addAction(Action action) {

        this.actions.put(action.name.toLowerCase(), action);
    }

    public Action[] getActions(String[] actionNames) {
        Action[] result = new Action[actionNames.length];

        for(int i = 0; i < actionNames.length; i++) {
            result[i] = this.actions.get(actionNames[i].toLowerCase());
        }

        return result;
    }
}
