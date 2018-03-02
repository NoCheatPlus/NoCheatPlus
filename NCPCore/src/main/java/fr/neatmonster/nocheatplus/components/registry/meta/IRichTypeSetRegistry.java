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

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * Combine a general with per check type TypeSetRegistries. TypeSetRegistries
 * are kept "synchronized", in terms of registered groups (not items).
 * 
 * @author asofold
 *
 */
public interface IRichTypeSetRegistry {
    // TODO: package name?

    /**
     * Get all data types extending 'registeredFor', that have been explicitly
     * registered for that group.
     * 
     * @param registeredFor
     * @return
     */
    public <T> Collection<Class<? extends T>> getGroupedTypes(
            Class<T> groupType);

    /**
     * Get all data types extending 'registeredFor', that have been explicitly
     * registered for that group and for that check type. No inheritance logic
     * is applied, nor will CheckType.ALL return all data types (use
     * {@link #getGroupedTypes(Class)} instead).
     * 
     * @param registeredFor
     * @return
     */
    public <T> Collection<Class<? extends T>> getGroupedTypes(
            Class<T> groupType, CheckType checkType);

    // TODO: getFactoryTypes -> registered factory return types ?

    /**
     * Register types for a group type.
     * <hr>
     * Groups are always present for the general registry, as well as for all
     * check type specific registries. Item type registration remains specific.
     * 
     * @param itemType
     * @param groupTypes
     */
    public <I> void addToGroups(Class<I> itemType, 
            Class<? super I>... groupTypes);

    /**
     * Register the itemType for all applicable group types that already have
     * been registered.
     * <hr>
     * Groups are always present for the general registry, as well as for all
     * check type specific registries. Item type registration remains specific.
     * 
     * @param itemType
     */
    public void addToExistingGroups(final Class<?> itemType);

    /**
     * Register an item type for group types for for a check type (and general). Types get added
     * both to the general grouped types and those specific to the given check
     * type.
     * <hr>
     * Groups are always present for the general registry, as well as for all
     * check type specific registries. Item type registration remains specific.
     * 
     * @param checkType
     * @param itemType
     * @param groupTypes
     */
    public <I> void addToGroups(CheckType checkType, 
            Class<I> itemType, Class<? super I>... groupTypes);

    /**
     * Register the itemType for all applicable group types that already have
     * been registered, both for the general registry and check type specific
     * (no inheritance logic).
     * <hr>
     * Groups are always present for the general registry, as well as for all
     * check type specific registries. Item type registration remains specific.
     * 
     * 
     * @param checkType
     * @param itemType
     */
    public <I> void addToExistingGroups(CheckType checkType, 
            Class<I> itemType);

    /**
     * Register an item type for group types for for all given check types (and
     * general). Data types get added both to the general grouped types and
     * those specific to the given check types. Suggested use is with
     * {@link fr.neatmonster.nocheatplus.utilities.CheckTypeUtil}.
     * <hr>
     * Groups are always present for the general registry, as well as for all
     * check type specific registries. Item type registration remains specific.
     * 
     * @param checkTypes
     * @param itemType
     * @param groupTypes
     */
    public <I> void addToGroups(Collection<CheckType> checkTypes, 
            Class<I> itemType, Class<? super I>... groupTypes);

    /**
     * Register the itemType for all applicable group types that already have
     * been registered, both for the general registry and check type specific
     * (no inheritance logic).Suggested use is with
     * {@link fr.neatmonster.nocheatplus.utilities.CheckTypeUtil}.
     * <hr>
     * Groups are always present for the general registry, as well as for all
     * check type specific registries. Item type registration remains specific.
     * 
     * 
     * @param checkType
     * @param itemType
     */
    public <I> void addToExistingGroups(Collection<CheckType> checkTypes, 
            Class<I> itemType);

    /**
     * Register the group type both for the general registry and for all check
     * types.
     * <hr>
     * Groups are always present for the general registry, as well as for all
     * check type specific registries. Item type registration remains specific.
     * 
     * @param groupType
     */
    public <G> void createGroup(Class<G> groupType);

}
