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

import fr.neatmonster.nocheatplus.components.registry.meta.IRichTypeSetRegistry;

/**
 * Combine an IFactoryOneRegistry with TypeSetRegistries (general and per check
 * type). TypeSetRegistries are kept "synchronized", in terms of registered
 * groups (not items).
 * 
 * @author asofold
 *
 */
public interface IRichFactoryRegistry<A> extends IRichTypeSetRegistry, IFactoryOneRegistry<A> {

    /**
     * Register a factory, see:
     * {@link IFactoryOneRegistry#registerFactory(Class, IFactoryOne)}
     * <hr/>
     * The type the factory is registered for might automatically be added to
     * all groups which have been set to auto grouping:
     * {@link #createAutoGroup(Class)}
     * <hr/>
     */
    @Override
    public <T> void registerFactory(final Class<T> registerFor, 
            final IFactoryOne<A, T> factory);


    /**
     * Create the group, and automatically add factory return types to this group
     * @param groupType
     */
    public <G> void createAutoGroup(Class<G> groupType);

}
