package fr.neatmonster.nocheatplus.checks.combined;

import org.bukkit.entity.Player;


/**
 * Static access API for shared use. This is actually not really a check, but access to combined or shared data.
 * @author mc_dev
 *
 */
public class Combined {
	
	/**
	 * Check if a penalty is set by changing horizontal facing dierection too often.
	 * @param player
	 * @param yaw
	 * @param now
	 * @param worldName
	 * @return
	 */
	public static final boolean checkYaw(final Player player, final float yaw, final long now, final String worldName){
		return checkYaw(player, yaw, now, worldName, CombinedData.getData(player));
	}
	
	/**
	 * Feed horizontal facing direction.
	 * @param player
	 * @param yaw
	 * @param now
	 * @param worldName
	 */
	public static final void feedYaw(final Player player, final float yaw, final long now, final String worldName){
		feedYaw(player, yaw, now, worldName, CombinedData.getData(player));
	}
	
	/**
	 * Update the yaw change data.
	 * @param player
	 * @param yaw
	 * @param now
	 * @param worldName
	 * @param data
	 */
	private static final void feedYaw(final Player player, final float yaw, final long now, final String worldName, final CombinedData data) {
		// Reset on world change or timeout.
		if (now - data.lastYawTime > 999 || !worldName.equals(data.lastWorld)){
			data.lastYaw = yaw;
			data.lastYawTime = now;
			data.lastWorld = worldName;
		}
		
		final float yawDiff = (yaw - data.lastYaw) % 180;
		final long elapsed = now - data.lastYawTime;
		
		// Set data to current state.
		data.lastYaw = yaw;
		data.lastYawTime = now;
		
		final float dAbs = Math.abs(yawDiff);
		final float dNorm = (float) dAbs / (float) (1 + elapsed);	
		
		data.yawFreq.add(now, dNorm);
	}

	/**
	 * This calls feedLastYaw and does nothing but set the freezing time to be used by whatever check.
	 * @param player
	 * @param yaw
	 * @param now
	 * @param worldName
	 * @return
	 */
	private static final boolean checkYaw(Player player, float yaw, long now, final String worldName, final CombinedData data) {

		feedYaw(player, yaw, now, worldName, data);
		
		final CombinedConfig cc = CombinedConfig.getConfig(player);
		
		// Angle diff per second
		final float total = Math.max(data.yawFreq.getScore(1f), data.yawFreq.getScore(0) * 3f);
		final float threshold = cc.lastYawRate;
		if (total > threshold){
			// Add time 
			data.timeFreeze = Math.max(data.timeFreeze, now + (long) ((total - threshold) / threshold * 1000f));
		}
		return now < data.timeFreeze;
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
	public static final boolean checkYaw(final Player player, final float yaw, final long now, final String worldName, final boolean yawRateCheck) {
		if (yawRateCheck) return checkYaw(player, yaw, now, worldName);
		else {
			feedYaw(player, yaw, now, worldName);
			return false;
		}
	}
}
