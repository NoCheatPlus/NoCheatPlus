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
package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * Extension for CheckData, enabling disable spell checking removal of sub check
 * data.
 * 
 * @author asofold
 *
 */
public interface IRemoveSubCheckData {

    /**
     * Remove the sub check data of the given CheckType.
     * 
     * @param checkType
     * @return True, if the sub check type has been contained <i>and the
     *         implementation is capable of removing it in general.</i> False,
     *         if the implementation is not capable of removing that type of
     *         data, or if the check type doesn't qualify for a sub check at
     *         all. If false is returned, the entire check group data (or super
     *         check data) might get removed, in order to ensure data removal.
     */
    public boolean removeSubCheckData(CheckType checkType);
}
