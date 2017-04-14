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
package fr.neatmonster.nocheatplus.checks.combined;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.utilities.TickTask;


/**
 * Static access API for shared use. This is actually not really a check, but access to combined or shared data.
 * @author mc_dev
 *
 */
public class Combined {

    /** All hits within this angle range are regarded as stationary. */
    private static float stationary = 32f;

    /**
     * Check if a penalty is set by changing horizontal facing dierection too often.
     * @param player
     * @param yaw
     * @param now
     * @param worldName
     * @return
     */
    public static final boolean checkYawRate(final Player player, final float yaw, final long now, 
            final String worldName) {
        return checkYawRate(player, yaw, now, worldName, CombinedData.getData(player));
    }

    /**
     * Feed horizontal facing direction.
     * @param player
     * @param yaw
     * @param now
     * @param worldName
     */
    public static final void feedYawRate(final Player player, final float yaw, final long now, 
            final String worldName) {
        feedYawRate(player, yaw, now, worldName, CombinedData.getData(player));
    }

    /**
     * Update the yaw change data. This will check for exemption and return
     * true, if the player is exempted from this check.
     * 
     * @param player
     * @param yaw
     * @param now
     * @param worldName
     * @param data
     * @return True, if the player was exempted from yawrate. False otherwise.
     */
    public static final boolean feedYawRate(final Player player, float yaw, final long now, 
            final String worldName, final CombinedData data) {
        // Check for exemption (hack, sort of).
        if (NCPExemptionManager.isExempted(player, CheckType.COMBINED_YAWRATE)) {
            resetYawRate(player, yaw, now, true);
            return true;
        }

        // Ensure the yaw is within bounds.
        if (yaw <= -360f) {
            yaw = -((-yaw) % 360f);
        }
        else if (yaw >= 360f) {
            yaw = yaw % 360f;
        }

        // Timeout, world change.
        if (now - data.lastYawTime > 999 || !worldName.equals(data.lastWorld)) {
            data.lastYaw = yaw;
            data.sumYaw = 0f;
            data.lastYawTime = now;
            data.lastWorld = worldName;
        }

        // Ensure difference is between -180, 180 (shortest used).
        float yawDiff = data.lastYaw - yaw;
        if (yawDiff < -180f) {
            yawDiff += 360f;
        }
        else if (yawDiff > 180f) {
            yawDiff -= 360f;
        }

        final long elapsed = now - data.lastYawTime;

        // Set data to current state.
        data.lastYaw = yaw;
        data.lastYawTime = now;

        final float dAbs = Math.abs(yawDiff);

        // Skip adding small changes.
        if (dAbs < stationary) {
            // This could also be done by keeping a "stationaryYaw" and taking the distance to yaw.
            data.sumYaw += yawDiff;
            if (Math.abs(data.sumYaw) < stationary) {
                // Still stationary, keep sum, add nothing.
                data.yawFreq.update(now);
                return false;
            }
            else {
                // Reset.
                data.sumYaw = 0f;
            }
        }
        else {
            data.sumYaw = 0f;
        }

        // Normalize yaw-change vs. elapsed time.
        final float dNorm = (float) dAbs / (float) (1 + elapsed);	

        data.yawFreq.add(now, dNorm);
        return false;
    }

    /**
     * This calls feedLastYaw and does nothing but set the freezing time to be
     * used by whatever check.
     * 
     * @param player
     * @param yaw
     * @param now
     * @param worldName
     * @return Classic 'cancel' state, i.e. true in case of a violation, false
     *         otherwise.
     */
    public static final boolean checkYawRate(final Player player, final float yaw, final long now, 
            final String worldName, final CombinedData data) {

        if (feedYawRate(player, yaw, now, worldName, data)) {
            return false;
        }

        final CombinedConfig cc = CombinedConfig.getConfig(player);

        final float threshold = cc.yawRate;

        // Angle diff per second
        final float stScore = data.yawFreq.bucketScore(0) * 3f; // TODO: Better have it 2.5 with lower threshold ?
        final float stViol;
        if (stScore > threshold) {
            // Account for server side lag.
            if (!cc.lag || TickTask.getLag(data.yawFreq.bucketDuration(), true) < 1.2) {
                stViol = stScore;
            }
            else {
                stViol = 0;
            }
        }
        else {
            stViol = 0f;
        }
        final float fullScore = data.yawFreq.score(1f);
        final float fullViol;
        if (fullScore > threshold) {
            // Account for server side lag.
            if (cc.lag) {
                fullViol = fullScore / TickTask.getLag(data.yawFreq.bucketDuration() * data.yawFreq.numberOfBuckets(), true);
            }
            else {
                fullViol = fullScore;
            }
        }
        else {
            fullViol = 0;
        }
        final float total = Math.max(stViol, fullViol);

        boolean cancel = false;
        if (total > threshold) {
            // Add time 
            final float amount = ((total - threshold) / threshold * 1000f);
            data.timeFreeze.applyPenalty(now, (long) Math.min(
                    Math.max(cc.yawRatePenaltyFactor * amount ,  cc.yawRatePenaltyMin), 
                    cc.yawRatePenaltyMax));
            // TODO: balance (100 ... 200 ) ?
            if (cc.yawRateImprobable && Improbable.check(player, amount / 100f, now, "combined.yawrate"))
                cancel = true;
        }
        if (data.timeFreeze.isPenalty()) {
            cancel = true;
        }
        return cancel;
    }

    /**
     * Reset the yawrate data to yaw and time. 
     * @param player
     * @param yaw
     * @param time
     * @param clear If to clear yaws.
     */
    public static final void resetYawRate(final Player player, float yaw, final long time, final boolean clear) {
        if (yaw <= -360f) {
            yaw = -((-yaw) % 360f);
        }
        else if (yaw >= 360f) {
            yaw = yaw % 360f;
        }
        final CombinedData data = CombinedData.getData(player);
        data.lastYaw = yaw;
        data.lastYawTime = time; // TODO: One might set to some past-time to allow any move at first.
        data.sumYaw = 0;
        if (clear) {
            data.yawFreq.clear(time);
        }
    }

    /**
     * Allow to pass a config flag if to check or only to feed.
     * @param player
     * @param yaw
     * @param now
     * @param worldName
     * @param yawRateCheck If to actually check the yaw rate, or just feed.
     * @return
     */
    public static final boolean checkYawRate(final Player player, final float yaw, final long now, final String worldName, final boolean yawRateCheck) {
        if (yawRateCheck) {
            return checkYawRate(player, yaw, now, worldName);
        }
        else {
            feedYawRate(player, yaw, now, worldName);
            return false;
        }
    }
}
