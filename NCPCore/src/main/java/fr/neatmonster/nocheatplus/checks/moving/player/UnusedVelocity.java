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
package fr.neatmonster.nocheatplus.checks.moving.player;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.velocity.SimpleAxisVelocity;
import fr.neatmonster.nocheatplus.checks.moving.velocity.UnusedTracker;
import fr.neatmonster.nocheatplus.logging.debug.DebugUtil;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

public class UnusedVelocity {
    
    // TODO: Currently is debug only (!). Only call if some debug flag is set. Needs checks.moving.debug to work at all.

    /**
     * Test if any unused velocity has been tracked and what that means. (Convenience.)
     * 
     * @param player
     * @return If the player has failed the check, whatever that means.
     */
    public static boolean checkUnusedVelocity(final Player player, 
            final CheckType checkType, final IPlayerData pData) {
        return checkUnusedVelocity(
                player, checkType, 
                pData.getGenericInstance(MovingData.class), 
                pData.getGenericInstance(MovingConfig.class)
                );
    }


    /**
     * Test if any unused velocity has been tracked and what that means.
     * 
     * @param player
     * @param data
     * @param cc
     * @return If the player has failed the check, whatever that means.
     */
    public static boolean checkUnusedVelocity(final Player player, 
            final CheckType checkType, final MovingData data, final MovingConfig cc) {
        boolean violation = false;
        final SimpleAxisVelocity verVel = data.getVerticalVelocityTracker();
        violation |= quickCheckDirection(player, verVel.unusedTrackerPos, checkType, "vert/pos", data, cc);
        violation |= quickCheckDirection(player, verVel.unusedTrackerNeg, checkType, "vert/neg", data, cc);
        return violation;
    }

    /**
     * Quick check tracker for one direction.
     * @param player
     * @param checkType 
     * @param unusedTrackerPos
     * @param string
     * @param data
     * @param cc
     */
    private static boolean quickCheckDirection(final Player player, 
            final UnusedTracker tracker, final CheckType checkType, final String trackerId ,
            MovingData data, MovingConfig cc) {
        final int countV = tracker.getResultViolationCount();
        if (countV > 0) {
            String msg = "Unused velocity " + trackerId + ": c_v: " + countV + " , a_v: " + tracker.getResultViolationAmount() + " , c_u: " + tracker.getResultUpdateCount();
            DebugUtil.debug(CheckUtils.getLogMessagePrefix(player, checkType) + msg);
        }
        tracker.resetResults(); // Reset for now.
        // TODO: A real check, e.g. track frequency and/or average amount of violation per so and so time.
        return false; // Player can't possibly fail this yet.
    }

}
