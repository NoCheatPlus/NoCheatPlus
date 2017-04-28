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
    protected boolean setUnusableToNull = true;

    protected abstract boolean check(final double x, final double y, final double z, 
            final float yaw, final float pitch,
            final int blockX, final int blockY, final int blockZ);

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
                if (setUnusableToNull) {
                    queue[i] = null;
                }
                continue;
            }
            final float yaw = packetData.getYaw();
            final float pitch = packetData.getPitch();
            // Simple heuristic: reduce impact of checking by skipping redundant entries.
            // TODO: Other heuristic / what's typical? 
            if (yaw == oldYaw && pitch == oldPitch) {
                if (setUnusableToNull) {
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
                if (setUnusableToNull) {
                    queue[i] = null;
                }
            }
        }
        return false;
    }

}
