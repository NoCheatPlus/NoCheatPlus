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

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

public class SelfHit extends Check {

    public SelfHit() {
        super(CheckType.FIGHT_SELFHIT);
    }

    public boolean check(final Player damager, final Player damaged, final FightData data, final FightConfig cc){
        // Check if the Entity Id's are Equals
        if (damager.getEntityId() != damaged.getEntityId()) return false;

        boolean cancel = false;
        // Treat self hitting as instant violation.
        data.selfHitVL.add(System.currentTimeMillis(), 1.0f);
        // NOTE: This lets VL decrease slightly over 30 seconds, one could also use a number, but  this is more tolerant.
        cancel = executeActions(damager, data.selfHitVL.score(0.99f), 1.0f, cc.selfHitActions).willCancel();

        return cancel;
    }

}
