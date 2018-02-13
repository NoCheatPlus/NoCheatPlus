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
package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * First WrongTurn variant (pitch, possibly other).
 * 
 * @author asofold
 *
 */
public class WrongTurn extends Check {

    public WrongTurn() {
        super(CheckType.FIGHT_WRONGTURN);
    }

    public boolean check (final Player player, final Location loc, 
            final FightData data, final FightConfig cc) {

        final float pitch = loc.getPitch();

        // Invalid Pitch
        // TODO: Does invalid pitch arrive here at all?
        // TODO: Prefer to detect on packet level already.
        if (Math.abs(pitch) > 90.0f) {
            data.wrongTurnVL += 1; // (Never cooldown.)
            if (executeActions(player, data.wrongTurnVL, 1.0, cc.wrongTurnActions).willCancel()) {
                return true;
            }
        }

        // TODO: Better have the following in a check not resulting in banning 100% of the time.
        // TODO: Suspicious yaw + pitch combination (static).
        /*
         * TODO: Past moves: detect fast turning towards opponent for an easier
         * thing (re-use for difficulty). Better have an extra spot for relating
         * moves of the attacker vs. moves of the attacked.
         */
        // TODO: Past moves: detect optimizing look/position in general.

        return false;
    }

}
