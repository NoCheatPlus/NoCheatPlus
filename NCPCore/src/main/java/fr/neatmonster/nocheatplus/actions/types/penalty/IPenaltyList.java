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
package fr.neatmonster.nocheatplus.actions.types.penalty;

/**
 * Contain applicable penalty types that need to be handled outside of ViolationData.executeActions, for access by ViolationData.
 * 
 * @author asofold
 *
 */
public interface IPenaltyList {
    // TODO: Typed ? + typed per input getter (mapped lists)
    
    /**
     * Add an input-specific penalty.
     * @param penalty
     */
    public void addInputSpecificPenalty(InputSpecificPenalty penalty);
}
