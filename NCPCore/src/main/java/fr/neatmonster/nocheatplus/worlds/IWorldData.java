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

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.data.checktype.IConfigDataAccess;
import fr.neatmonster.nocheatplus.components.registry.IGetGenericInstance;
import fr.neatmonster.nocheatplus.components.registry.IGetGenericInstanceHandle;

/**
 * Public access interface for per-world data.
 * 
 * @author asofold
 *
 */
public interface IWorldData extends IConfigDataAccess, IGetGenericInstance, IGetGenericInstanceHandle {


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

    // TODO: Generic data (includes config) storage.

    // TODO: isDebugActive(CheckType checkType);

}
