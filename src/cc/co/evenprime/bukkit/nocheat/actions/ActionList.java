package cc.co.evenprime.bukkit.nocheat.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A list of actions, that associates actions and a treshold. It allows to
 * retrieve all actions that match a certain treshold.
 * 
 * @author Evenprime
 * 
 */
public class ActionList {

    public ActionList() {}

    private final List<ActionListEntry> actionList = new ArrayList<ActionListEntry>();

    private class ActionListEntry implements Comparable<ActionListEntry> {

        public final ArrayList<String> actions = new ArrayList<String>();
        public final double            treshold;

        public ActionListEntry(double treshold, String[] actionNames) {

            this.treshold = treshold;

            for(String actionName : actionNames) {
                if(actionName != null && actionName.length() > 0) {
                    actions.add(actionName.toLowerCase());
                }
            }
        }

        @Override
        public int compareTo(ActionListEntry entry) {
            if(treshold < entry.treshold) {
                return -1;
            } else if(treshold == entry.treshold) {
                return 0;
            } else
                return 1;
        }
    }

    /**
     * Add an entry to this actionList. The list will be sorted by tresholds
     * automatically after the insertion.
     * 
     * @param treshold
     * @param actionNames
     */
    public void addEntry(double treshold, String[] actionNames) {
        this.actionList.add(new ActionListEntry(treshold, actionNames));
        Collections.sort(this.actionList);
    }

    /**
     * Get a list of actions that match the violation level.
     * The only method that has to be called by a check, besides
     * a call to Action
     * 
     * @param violationLevel
     * @return
     */
    public List<String> getActions(int vl) {

        ActionListEntry result = null;

        for(ActionListEntry entry : actionList) {
            if(entry.treshold <= vl) {
                result = entry;
            }
        }

        if(result != null)
            return result.actions;
        else
            return Collections.emptyList();
    }
}
