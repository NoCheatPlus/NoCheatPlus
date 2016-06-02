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
package fr.neatmonster.nocheatplus.workaround;

import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.IAcceptDenyCounter;

/**
 * Workaround allowing for resetting pre/side-conditions, with an extra counter
 * lasting until reset. Parent counters may not be supported.
 * 
 * @author asofold
 *
 */
public interface IStagedWorkaround {

    // TODO: resetConditions: Consider to have it return false by default (x.use() || x.resetConditions() with pre-conditions).
    /**
     * Generic reset to the initial conditions. This does not reset the all time
     * counters, other effects depend on the implementation.
     */
    public void resetConditions();

    /**
     * Get the counter that will reset with the next call to resetConditions.
     * Parent counters may not be supported.
     * 
     * @return
     */
    public IAcceptDenyCounter getStageCounter();

}
