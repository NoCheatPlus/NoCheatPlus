package fr.neatmonster.nocheatplus.checks.net;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.net.model.DataPacketFlying;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Frequency of (pos/look/) flying packets checking.
 * 
 * @author asofold
 *
 */
public class FlyingFrequency extends Check {

    // Thresholds for firing moving events (CraftBukkit). TODO: Move to some model thing in NCPCore, possibly a ServerConfig?
    public static final double minMoveDistSq = 1f / 256; // PlayerConnection magic.
    public static final float minLookChange = 10f;

    public FlyingFrequency() {
        super(CheckType.NET_FLYINGFREQUENCY);
    }

    /**
     * Always update data, check bypass on violation only.
     * 
     * @param player
     * @param time
     * @param data
     * @param cc
     * @return
     */
    public boolean check(final Player player, final DataPacketFlying packetData, final long time, final NetData data, final NetConfig cc) {
        data.flyingFrequencyAll.add(time, 1f);
        final float allScore = data.flyingFrequencyAll.score(1f);
        if (allScore / cc.flyingFrequencySeconds > cc.flyingFrequencyPPS && !CheckUtils.hasBypass(CheckType.NET_FLYINGFREQUENCY, player, data) && executeActions(player, allScore / cc.flyingFrequencySeconds - cc.flyingFrequencyPPS, 1.0 / cc.flyingFrequencySeconds, cc.flyingFrequencyActions)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <b>Currently not used (too many potential and actual issues).</b><br>
     * Skip packets that are not going to cause a moving event to fire, in case
     * the frequency of packets is high.
     * 
     * @param player
     * @param packetData
     * @param allScore
     * @param time
     * @param data
     * @param cc
     * @return
     */
    @SuppressWarnings("unused")
    private boolean checkRedundantPackets(final Player player, final DataPacketFlying packetData, final float allScore, final long time, final NetData data, final NetConfig cc) {
        // TODO: Debug logging (better with integration into DataManager).
        // TODO: Consider to compare to moving data directly, skip keeping track extra.

        final MovingData mData = MovingData.getData(player);
        if (mData.toX == Double.MAX_VALUE && mData.toYaw == Float.MAX_VALUE) {
            // Can not check.
            return false;
        }

        boolean onGroundSkip = false;

        // Allow at least one on-ground change per state and second.
        // TODO: Consider to verify on ground somehow (could tell MovingData the state).
        if (packetData.onGround != data.flyingFrequencyOnGround) {
            // Regard as not redundant only if sending the same state happened at least a second ago.
            final long lastTime;
            if (packetData.onGround) {
                lastTime = data.flyingFrequencyTimeOnGround;
                data.flyingFrequencyTimeOnGround = time;
            } else {
                lastTime = data.flyingFrequencyTimeNotOnGround;
                data.flyingFrequencyTimeNotOnGround = time;
            }
            if (time < lastTime || time - lastTime > 1000) {
                // Override 
                onGroundSkip = true;
            }
        }
        data.flyingFrequencyOnGround = packetData.onGround;

        if (packetData.hasPos) {
            if (TrigUtil.distanceSquared(packetData.x, packetData.y, packetData.z, mData.toX, mData.toY, mData.toZ) > minMoveDistSq) {
                return false;
            }
        }

        if (packetData.hasLook) {
            if (Math.abs(TrigUtil.yawDiff(packetData.yaw, mData.toYaw)) > minLookChange || Math.abs(TrigUtil.yawDiff(packetData.pitch, mData.toPitch)) > minLookChange) {
                return false;
            }
        }

        if (onGroundSkip) {
            return false;
        }

        // Packet is redundant, if more than 20 packets per second arrive.
        if (allScore / cc.flyingFrequencySeconds > 20f && !hasBypass(player)) {
            // (Must re-check bypass here.)
            data.flyingFrequencyRedundantFreq.add(time, 1f);
            if (executeActions(player, data.flyingFrequencyRedundantFreq.score(1f) / cc.flyingFrequencyRedundantSeconds, 1.0 / cc.flyingFrequencyRedundantSeconds, cc.flyingFrequencyRedundantActions)) {
                return true;
            }
        }
        return false;
    }

}
