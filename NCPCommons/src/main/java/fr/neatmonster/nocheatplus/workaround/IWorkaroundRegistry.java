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
package fr.neatmonster.nocheatplus.workaround;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.IAcceptDenyCounter;

/**
 * An access point for fetching global WorkaroundCounter instances and a factory
 * for fetching new sets of per-player workarounds.
 * 
 * @author asofold
 *
 */
public interface IWorkaroundRegistry {

    /**
     * Convenience to retrieve any type of per-player Workaround by id, for the
     * case one doesn't want to store the registry and/or individual Workaround
     * implementations as members. Groups allow resetting certain types of
     * workarounds in bunches. The resetConditions methods only call
     * resetConditions for instances of IStagedWorkaround.
     * 
     * @author asofold
     *
     */
    public static class WorkaroundSet {

        // TODO: getUseCount()
        // TODO: A list of just used IStageWorkaround / maybe other extra, or a flag (reset externally).
        // TODO: Alternative: provide a use(Collection<String>) method to add the id to on accept.
        // TODO: Better optimized constructor (instanceof-decisions can be pre-cached).

        /** Map workaround id to workaround. */
        private final Map<String, IWorkaround> workaroundsById = new LinkedHashMap<String, IWorkaround>();

        /** Only the workarounds that might need resetting. */
        private final IStagedWorkaround[] stagedWorkarounds;

        // TODO: Consider to make accessible (flexible log/stats command) or remove keeping entire groups.
        /** Map groupId to workarounds. Set to null, if no groups are present. */
        private final Map<String, IWorkaround[]> groups;

        /** Only the staged workarounds within a group by group id. Set to null, if no groups are present. */
        private final Map<String, IStagedWorkaround[]> stagedGroups;

        /**
         * Collection of just used ids, only set in use(...).
         */
        private Collection<String> justUsedIds = null;

        /**
         * 
         * @param bluePrints
         * @param groups
         *            Map groupId to workaroundIds, groups may be null if none
         *            are set. All referenced workaround ids must be registered,
         *            workarounds can be in multiple groups.
         */
        public WorkaroundSet(final IWorkaround[] bluePrints, final Map<String, String[]> groups) {
            // Add new instances to workaroundsById and stagedWorkarounds.
            final ArrayList<IWorkaround> stagedWorkarounds = new ArrayList<IWorkaround>(bluePrints.length);
            for (int i = 0; i < bluePrints.length; i++) {
                final IWorkaround workaround = bluePrints[i].getNewInstance();
                workaroundsById.put(workaround.getId(), workaround);
                if (workaround instanceof IStagedWorkaround) {
                    stagedWorkarounds.add(workaround);
                }
            }
            this.stagedWorkarounds = stagedWorkarounds.toArray(new IStagedWorkaround[stagedWorkarounds.size()]);

            // Prepare fast to reset lists, if groups are given.
            if (groups != null) {
                this.groups = new HashMap<String, IWorkaround[]>();
                this.stagedGroups =  new HashMap<String, IStagedWorkaround[]>();
                for (final Entry<String, String[]> entry : groups.entrySet()) {
                    final String groupId = entry.getKey();
                    final String[] workaroundIds = entry.getValue();
                    final IWorkaround[] group = new IWorkaround[workaroundIds.length];
                    final ArrayList<IStagedWorkaround> stagedGroup = new ArrayList<IStagedWorkaround>(workaroundIds.length);
                    for (int i = 0; i < workaroundIds.length; i++) {
                        final IWorkaround workaround = getWorkaround(workaroundIds[i]);
                        group[i] = workaround;
                        if (workaround instanceof IStagedWorkaround) {
                            stagedGroup.add((IStagedWorkaround) workaround);
                        }
                    }
                    this.groups.put(groupId, group);
                    if (!stagedGroup.isEmpty()) {
                        this.stagedGroups.put(groupId, stagedGroup.toArray(new IStagedWorkaround[stagedGroup.size()]));
                    }
                }
            }
            else {
                this.groups = null;
                this.stagedGroups = null;
            }
        }

        @SuppressWarnings("unchecked")
        public <C extends IWorkaround> C getWorkaround(final String id, final Class<C> workaroundClass) {
            final IWorkaround present = getWorkaround(id);
            if (!workaroundClass.isAssignableFrom(present.getClass())) {
                throw new IllegalArgumentException("Wrong type of registered workaround requested: " + workaroundClass.getName() + " instead of " + present.getClass().getName());
            }
            else {
                return (C) present;
            }
        }

        /**
         * 
         * @param id
         * @return
         * @throws IllegalArgumentException
         *             If no workaround is set for the id.
         */
        public IWorkaround getWorkaround(final String id) {
            final IWorkaround present = workaroundsById.get(id);
            if (present == null) {
                throw new IllegalArgumentException("Workaround id not registered: " + id);
            }
            return present;
        }

        /**
         * Call resetConditions for all stored workarounds, excluding
         * WorkaroundCounter instances (sub classes get reset too).
         */
        public void resetConditions() {
            for (int i = 0; i < stagedWorkarounds.length; i++) {
                stagedWorkarounds[i].resetConditions();
            }
        }

