package fr.neatmonster.nocheatplus.checks.net;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

public class KeepAliveFrequency extends Check {

    public KeepAliveFrequency() {
        super(CheckType.NET_KEEPALIVEFREQUENCY);
    }

    /**
     * Checks hasBypass on violation only.
     * @param player
     * @param time
     * @param data
     * @param cc
     * @return If to cancel.
     */
    public boolean check(final Player player, final long time, final NetData data, final NetConfig cc) {
        data.keepAliveFreq.add(time, 1f);
        final float first = data.keepAliveFreq.bucketScore(0);
        if (first > 1f && !CheckUtils.hasBypass(CheckType.NET_KEEPALIVEFREQUENCY, player, data)) {
            // Trigger a violation.
            final double vl = Math.max(first - 1f, data.keepAliveFreq.score(1f) - data.keepAliveFreq.numberOfBuckets());
            if (executeActions(player, vl, 1.0, cc.keepAliveFrequencyActions).willCancel()) {
                return true;
            }
        }
        return false;
    }

}
