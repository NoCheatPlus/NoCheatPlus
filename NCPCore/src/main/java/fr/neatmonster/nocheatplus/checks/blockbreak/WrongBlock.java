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
package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;

public class WrongBlock extends Check {

    public WrongBlock() {
        super(CheckType.BLOCKBREAK_WRONGBLOCK);
    }

    /**
     * Check if the player destroys another block than interacted with last.<br>
     * This does occasionally trigger for players that destroy grass or snow, 
     * probably due to packet delaying issues for insta breaking.
     * @param player
     * @param block
     * @param data 
     * @param cc 
     * @param isInstaBreak 
     * @return
     */
    public boolean check(final Player player, final Block block, final BlockBreakConfig cc, final BlockBreakData data, final AlmostBoolean isInstaBreak) {

        boolean cancel = false;

        final boolean wrongTime = data.fastBreakfirstDamage < data.fastBreakBreakTime;
        final int dist = Math.min(4, data.clickedX == Integer.MAX_VALUE ? 100 : TrigUtil.manhattan(data.clickedX, data.clickedY, data.clickedZ, block));
        final boolean wrongBlock;
        final long now = System.currentTimeMillis();
        // TODO: Remove isInstaBreak argument or use it.
        if (dist == 0) {
            if (wrongTime) {
                data.fastBreakBreakTime = now;
                data.fastBreakfirstDamage = now;
                // Could set to wrong block, but prefer to transform it into a quasi insta break.
            }
            wrongBlock = false;
        }
        else if (dist == 1) {
            // One might to a concession in case of instant breaking.
            if (now - data.wasInstaBreak < 60) {
                wrongBlock = false;
            }
            else {
                wrongBlock = true;
            }
        }
        else {
            // Note that the maximally counted distance is set above.
            wrongBlock = true;
        }

        if (wrongBlock) {
            if ((data.debug) && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)) {
                player.sendMessage("WrongBlock failure with dist: " + dist);
            }
            data.wrongBlockVL.add(now, (float) (dist + 1) / 2f);
            final float score = data.wrongBlockVL.score(0.9f);
            if (score > cc.wrongBLockLevel) {
                if (executeActions(player, score, 1D, cc.wrongBlockActions).willCancel()) {
                    cancel = true;
                }
                if (Improbable.check(player, 2.0f, now, "blockbreak.wrongblock")) {
                    cancel = true;
                }
            }
        }

        return cancel;
    }

}
