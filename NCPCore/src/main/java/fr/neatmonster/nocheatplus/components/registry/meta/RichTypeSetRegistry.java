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
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.ds.map.HashMapLOW;

public class RichTypeSetRegistry implements IRichTypeSetRegistry {

    private final Lock lock;

    /**
     * Grouped types to have a faster way of iterating data types stored in the
     * PlayerData cache.
     */
    private final TypeSetRegistry groupedTypes;
    /**
     * Additional attachment of grouped types to check types - needs to be
     * registered explicitly.
     */
    private final HashMapLOW<CheckType, TypeSetRegistry> groupedTypesByCheckType;


    public RichTypeSetRegistry(final Lock lock) {
        this.lock = lock;
        groupedTypes = new TypeSetRegistry(lock);
        groupedTypesByCheckType = new HashMapLOW<CheckType, 
                TypeSetRegistry>(lock, 35);
    }

    @Override
    public <T> Collection<Class<? extends T>> getGroupedTypes(final Class<T> groupType) {
        return groupedTypes.getGroupedTypes(groupType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<Class<? extends T>> getGroupedTypes(final Class<T> groupType,
            final CheckType checkType) {
        final TypeSetRegistry reg = groupedTypesByCheckType.get(checkType);
        return  (reg == null 
                ? (Collection<Class<? extends T>>) Collections.EMPTY_SET 
                        : reg.getGroupedTypes(groupType));
    }

    @Override
    public <I> void addToGroups(final Class<I> itemType, 
            final Class<? super I>... groupTypes) {
        lock.lock();
        for (final Class<? super I> groupType : groupTypes) {
            createGroup(groupType);
        }
        groupedTypes.addToGroups(itemType, groupTypes);
        lock.unlock();
    }

    @Override
    public <I> void addToGroups(CheckType checkType, Class<I> itemType,
            Class<? super I>... groupTypes) {
        lock.lock();
        for (final Class<? super I> groupType : groupTypes) {
            createGroup(groupType);
        }
        TypeSetRegistry reg = groupedTypesByCheckType.get(checkType);
        if (reg == null) {
            reg = new TypeSetRegistry(lock);
            updateRegistry(reg);
            groupedTypesByCheckType.put(checkType, reg);
        }
        reg.addToGroups(itemType, groupTypes);
        groupedTypes.addToGroups(itemType, groupTypes);
        lock.unlock();
    }

    @Override
    public void addToExistingGroups(Class<?> itemType) {
        groupedTypes.addToExistingGroups(itemType);
    }

    @Override
    public <I> void addToExistingGroups(final CheckType checkType,
            final Class<I> itemType) {
        lock.lock();
        TypeSetRegistry reg = groupedTypesByCheckType.get(checkType);
        if (reg == null) {
            reg = new TypeSetRegistry(lock);
            updateRegistry(reg);
            groupedTypesByCheckType.put(checkType, reg);
        }
        reg.addToExistingGroups(itemType);
        groupedTypes.addToExistingGroups(itemType);
        lock.unlock();
    }

    /**
     * Update the given registry to contain all group types of the general
     * registry.
     * 
     * @param reg
     */
    private void updateRegistry(final TypeSetRegistry reg) {
        reg.updateGroupTypes(groupedTypes);
    }

    @Override
    public <G> void createGroup(Class<G> groupType) {
        groupedTypes.createGroup(groupType);
        for (Entry<CheckType, TypeSetRegistry> entry : groupedTypesByCheckType.iterable()) {
            entry.getValue().createGroup(groupType);
        }
    }

    @Override
    public <I> void addToGroups(final Collection<CheckType> checkTypes,
            final Class<I> itemType, final Class<? super I>... groupTypes) {
        lock.lock();
        for (final CheckType checkType : checkTypes) {
            addToGroups(checkType, itemType, groupTypes);
        }
        lock.unlock();
    }

    @Override
    public <I> void addToExistingGroups(final Collection<CheckType> checkTypes,
            final Class<I> itemType) {
        lock.lock();
        for (final CheckType checkType : checkTypes) {
            addToExistingGroups(checkType, itemType);
        }
        lock.unlock();
    }

}
