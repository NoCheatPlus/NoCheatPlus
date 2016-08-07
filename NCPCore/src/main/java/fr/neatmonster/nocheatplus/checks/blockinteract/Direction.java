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
import fr.neatmonster.nocheatplus.checks.moving.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.collision.CollideRayVsAABB;
import fr.neatmonster.nocheatplus.utilities.collision.ICollideRayVsAABB;

/**
 * The Direction check will find out if a player tried to interact with something that's not in their field of view.
 */
public class Direction extends Check {

    private final ICollideRayVsAABB boulder = new CollideRayVsAABB();

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

        // How far "off" is the player with their aim.
        final Vector direction = loc.getDirection();
        // Initialize fully each time.
        boulder.setFindNearestPointIfNotCollide(true)
        .setRay(loc.getX(), loc.getY() + player.getEyeHeight(), loc.getZ(), 
                direction.getX(), direction.getY(), direction.getZ())
        .setAABB(block.getX(), block.getY(), block.getZ(), 0.1)
        .loop();
        // TODO: if (boulder.collides()) { // Check flying queue.

        if (!boulder.collides()) {
            final double distance = Math.sqrt(boulder.getClosestDistanceSquared());

            if (data.debug) {
                outputDebugFail(player, boulder, distance);
            }

            // Add the overall violation level of the check.
            data.directionVL += distance;

            // TODO: Set distance parameter.

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.directionVL, distance, cc.directionActions).willCancel();
        } else {
            // Player did likely nothing wrong, reduce violation counter to reward them.
            data.directionVL *= 0.9D;
        }

        return cancel;
    }

    private void outputDebugFail(Player player, ICollideRayVsAABB boulder, double distance) {
        debug(player, "Failed: collides: " + boulder.collides() + " , dist: " + distance + " , pos: " + LocUtil.simpleFormat(boulder));
    }

}
