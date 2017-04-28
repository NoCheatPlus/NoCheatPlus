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
import fr.neatmonster.nocheatplus.checks.net.FlyingQueueHandle;
import fr.neatmonster.nocheatplus.checks.net.FlyingQueueLookBlockChecker;
import fr.neatmonster.nocheatplus.utilities.collision.CollideRayVsAABB;
import fr.neatmonster.nocheatplus.utilities.collision.ICollideRayVsAABB;
import fr.neatmonster.nocheatplus.utilities.location.LocUtil;

/**
 * The Direction check will find out if a player tried to interact with something that's not in their field of view.
 */
public class Direction extends Check {

    private final class BoulderChecker extends FlyingQueueLookBlockChecker {
        // (Not static for convenience.)

        private double minDistance;

        @Override
        protected boolean check(final double x, final double y, final double z, 
                final float yaw, final float pitch, 
                final int blockX, final int blockY, final int blockZ) {
            final double distance = checkBoulder(x, y, z, yaw, pitch, blockX, blockY, blockZ);
            if (distance == Double.MAX_VALUE) {
                // minDistance is not updated, in case the information is interesting ever.
                return true;
            }
            else {
                minDistance = Math.min(minDistance, distance);
                return false;
            }
        }

        @Override
        public boolean checkFlyingQueue(double x, double y, double z, float oldYaw, float oldPitch, int blockX,
                int blockY, int blockZ, FlyingQueueHandle flyingHandle) {
            minDistance = Double.MAX_VALUE;
            return super.checkFlyingQueue(x, y, z, oldYaw, oldPitch, blockX, blockY, blockZ, flyingHandle);
        }

        public double getMinDistance() {
            return minDistance;
        }

    }

    private final ICollideRayVsAABB boulder = new CollideRayVsAABB();
    private final Location useLoc = new Location(null, 0, 0, 0);

    private final BoulderChecker checker = new BoulderChecker();

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
    public boolean check(final Player player, final Location loc, final Block block, 
            final FlyingQueueHandle flyingHandle, final BlockInteractData data, final BlockInteractConfig cc) {

        boolean cancel = false;
        // How far "off" is the player with their aim.
        final double x = loc.getX();
        final double y = loc.getY() + player.getEyeHeight();
        final double z = loc.getZ();
        final int blockX = block.getX();
        final int blockY = block.getY();
        final int blockZ = block.getZ();
        // The distance is squared initially.
        double distance = checkBoulder(x, y, z, loc.getYaw(), loc.getPitch(), blockX, blockY, blockZ);
        if (distance != Double.MAX_VALUE) {
            if (checker.checkFlyingQueue(x, y, z, loc.getYaw(), loc.getPitch(), 
                    blockX, blockY, blockZ, flyingHandle)) {
                distance = Double.MAX_VALUE;
            }
            else {
                distance = Math.min(distance, checker.getMinDistance());
            }
        }

        if (distance != Double.MAX_VALUE) {
            distance = Math.sqrt(distance);
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

    /**
     * Check one configuration.
     * 
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     * @param blockX
     * @param blockY
     * @param blockZ
     * @return Double.MAX_VALUE if this passes the check, otherwise the squared
     *         violation distance (some measure).
     */
    private double checkBoulder(final double x, final double y, final double z,
            final float yaw, final float pitch,
            final int blockX, final int blockY, final int blockZ) {
        useLoc.setYaw(yaw);
        useLoc.setPitch(pitch);
        final Vector dir = useLoc.getDirection(); // TODO: More efficient.
        final double dirX = dir.getX();
        final double dirY = dir.getY();
        final double dirZ = dir.getZ();
        // Initialize fully each time.
        boulder.setFindNearestPointIfNotCollide(true)
        .setRay(x, y, z, dirX, dirY, dirZ)
        .setAABB(blockX, blockY, blockZ, 0.1)
        .loop();
        // Interpret result.
        if (boulder.collides()) {
            return Double.MAX_VALUE;
        }
        else {
            return boulder.getClosestDistanceSquared();
        }
    }

    private void outputDebugFail(Player player, ICollideRayVsAABB boulder, double distance) {
        debug(player, "Failed: collides: " + boulder.collides() + " , dist: " + distance + " , pos: " + LocUtil.simpleFormat(boulder));
    }

}
