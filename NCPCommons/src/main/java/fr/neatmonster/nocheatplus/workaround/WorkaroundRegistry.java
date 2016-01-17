package fr.neatmonster.nocheatplus.workaround;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An access point for fetching global WorkaroundCounter instances and a factory
 * for fetching new sets of per-player workarounds.
 * 
 * @author asofold
 *
 */
public interface WorkaroundRegistry {

    /**
     * Convenience to retrieve any type of per-player Workaround by id, for the
     * case one doesn't want to store the registry and/or individual Workaround
     * implementations as members. Groups allow resetting certain types of
     * workarounds in bunches.
     * 
     * @author asofold
     *
     */
    public static class WorkaroundSet {

        // TODO: getUseCount()
        // TODO: A list for ids of just used workarounds (reset externally. Add use(id) vs alter Workaround)?
        // TODO: Better optimized constructor.

        /** Map workaround id to workaround. */
        private final Map<String, Workaround> workaroundsById = new LinkedHashMap<String, Workaround>();

        /** Only the workarounds that might need resetting. */
        private final Workaround[] mightNeedReset;

        /** Map groupId to workarounds. */
        private final Map<String, Workaround[]> groups;

        /**
         * 
         * @param bluePrints
         * @param groups
         *            Map groupId to workaroundIds, groups may be null if none
         *            are set. All referenced workaround ids must be registered,
         *            workarounds can be in multiple groups.
         */
        public WorkaroundSet(final Workaround[] bluePrints, final Map<String, String[]> groups) {
            final Class<?> excludeFromReset = WorkaroundCounter.class;
            final List<Workaround> mightNeedReset = new ArrayList<Workaround>(bluePrints.length);
            for (int i = 0; i < bluePrints.length; i++) {
                final Workaround workaround = bluePrints[i].getNewInstance();
                workaroundsById.put(workaround.getId(), workaround);
                if (workaround.getClass() != excludeFromReset) {
                    mightNeedReset.add(workaround);
                }
            }
            this.mightNeedReset = mightNeedReset.toArray(new Workaround[mightNeedReset.size()]);
            // Prepare fast to reset lists, if groups are given.
            if (groups != null) {
                this.groups = new HashMap<String, Workaround[]>();
                for (final Entry<String, String[]> entry : groups.entrySet()) {
                    final String[] workaroundIds = entry.getValue();
                    final Workaround[] group = new Workaround[workaroundIds.length];
                    for (int i = 0; i < workaroundIds.length; i++) {
                        group[i] = getWorkaround(workaroundIds[i]);
                    }
                    this.groups.put(entry.getKey(), group);
                }
            } else {
                this.groups = null;
            }
        }

        @SuppressWarnings("unchecked")
        public <C extends Workaround> C getWorkaround(final String id, final Class<C> workaroundClass) {
            final Workaround present = getWorkaround(id);
            if (!workaroundClass.isAssignableFrom(present.getClass())) {
                throw new IllegalArgumentException("Wrong type of registered workaround requested: " + workaroundClass.getName() + " instead of " + present.getClass().getName());
            } else {
                return (C) present;
            }
        }

        public Workaround getWorkaround(final String id) {
            final Workaround present = workaroundsById.get(id);
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
            for (int i = 0; i < mightNeedReset.length; i++) {
                mightNeedReset[i].resetConditions();
            }
        }

        /**
         * Call resetConditions for all workarounds that are within the group
         * with the given groupId.
         * 
         * @param groupId
         */
        public void resetConditions(final String groupId) {
            final Workaround[] workarounds = groups.get(groupId);
            if (workarounds == null) {
                throw new IllegalArgumentException("Group not registered: " + groupId);
            }
            for (int i = 0; i < workarounds.length; i++) {
                workarounds[i].resetConditions();
            }
        }

    }

    // TODO: Might make getWorkaround non public, to favor use of WorkaroundSet.

    /**
     * Registers workaround.getNewInstance() for the set id. Set parent to
     * createGlobalCounter(id), if a global counter is desired.
     * 
     * @param bluePrints
     */
    public void setWorkaroundBluePrint(Workaround...bluePrints);

    /**
     * Specify what workaround ids belong to a certain group. Workarounds can be
     * in multiple groups.
     * 
     * @param groupId
     * @param workaroundIds
     */
    public void setGroup(String groupId, Collection<String> workaroundIds);

    /**
     * Define which workarounds and which groups belong to the WorkaroundSet of
     * the given workaroundSetId.
     * 
     * @param workaroundSetId
     * @param bluePrints
     *            Lazily registers, if no blueprint is present. Already
     *            registered blueprints are kept.
     * @param groupIds
     *            Must already be registered.
     */
    public void setWorkaroundSet(String workaroundSetId, Collection<Workaround> bluePrints, String... groupIds);

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
     * Get a registered global WorkaroundCounter, if registered.
     * 
     * @param id
     * @return The registered WorkaroundCounter instance, or null if none is
     *         registered for the given id.
     */
    public WorkaroundCounter getGlobalCounter(String id);

    /**
     * Get a registered global WorkaroundCounter, create if not present.
     * 
     * @param id
     * @return
     */
    public WorkaroundCounter createGlobalCounter(String id);

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
    public <C extends Workaround> C getWorkaround(String id, Class<C> workaroundClass);

    /**
     * Retrieve a new instance, ready for use, attached to a global counter of
     * the same id.
     * 
     * @param id
     * @return
     * @throws IllegalArgumentException
     *             If either of id or workaroundClass is not possible to use.
     */
    public Workaround getWorkaround(String id);

    /**
     * Get all global count values by id.
     * 
     * @return
     */
    public Map<String, Integer> getGlobalUseCount();

}
