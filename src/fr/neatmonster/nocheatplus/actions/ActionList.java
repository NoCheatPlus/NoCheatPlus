package fr.neatmonster.nocheatplus.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
 * MMP"""""""MM            dP   oo                   M""MMMMMMMM oo            dP   
 * M' .mmmm  MM            88                        M  MMMMMMMM               88   
 * M         `M .d8888b. d8888P dP .d8888b. 88d888b. M  MMMMMMMM dP .d8888b. d8888P 
 * M  MMMMM  MM 88'  `""   88   88 88'  `88 88'  `88 M  MMMMMMMM 88 Y8ooooo.   88   
 * M  MMMMM  MM 88.  ...   88   88 88.  .88 88    88 M  MMMMMMMM 88       88   88   
 * M  MMMMM  MM `88888P'   dP   dP `88888P' dP    dP M         M dP `88888P'   dP   
 * MMMMMMMMMMMM                                      MMMMMMMMMMM                    
 */
/**
 * A list of actions, that associates actions to thresholds. It allows to retrieve all actions that match a certain
 * threshold.
 */
public class ActionList {

    /** This is a very bad design decision, but it's also really convenient to define this here. */
    public final String                  permissionSilent;

    /** If there are no actions registered, we still return an Array. It's just empty/size=0. */
    private final static Action[]        emptyArray = new Action[0];

    /** The actions of this ActionList, "bundled" by treshold (violation level). */
    private final Map<Integer, Action[]> actions    = new HashMap<Integer, Action[]>();

    /** The thresholds of this list. **/
    private final List<Integer>          thresholds = new ArrayList<Integer>();

    /**
     * Instantiates a new action list.
     * 
     * @param permission
     *            the permission
     */
    public ActionList(final String permission) {
        permissionSilent = permission + ".silent";
    }

    /**
     * Get a list of actions that match the violation level. The only method that has to be called by a check.
     * 
     * @param violationLevel
     *            the violation level that should be matched
     * @return the array of actions whose threshold was closest to the violation level but not bigger
     */
    public Action[] getActions(final double violationLevel) {
        Integer result = null;

        for (final Integer threshold : thresholds)
            if (threshold <= violationLevel)
                result = threshold;

        if (result != null)
            return actions.get(result);
        else
            return emptyArray;
    }

    /**
     * Get a sorted list of the thresholds/violation levels that were used in this list.
     * 
     * @return the sorted list of thresholds.
     */
    public List<Integer> getThresholds() {
        return thresholds;
    }

    /**
     * Add an entry to this actionList. The list will be sorted by thresholds automatically after the insertion.
     * 
     * @param threshold
     *            the minimum violation level a player needs to have to be suspected to the given actions
     * @param actions
     *            the actions that will be used if the player reached the accompanying threshold/violation level
     */
    public void setActions(final Integer threshold, final Action[] actions) {
        if (!thresholds.contains(threshold)) {
            thresholds.add(threshold);
            Collections.sort(thresholds);
        }

        this.actions.put(threshold, actions);
    }
}
