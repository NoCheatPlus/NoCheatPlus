/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.neatmonster.nocheatplus.config.ConfigFileWithActions;
import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;

public abstract class AbstractActionList<D extends ActionData,  L extends AbstractActionList<D, L>>{


    public static interface ActionListFactory<D extends ActionData,  L extends AbstractActionList<D, L>>{
        /**
         * 
         * @param permissionSilent
         *            The permission to bypass log actions.
         * @return
         */
        public L getNewActionList(RegisteredPermission permissionSilent);
    }

    /** Something to return if nothing is set. */
    protected static final Action<?, ?>[] emptyArray = new Action[0];

    /** This is a very bad design decision, but it's also really convenient to define this here. */
    public final RegisteredPermission                  permissionSilent;

    /** The actions of this AbstractActionList, "bundled" by threshold (violation level). */
    private final Map<Integer, Action<D, L>[]> actions    = new HashMap<Integer, Action<D, L>[]>();

    /** The thresholds of this list. **/
    protected final List<Integer>          thresholds = new ArrayList<Integer>();

    protected final ActionListFactory<D, L> listFactory;

    /**
     * Instantiates a new action list.
     * 
     * @param permissionSilent
     *            The permission to to bypass logging actions.
     */
    public AbstractActionList(final RegisteredPermission permissionSilent, final ActionListFactory<D, L> listFactory) {
        this.listFactory = listFactory;
        this.permissionSilent = permissionSilent;
    }

    /**
     * Get a list of actions that match the violation level. The only method that has to be called by a check.
     * 
     * @param violationLevel
     *            the violation level that should be matched
     * @return the array of actions whose threshold was closest to the violation level but not bigger
     */
    @SuppressWarnings("unchecked")
    public Action<D, L>[] getActions(final double violationLevel) {
        Integer result = null;

        for (final Integer threshold : thresholds)
            if (threshold <= violationLevel)
                result = threshold;

        if (result != null)
            return actions.get(result);
        else
            return (Action<D, L>[]) emptyArray;
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
     * Add an entry to this AbstractActionList. The list will be sorted by thresholds automatically after the insertion.
     * 
     * @param threshold
     *            the minimum violation level a player needs to have to be suspected to the given actions
     * @param actions
     *            the actions that will be used if the player reached the accompanying threshold/violation level
     */
    public void setActions(final Integer threshold, final Action<D, L>[] actions) {
        if (!thresholds.contains(threshold)) {
            thresholds.add(threshold);
            Collections.sort(thresholds);
        }

        this.actions.put(threshold, actions);
    }

    /**
     * Return a copy of this list, but optimize it, i.e. remove entries that are
     * never called, possibly do other optimizations which based on the specific
     * configuration.
     * 
     * @param config
     *            Configuration to adapt to.
     * @return Optimized AbstractActionList, individual Actions can be identical
     *         instances, altered Action instances must always be new instances,
     *         arrays are always new arrays.
     */
    public L getOptimizedCopy(final ConfigFileWithActions<D, L> config) {
        final L newList = listFactory.getNewActionList(permissionSilent);
        for (final Entry<Integer, Action<D, L>[]> entry : actions.entrySet()){
            final Integer t = entry.getKey();
            final Action<D, L>[] a = getOptimizedCopy(config, t, entry.getValue());
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
    public Action<D, L>[] getOptimizedCopy(final ConfigFileWithActions<D, L> config, final Integer threshold, final Action<D, L>[] actions)
    {
        if (actions == null || actions.length == 0) {
            return null;
        }
        final ArrayList<Action<D, L>> optimized = new ArrayList<Action<D, L>>();
        for (final Action<D, L> action : actions){
            final Action<D, L> optAction = action.getOptimizedCopy(config, threshold);
            if (optAction != null) {
                optimized.add(optAction);
            }
        }
        if (optimized.isEmpty()) {
            return null;
        }
        @SuppressWarnings("unchecked")
        final Action<D, L>[] optActions = (Action<D, L>[]) new Action<?, ?>[optimized.size()];
        optimized.toArray(optActions);
        return optActions;
    }
}
