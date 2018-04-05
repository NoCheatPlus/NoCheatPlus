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

public interface InputSpecificPenalty extends Penalty {

    /**
     * Apply the input-specific effects of a penalty, for other input than
     * Player.
     * <hr/>
     * Applying input specific penalties might only be possible within the
     * surrounding context of creation of ViolationData, i.e. during the event
     * handling. Input-specific effects will not apply within
     * ViolationData.executeActions, be it within the TickTask
     * (requestActionsExecution) or during handling a primary-thread check
     * failure. Instead input specific penalties are executed within the context
     * that provides the input, e.g. after handling a damage event.
     * <hr/>
     * 
     * @param input
     *            May be of unexpected type.
     */
    // TODO: Consider boolean result for "the input type was accepted", in order to detect if an input is not accepted by any generic penalty.
    public <I> void apply(I input);

}
