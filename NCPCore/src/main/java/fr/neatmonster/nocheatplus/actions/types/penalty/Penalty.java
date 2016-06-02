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

import org.bukkit.entity.Player;

/**
 * A simple static penalty. If this penalty applies must be determined
 * externally (PenaltyAction).
 * 
 * @author asofold
 *
 */
public interface Penalty {

    /**
     * Effects that apply only to a player. Usually executed on
     * 
     * @return
     */
    public boolean hasPlayerEffects();

    /**
     * Test if there are input-specific effects, other than with Player instance
     * input.
     * 
     * @return If true, this instance must implement InputSpecificPenalty as
     *         well. Applying input specific penalties might only be possible
     *         within the surrounding context of creation of ViolationData, i.e.
     *         during the event handling. Input-specific effects will not apply
     *         within ViolationData.executeActions, be it within the TickTask
     *         (requestActionsExecution) or during handling a primary-thread
     *         check failure.
     */
    public boolean hasInputSpecificEffects();

    /**
     * Apply player-specific effects. Executed within
     * ViolationData.executeActions, extra to input-specific effects (likely
     * before those, if within the primary thread, or within the TickTask for
     * off-primary-thread checks).
     * 
     * @param player
     */
    public void apply(Player player);

}
