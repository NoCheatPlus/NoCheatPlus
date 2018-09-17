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
package fr.neatmonster.nocheatplus.checks.net;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AttackMotion extends Check {

    public AttackMotion() {
        super(CheckType.NET_ATTACKMOTION);
    }

    public boolean check(final Player player, final long time,
                         final NetData data, final NetConfig cc, final IPlayerData pData) {
        boolean cancel = false;

        // The player attacked in the same tick as motion updates
        if (data.lastFlyingTime + cc.attackMotionTimeDiff > time) {

            // Add a vl depending on the time difference
            data.attackMotVL += (System.currentTimeMillis() - data.lastFlyingTime) + 12;

            if (data.attackMotVL > 120) {
                // Trigger a violation.
                final ViolationData vd = new ViolationData(this, player, data.attackMotVL / 12.0, 1.0, cc.attackMotionActions);
                if (pData.isDebugActive(type) || vd.needsParameters()) {
                    vd.setParameter(ParameterName.VIOLATIONS, Integer.toString(data.attackMotVL));
                }

                if (executeActions(vd).willCancel()) {
                    cancel = true;
                }
                // Feed Improbable.
                TickTask.requestImprobableUpdate(player.getUniqueId(), 4f);
            }
        } else {
            // Attack didnt come after motion updates
            // Decrease their vl
            data.attackMotVL *= 0.95;
        }

        return cancel;
    }
}
