package fr.neatmonster.nocheatplus.checks.net;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.net.model.DataPacketFlying;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

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

}
