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
 * This penalty does nothing. It's presence solely indicates that an action is
 * to be canceled or rolled back.
 * 
 * @author asofold
 *
 */
public final class CancelPenalty implements Penalty {
    
    // TODO: Consider putting a static instance somewhere instead (check with ==).

    @Override
    public final boolean hasPlayerEffects() {
        return false;
    }

    @Override
    public final boolean hasInputSpecificEffects() {
        return false;
    }

    @Override
    public final void apply(Player player) {
    }

}
