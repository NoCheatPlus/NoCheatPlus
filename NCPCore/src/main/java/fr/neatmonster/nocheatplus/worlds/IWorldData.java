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
package fr.neatmonster.nocheatplus.worlds;

import java.util.Collection;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.data.IDataOnRemoveSubCheckData;
import fr.neatmonster.nocheatplus.components.data.checktype.IConfigDataAccess;
import fr.neatmonster.nocheatplus.components.registry.IGetGenericInstance;

/**
 * Public access interface for per-world data.
 * 
 * @author asofold
 *
 */
public interface IWorldData extends IConfigDataAccess, IGetGenericInstance {


    /**
     * Overridden to provide extra per-world functionality. See:
     * {@link fr.neatmonster.nocheatplus.components.data.checktype.IConfigDataAccess}
     */
    @Override
    public IWorldCheckNode getCheckNode(CheckType checkType);

    /**
     * Retrieve the world name this instance has been registered for. To prevent
     * issues with misspelling on commands and configuration, the lower case
     * variant is the main identifier.
     * 
     * @return
     */
    public String getWorldNameLowerCase();

    /**
     * Get an object containing exact case name and UUID.
     * 
     * @return In case the world hasn't been loaded, null will be returned.
     */
    public WorldIdentifier getWorldIdentifier();

    /**
     * Server side lag adaption flag - subject to rename / change.
     * 
     * @param checkType
     * @return
     */
    public boolean shouldAdjustToLag(CheckType checkType);

    /**
     * Remove data from the cache (not from underlying factories, nor from per
     * world storage.
     * 
     * @param registeredFor
     */
    public <T> void removeGenericInstance(Class<T> registeredFor);

    /**
     * Remove all generic instances from cache, which are contained in the given
     * collection.
     * 
     * @param types
     */
    public void removeAllGenericInstances(Collection<Class<?>> types);

    /**
     * Call dataOnRemoveSubCheckData(...).
     * 
     * @param subCheckRemoval
     */
    public void removeSubCheckData(
            Collection<Class<? extends IDataOnRemoveSubCheckData>> subCheckRemoval,
            Collection<CheckType> checkTypes);

    // TODO: isDebugActive(CheckType checkType);

}
