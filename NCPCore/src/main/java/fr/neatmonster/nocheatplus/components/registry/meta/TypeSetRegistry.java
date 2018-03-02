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
package fr.neatmonster.nocheatplus.components.registry.meta;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import fr.neatmonster.nocheatplus.utilities.ds.map.HashMapLOW;

/**
 * Register sets of 'item types' for group types.
 * <hr>
 * Typo :).
 * 
 * @author asofold
 *
 */
public class TypeSetRegistry {

    /**
     * Idea of typed nodes is to help the JIT, not sure such ever will work.
     * 
     * @author asofold
     *
     * @param <G>
     */
    private static class GroupNode<G> {

        @SuppressWarnings("unchecked")
        private Collection<Class<? extends G>> group = Collections.EMPTY_LIST;
        private final Class<G> groupType;

        GroupNode(Class<G> groupType) {
            this.groupType = groupType;
        }

        void add(Class<? extends G> itemClass) {
            final Collection<Class<? extends G>> copyGroup = new LinkedHashSet<Class<? extends G>>(group);
            copyGroup.add(itemClass);
            group = Collections.unmodifiableCollection(copyGroup);
        }

        @SuppressWarnings("unchecked")
        void add(Class<?> groupType, Class<?> itemClass) {
            if (groupType == this.groupType) {
                add((Class<? extends G>) itemClass);
            }
            else {
                throw new IllegalArgumentException("Invalid group type.");
            }
        }

        Collection<Class<? extends G>> getItems() {
            return group;
        }

        void createGroup(TypeSetRegistry extReg) {
            extReg.createGroup(this.groupType);
        }

    }

    ////////////////////////77
    // Instance.
    /////////////////////

    private final Lock lock;
    /**
     * Types grouped by other types for efficient getting.
     */
    private final HashMapLOW<Class<?>, GroupNode<?>> groupedTypes;


    public TypeSetRegistry(Lock lock) {
        this.lock = lock;
        groupedTypes = new HashMapLOW<Class<?>, GroupNode<?>>(lock, 10);
    }

    /**
     * Register the item type for the given group types.
     * 
     * @param itemType
     * @param groupTypes
     */
    public <I> void addToGroups(final Class<I> itemType, final Class<? super I>... groupTypes) {
        lock.lock();
        for (final Class<? super I> groupType : groupTypes) {
            if (!groupType.isAssignableFrom(itemType)) {
                lock.unlock();
                throw new IllegalArgumentException("Can't assign " + itemType.getName() + " to " + groupType.getName() + "!");
            }
            @SuppressWarnings("unchecked")
            GroupNode<? super I> node = (GroupNode<? super I>) groupedTypes.get(groupType);
            if (node == null) {
                node = newGroupNode(groupType);
            }
            node.add(groupType, itemType);
        }
        lock.unlock();
    }

    /**
     * Create if not existent.
     * 
     * @param itemTypes
     */
    public <G> void createGroup(final Class<G> groupType) {
        lock.lock();
        if (!groupedTypes.containsKey(groupType)) {
            newGroupNode(groupType);
        }
        lock.unlock();
    }

    private <G> GroupNode<G> newGroupNode(Class<G> groupType) {
        final GroupNode<G> node =  new GroupNode<G>(groupType);
        groupedTypes.put(groupType, node);
        return node;
    }

    /**
     * Add the item type only to already registered (super) groups.
     * 
     * @param itemType
     */
    public void addToExistingGroups(final Class<?> itemType) {
        lock.lock();
        // Sort in all existing registered group types.
        for (final Entry<Class<?>, GroupNode<?>> entry : groupedTypes.iterable()) {
            final Class<?> groupType = entry.getKey();
            if (groupType.isAssignableFrom(itemType)) {
                // TODO: also here try catch.
                final GroupNode<?> group = entry.getValue();
                group.add(groupType, itemType);
            }
        }
        lock.unlock();
    }

    /**
     * Retrieve all types that are grouped under the given group type.
     * 
     * @param groupType
     * @return
     */
    @SuppressWarnings("unchecked")
    public <G> Collection<Class<? extends G>> getGroupedTypes(Class<G> groupType) {
        final GroupNode<G> group = (GroupNode<G>) groupedTypes.get(groupType);
        return group == null ? Collections.EMPTY_LIST : group.getItems();
    }

    /**
     * Ensure all groups exist (items are not copied).
     * 
     * @param refReg
     */
    public void updateGroupTypes(TypeSetRegistry refReg) {
        lock.lock(); // Ensure no interruption - iterator is not using lock, thus no dead locks.
        for (final Entry<Class<?>, GroupNode<?>> entry : refReg.groupedTypes.iterable()) {
            if (!this.groupedTypes.containsKey(entry.getKey())) {
                entry.getValue().createGroup(this);
            }
        }
        lock.unlock();
    }

}