        /**
         * Call resetConditions for all workarounds that are within the group
         * with the given groupId.
         * 
         * @param groupId
         */
        public void resetConditions(final String groupId) {
            if (this.stagedGroups != null) {
                final IStagedWorkaround[] workarounds = stagedGroups.get(groupId);
                if (workarounds != null) {
                    for (int i = 0; i < workarounds.length; i++) {
                        workarounds[i].resetConditions();
                    }
                }
            }
        }

        /**
         * Unchecked use.
         * 
         * @param workaroundId
         * @return The result of IWorkaround.use() for the registered instance.
         * @throws IllegalArgumentException
         *             If no workaround is registered for this id.
         * 
         */
        public boolean use(String workaroundId) {
            // TODO: For consistency might throw the same exception everywhere (IllegalArgument?). 
            final IWorkaround workaround = workaroundsById.get(workaroundId);
            if (workaround == null) {
                throw new IllegalArgumentException("Workaround id not registered: " + workaroundId);
            }
            else {
                if (workaround.use()) {
                    if (justUsedIds != null) {
                        justUsedIds.add(workaround.getId());
                    }
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        /**
         * Unchecked canUse.
         * 
         * @param workaroundId
         * @return The result of IWorkaround.canUse() for the registered
         *         instance.
         * @throws NullPointerException
         *             if no workaround is registered for this id.
         * 
         */
        public boolean canUse(String workaroundId) {
            // TODO: For consistency might throw the same exception everywhere (IllegalArgument?). 
            return workaroundsById.get(workaroundId).canUse();
        }

        /**
         * Set the just used ids collection or null, to set or not set on
         * use(...).
         * 
         * @param justUsedIds
         */
        public void setJustUsedIds(final Collection<String> justUsedIds) {
            this.justUsedIds = justUsedIds;
        }

    }

    /**
     * Registers workaround.getNewInstance() for the set id as a blueprint. A
     * parent counter will be created, if it doesn't exist yet. The counter will
     * not be added to the allTimeCounter of the given IWorkaround instances,
     * but to the one of the internally stored instance.
     * 
     * @param bluePrints
     */
    public void setWorkaroundBluePrint(IWorkaround...bluePrints);

    /**
     * Specify what workaround ids belong to a certain group. Workarounds can be
     * in multiple groups. The workaroundIds must exist.
     * 
     * @param groupId
     * @param workaroundIds
     */
    public void setGroup(String groupId, Collection<String> workaroundIds);

    /**
     * Specify what workaround ids belong to a certain group. Workarounds can be
     * in multiple groups. The workaroundIds must exist.
     * 
     * @param groupId
     * @param bluePrints
     *            The ids are used, must exist.
     */
    public void setGroup(String groupId, IWorkaround... bluePrints);

    /**
     * Define which workarounds and which groups belong to the WorkaroundSet of
     * the given workaroundSetId.
     * 
     * @param workaroundSetId
     * @param bluePrintIds
     * @param groupIds
     */
    public void setWorkaroundSetByIds(String workaroundSetId, Collection<String> bluePrintIds, String... groupIds);

    /**
     * Retrieve a pre-set WorkaroundSet instance with new Workaround instances
     * generated from the blueprints.
     * 
     * @param workaroundSetId
     * @return
     */
    public WorkaroundSet getWorkaroundSet(String workaroundSetId);

    /**
     * Get a registered global IAcceptDenyCounter instance, if registered.
     * 
     * @param id
     * @return The registered IAcceptDenyCounter instance, or null if none is
     *         registered for the given id.
     */
    public IAcceptDenyCounter getGlobalCounter(String id);

    /**
     * Get a registered global IAcceptDenyCounter instance, create if not
     * present.
     * 
     * @param id
     * @return
     */
    public IAcceptDenyCounter createGlobalCounter(String id);

    /**
     * Retrieve a new instance, ready for use, attached to a global counter of
     * the same id.
     * 
     * @param id
     * @param workaroundClass
     *            Specific type to use. The registry may have a blueprint set
     *            and just clone that.
     * @return
     * @throws IllegalArgumentException
     *             If either of id or workaroundClass is not possible to use.
     */
    public <C extends IWorkaround> C getWorkaround(String id, Class<C> workaroundClass);

    /**
     * Retrieve a new instance, ready for use, attached to a global counter of
     * the same id.
     * 
     * @param id
     * @return
     * @throws IllegalArgumentException
     *             If either of id or workaroundClass is not possible to use.
     */
    public IWorkaround getWorkaround(String id);

    /**
     * Retrieve an unmodifiable map for all registered global counters. The
     * counters are not copies, so they could be altered, discouraged though.
     * 
     * @return
     */
    public Map<String, IAcceptDenyCounter> getGlobalCounters();

    /**
     * Convenience to get the internally registered id.
     * 
     * @param workaroundId
     * @return
     * @throws IllegalArgumentException
     *             If an id is not registered for a given workaround.
     */
    public String getCheckedWorkaroundId(String workaroundId);

    /**
     * Convenience method to get a set of ids, testing if bluePrints exist.
     * 
     * @param workarounds
     * @return A set fit for iteration. Contained ids are taken from the
     *         internally registered instances.
     * @throws IllegalArgumentException
     *             If an id is not registered for a given workaround.
     */
    public Set<String> getCheckedIdSet(Collection<? extends IWorkaround> workarounds); // UH.

}
