package fr.neatmonster.nocheatplus.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.neatmonster.nocheatplus.config.ConfigFile;


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
 * <hr>
 * TODO: refactor to an array of Actions entries (threshold + Action[]) + sort that one.
 */
public class ActionList {
	
	/** Something to return if nothing is set. */
	private static final Action[] emptyArray = new Action[0];

    /** This is a very bad design decision, but it's also really convenient to define this here. */
    public final String                  permissionSilent;

    /** The actions of this ActionList, "bundled" by treshold (violation level). */
    private final Map<Integer, Action[]> actions    = new HashMap<Integer, Action[]>();

    /** The thresholds of this list. **/
    private final List<Integer>          thresholds = new ArrayList<Integer>();

    /**
     * Instantiates a new action list.
     * 
     * @param permissionSilent
     *            the permission
     */
    public ActionList(final String permissionSilent) {
        this.permissionSilent = permissionSilent + ".silent";
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

	/**
	 * Return a copy of this list, but optimize it, i.e. remove entries that are
	 * never called, possibly do other optimizations which are possible given
	 * the specific configuration.
	 * 
	 * @param config Configuration to adapt to.
	 * @return Optimized ActionList, individual Actions can be identical instances, altered Action instances must always be new instances, arrays are always new arrays.
	 */
	public ActionList getOptimizedCopy(final ConfigFile config) {
		final ActionList newList = new ActionList(this.permissionSilent);
		for (final Entry<Integer, Action[]> entry : actions.entrySet()){
			final Integer t = entry.getKey();
			final Action[] a = getOptimizedCopy(config, t, entry.getValue());
			if (a != null && a.length > 0){
				newList.setActions(t, a);
			}
		}
		
		return newList;
	}

	/**
	 * Get an optimized copy of the Actions array, given the config in use.
	 * @param config
	 * @param threshold
	 * @param actions
	 * @return Copy with optimized entries, null or empty arrays are possible. Contained Actions might be identical to the given ones, just changed actions must be new instances to preserve consistency, Action instances are not to be altered.
	 */
	public Action[] getOptimizedCopy(final ConfigFile config, final Integer threshold, final Action[] actions)
	{
		if (actions == null || actions.length == 0) return null;
		final ArrayList<Action> optimized = new ArrayList<Action>();
		for (final Action action : actions){
			final Action optAction = action.getOptimizedCopy(config, threshold);
			if (optAction != null) optimized.add(optAction);
		}
		if (optimized.isEmpty()) return null;
		final Action[] optActions = new Action[optimized.size()];
		optimized.toArray(optActions);
		return optActions;
	}
}
