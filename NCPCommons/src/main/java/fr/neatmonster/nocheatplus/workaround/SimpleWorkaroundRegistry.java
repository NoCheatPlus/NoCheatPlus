package fr.neatmonster.nocheatplus.workaround;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple registry for workarounds. No thread-safety built in.
 * 
 * @author asofold
 *
 */
public class SimpleWorkaroundRegistry implements WorkaroundRegistry {

    /** Global counter by id. */
    private final Map<String, WorkaroundCounter> counters = new HashMap<String, WorkaroundCounter>();

    /** Workaround blue print by id. */
    private final Map<String, Workaround> bluePrints = new HashMap<String, Workaround>();

    /** Map group id to array of workaround ids. */
    private final Map<String, String[]> groups = new HashMap<String, String[]>();

    /** Map WorkaroundSet id to the contained blueprint ids. */
    private final Map<String, String[]> workaroundSets = new HashMap<String, String[]>();

    /** Map WorkaroundSet id to the contained group ids. Might not contain entries for all ids. */
    private final Map<String, String[]> workaroundSetGroups = new HashMap<String, String[]>();

    @Override
    public void setWorkaroundBluePrint(final Workaround... bluePrints) {
        for (int i = 0; i < bluePrints.length; i++) {
            final Workaround workaround = bluePrints[i];
            this.bluePrints.put(workaround.getId(), workaround.getNewInstance());
        }
    }

    @Override
    public void setGroup(final String groupId, final Collection<String> workaroundIds) {
        groups.put(groupId, workaroundIds.toArray(new String[workaroundIds.size()]));
    }

    @Override
    public void setWorkaroundSet(final String workaroundSetId, final Collection<Workaround> bluePrints, final String... groupIds) {
        final String[] ids = new String[bluePrints.size()];
        int i = 0;
        for (final Workaround bluePrint : bluePrints) {
            final String id = bluePrint.getId();
            if (!this.bluePrints.containsKey(id)) {
                // Lazily register.
                setWorkaroundBluePrint(bluePrint);
            }
            ids[i] = id;
            i ++;
        }
        this.workaroundSets.put(workaroundSetId, ids);
        if (groupIds != null && groupIds.length > 0) {
            for (i = 0; i < groupIds.length; i++) {
                if (!this.groups.containsKey(groupIds[i])) {
                    throw new IllegalArgumentException("Group not registered: " + groupIds[i]);
                }
            }
            this.workaroundSetGroups.put(workaroundSetId, groupIds);
        }
    }

    @Override
    public void setWorkaroundSetByIds(final String workaroundSetId, final Collection<String> bluePrintIds, final String... groupIds) {
        final List<Workaround> bluePrints = new ArrayList<Workaround>(bluePrintIds.size());
        for (final String id : bluePrintIds) {
            final Workaround bluePrint = this.bluePrints.get(id);
            if (bluePrint == null) {
                throw new IllegalArgumentException("the blueprint is not registered: " + id);
            }
            bluePrints.add(bluePrint);
        }
        setWorkaroundSet(workaroundSetId, bluePrints, groupIds);
    }

    @Override
    public WorkaroundSet getWorkaroundSet(final String workaroundSetId) {
        final String[] workaroundIds = workaroundSets.get(workaroundSetId);
        if (workaroundIds == null) {
            throw new IllegalArgumentException("WorkaroundSet not registered: " + workaroundSetId);
        }
        final Workaround[] bluePrints = new Workaround[workaroundIds.length];
        for (int i = 0; i < workaroundIds.length; i++) {
            bluePrints[i] = this.bluePrints.get(workaroundIds[i]);
        }
        final Map<String, String[]> groups;
        final String[] groupIds = this.workaroundSetGroups.get(workaroundSetId);
        if (groupIds == null) {
            groups = null;
        }
        else {
            groups = new HashMap<String, String[]>();
            for (int i = 0; i < groupIds.length; i++) {
                final String groupId = groupIds[i];
                groups.put(groupId, this.groups.get(groupId));
            }
        }
        return new WorkaroundSet(bluePrints, groups);
    }

    @Override
    public WorkaroundCounter getGlobalCounter(final String id) {
        return counters.get(id);
    }

    @Override
    public WorkaroundCounter createGlobalCounter(final String id) {
        WorkaroundCounter counter = counters.get(id);
        if (counter == null) {
            counter = new WorkaroundCounter(id);
            counters.put(id, counter);
        }
        return counter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends Workaround> C getWorkaround(final String id, final Class<C> workaroundClass) {
        final Workaround workaround = getWorkaround(id);
        if (workaroundClass.isAssignableFrom(workaround.getClass())) {
            return (C) workaround;
        }
        else {
            throw new IllegalArgumentException("Unsupported class for id '" + id + "': " + workaroundClass.getName() + " (actual class is " + workaround.getClass().getName() + ")");
        }
    }

    @Override
    public Workaround getWorkaround(final String id) {
        final Workaround bluePrint = bluePrints.get(id);
        if (bluePrint == null) {
            throw new IllegalArgumentException("Id not registered as blueprint: " + id);
        }
        return bluePrint.getNewInstance();
    }

    @Override
    public Map<String, Integer> getGlobalUseCount() {
        final Map<String, Integer> currentCounts = new LinkedHashMap<String, Integer>(counters.size());
        for (final WorkaroundCounter counter : counters.values()) {
            currentCounts.put(counter.getId(), counter.getUseCount());
        }
        return currentCounts;
    }

}
