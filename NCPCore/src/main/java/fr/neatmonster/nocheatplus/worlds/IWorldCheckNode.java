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

import fr.neatmonster.nocheatplus.components.config.value.OverrideType;
import fr.neatmonster.nocheatplus.components.data.checktype.IConfigCheckNode;

/**
 * Per world per check type node for most efficient direct access.
 * 
 * @author asofold
 *
 */
public interface IWorldCheckNode extends IConfigCheckNode {

    /**
     * Get the override type for the debug flag.
     * 
     * @return
     */
    public OverrideType getOverrideTypeDebug();

    /**
     * Server side lag detection - migth also do different things - subject to
     * rename / change.
     */
    public boolean shouldAdjustToLag();

}
