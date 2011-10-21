package cc.co.evenprime.bukkit.nocheat.config.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.co.evenprime.bukkit.nocheat.actions.types.Action;

/**
 * A list of actions, that associates actions and a treshold. It allows to
 * retrieve all actions that match a certain treshold.
 * 
 */
public class ActionList {

    public ActionList() {}

    private final static Action[]        emptyArray = new Action[0];

    private final Map<Integer, Action[]> actions    = new HashMap<Integer, Action[]>();
    private final List<Integer>          tresholds  = new ArrayList<Integer>();

    /**
     * Add an entry to this actionList. The list will be sorted by tresholds
     * automatically after the insertion.
     * 
     * @param treshold
     * @param actionNames
     */
    public void setActions(Integer treshold, Action[] actions) {

        if(!this.tresholds.contains(treshold)) {
            this.tresholds.add(treshold);
            Collections.sort(this.tresholds);
        }

        this.actions.put(treshold, actions);
    }

    /**
     * Get a list of actions that match the violation level.
     * The only method that has to be called by a check, besides
     * a call to Action
     * 
     * @param violationLevel
     * @return
     */
    public Action[] getActions(int vl) {

        Integer result = null;

        for(Integer treshold : tresholds) {
            if(treshold <= vl) {
                result = treshold;
            }
        }

        if(result != null)
            return actions.get(result);
        else
            return emptyArray;
    }

    public List<Integer> getTresholds() {
        return tresholds;
    }
}
