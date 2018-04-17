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
package fr.neatmonster.nocheatplus.penalties;

/**
 * A reference to a penalty by id. Concept-wise this is meant for an early
 * configuration processing state to provide place holders. Might get replaced
 * by a generic placeholder for actions and penalties alike or implemented by a
 * throw-on-apply penalty implementation.
 * 
 * @author asofold
 *
 */
public interface IPenaltyReference {

    public String getReferencedPenaltyId();

}
