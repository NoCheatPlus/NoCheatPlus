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
package fr.neatmonster.nocheatplus.checks.blockinteract;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.collision.CollisionUtil;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;

/**
 * The Direction check will find out if a player tried to interact with something that's not in their field of view.
 */
public class Direction extends Check {

    /**
     * Instantiates a new direction check.
     */
    public Direction() {
        super(CheckType.BLOCKINTERACT_DIRECTION);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param blockLocation
     *            the location
     * @return true, if successful
     */
    public boolean check(final Player player, final Location loc, final Block block, final BlockInteractData data, final BlockInteractConfig cc) {

        boolean cancel = false;

        // How far "off" is the player with their aim. We calculate from the players eye location and view direction to
        // the center of the target block. If the line of sight is more too far off, "off" will be bigger than 0.
        final Vector direction = loc.getDirection();
        final double off = CollisionUtil.directionCheck(loc, player.getEyeHeight(), direction, block, TrigUtil.DIRECTION_PRECISION);

        if (off > 0.1D) {
            // Player failed the check. Let's try to guess how far they were from looking directly to the block...
            final Vector blockEyes = new Vector(0.5 + block.getX() - loc.getX(), 0.5 + block.getY() - loc.getY() - player.getEyeHeight(), 0.5 + block.getZ() - loc.getZ());
            final double distance = blockEyes.crossProduct(direction).length() / direction.length();

            // Add the overall violation level of the check.
            data.directionVL += distance;

            // TODO: Set distance parameter.

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.directionVL, distance, cc.directionActions).willCancel();
        } else
            // Player did likely nothing wrong, reduce violation counter to reward them.
            data.directionVL *= 0.9D;

        return cancel;
    }
}
