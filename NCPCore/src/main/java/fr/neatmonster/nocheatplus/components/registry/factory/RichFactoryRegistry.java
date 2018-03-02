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
package fr.neatmonster.nocheatplus.components.registry.factory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.data.IData;
import fr.neatmonster.nocheatplus.components.data.IDataOnRemoveSubCheckData;
import fr.neatmonster.nocheatplus.components.registry.meta.RichTypeSetRegistry;
import fr.neatmonster.nocheatplus.utilities.CheckTypeUtil;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/**
 * Thread safe "read" factory registry with additional convenience
 * functionality. (TODO: Registration might not be thread-safe.)
 * <hr>
 * Thread safety further depends on the registered factories for fetching new
 * instances.
 * 
 * @author asofold
 *
 * @param <A>
 *            Factory argument type.
 */
public class RichFactoryRegistry<A> extends RichTypeSetRegistry implements IRichFactoryRegistry<A> {

    public static class CheckRemovalSpec {

        public final Collection<Class<?>> completeRemoval = new LinkedHashSet<Class<?>>();
        public final Collection<Class<? extends IDataOnRemoveSubCheckData>> subCheckRemoval = new LinkedHashSet<Class<? extends IDataOnRemoveSubCheckData>>();
        public final Collection<CheckType> checkTypes;;

        public CheckRemovalSpec(final CheckType checkType, 
                final boolean withDescendantCheckTypes, 
                final IRichFactoryRegistry<?> factoryRegistry
                ) {
            this(withDescendantCheckTypes 
                    ? CheckTypeUtil.getWithDescendants(checkType)
                            : Arrays.asList(checkType), factoryRegistry);
        }

        public CheckRemovalSpec(final Collection<CheckType> checkTypes, 
                final IRichFactoryRegistry<?> factoryRegistry) {
            this.checkTypes = checkTypes;
            for (final CheckType refType : checkTypes) {
                for (final Class<? extends IData> type : factoryRegistry.getGroupedTypes(
                        IData.class, refType)) {
                    completeRemoval.add(type);
                }
                for (final Class<? extends IDataOnRemoveSubCheckData> type : factoryRegistry.getGroupedTypes(
                        IDataOnRemoveSubCheckData.class, refType)) {
                    subCheckRemoval.add(type);
                }
            }
            if (checkTypes.contains(CheckType.ALL)) {
                for (final Class<? extends IData> type : factoryRegistry.getGroupedTypes(
                        IData.class)) {
                    completeRemoval.add(type);
                }
                for (final Class<? extends IDataOnRemoveSubCheckData> type : factoryRegistry.getGroupedTypes(
                        IDataOnRemoveSubCheckData.class)) {
                    subCheckRemoval.add(type);
                }
            }
        }
    }

    private final Lock lock;
    private final FactoryOneRegistry<A> factoryRegistry;
    @SuppressWarnings("unchecked")
    private Set<Class<?>> autoGroups = Collections.EMPTY_SET;


    public RichFactoryRegistry(final Lock lock) {
        super(lock);
        this.lock = lock;
        factoryRegistry = new FactoryOneRegistry<A>(
                lock, CheckUtils.primaryServerThreadContextTester);
    }

    @Override
    public <T> T getNewInstance(final Class<T> registeredFor, final A arg) {
        return factoryRegistry.getNewInstance(registeredFor, arg);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void registerFactory(final Class<T> registerFor,
            final IFactoryOne<A, T> factory) {
        lock.lock();
        factoryRegistry.registerFactory(registerFor, factory);
        for (final Class<?> groupType: autoGroups) {
            if (groupType.isAssignableFrom(registerFor)) {
                addToGroups(registerFor, (Class<? super T>) groupType);
            }
        }
        lock.unlock();
    }

    @Override
    public <G> void createAutoGroup(final Class<G> groupType) {
        lock.lock();
        createGroup(groupType);
        final Set<Class<?>> autoGroups = new LinkedHashSet<Class<?>>(this.autoGroups);
        autoGroups.add(groupType);
        this.autoGroups = autoGroups;
        lock.unlock();
    }

}
