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

import fr.neatmonster.nocheatplus.checks.net.model.DataPacketFlying;

/**
 * A simple loop through checker for the flying queue feature. 
 * @author asofold
 *
 */
public abstract class FlyingQueueLookBlockChecker {

    /**
     * Override to prevent setting elements that can't be used or for which
     * check returned false to null.
     */
    protected boolean invalidateFailed = true;

    /**
     * Run the check with the given oldPitch and oldYaw, unless invalidated.
     * invalidateFailed applies.
     */
    protected boolean checkOldLook = true;

    protected abstract boolean check(final double x, final double y, final double z, 
            final float yaw, final float pitch,
            final int blockX, final int blockY, final int blockZ);

    public FlyingQueueLookBlockChecker setInvalidateFailed(boolean invalidatedFailed) {
        this.invalidateFailed = invalidatedFailed;
        return this;
    }

    public FlyingQueueLookBlockChecker setCheckOldLook(boolean checkOldLook) {
        this.checkOldLook = checkOldLook;
        return this;
    }

    /**
     * Run check with the given start position (e.g. eye coordinates), but use
     * yaw and pitch from the flying queue. Non matching entries are nulled,
     * unless setUnusableToNull is set to false.
     * 
     * @param x
     * @param y
     * @param z
     * @param oldYaw
     * @param oldPitch
     * @param blockX
     * @param blockY
     * @param blockZ
     * @param flyingHandle
     * @return True, if check returned true (the first time is returned). False
     *         if the queue is empty or check has not returned true for any
     *         contained element. Special return values have to be set
     *         elsewhere. An empty queue also yields false as return value.
     */
    public boolean checkFlyingQueue(final double x, final double y, final double z, 
            final float oldYaw, final float oldPitch,
            final int blockX, final int blockY, final int blockZ, final FlyingQueueHandle flyingHandle) {
        if (checkOldLook && flyingHandle.isCurrentLocationValid()) {
            if (check(x, y, z, oldYaw, oldPitch, blockX, blockY, blockZ)) {
                return true;
            }
            else {
                // Invalidate.
                flyingHandle.setCurrentLocationValid(false);
            }
        }
        final DataPacketFlying[] queue = flyingHandle.getHandle();
        if (queue.length == 0) {
            return false;
        }
        for (int i = 0; i < queue.length; i++) {
            final DataPacketFlying packetData = queue[i];
            if (packetData == null) {
                continue;
            }
            if (!packetData.hasLook) {
                if (invalidateFailed) {
                    queue[i] = null;
                }
                continue;
            }
            final float yaw = packetData.getYaw();
            final float pitch = packetData.getPitch();
            // Simple heuristic: reduce impact of checking by skipping redundant entries.
            // TODO: Other heuristic / what's typical? 
            if (yaw == oldYaw && pitch == oldPitch) {
                if (invalidateFailed) {
                    queue[i] = null;
                }
                continue;
            }
            // TODO: Consider support some other type of metric (possibly checking positions too?);
            if (check(x, y, z, yaw, pitch, blockX, blockY, blockZ)) {
                // TODO: Consider to remember index and entry as well?
                return true;
            }
            else {
                if (invalidateFailed) {
                    queue[i] = null;
                }
            }
        }
        return false;
    }

}
