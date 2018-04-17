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
 * Penalty for one type of input.
 * 
 * @author asofold
 *
 * @param <RI>
 */
public interface IPenalty<RI> {

    /**
     * Get the class that determines the accepted input type.
     * 
     * @return
     */
    public Class<RI> getRegisteredInput();

    /**
     * Internal convenience method to get around some of generics.
     * <hr>
     * <b>This method must not call
     * {@link IPenaltyList#addInputSpecificPenalty(InputSpecificPenalty)}</b>
     * 
     * @param penaltyList
     */
    public void addToPenaltyList(IPenaltyList penaltyList);

    /**
     * Apply the penalty using an appropriate input.
     * 
     * @param input
     * @return If the input was processed. Return true, in order to prevent
     *         double processing in case of the penalty applying for multiple
     *         (specific) types. Return false, to keep it in a list.
     */
    public boolean apply(RI input);

}
