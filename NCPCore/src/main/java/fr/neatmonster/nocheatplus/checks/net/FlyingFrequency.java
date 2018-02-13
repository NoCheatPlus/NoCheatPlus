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

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.net.model.DataPacketFlying;
import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * Check frequency of (pos/look/) flying packets, disregarding packet content.
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
    public boolean check(final Player player, final DataPacketFlying packetData, final long time, 
            final NetData data, final NetConfig cc, final IPlayerData pData) {
        data.flyingFrequencyAll.add(time, 1f);
        final float allScore = data.flyingFrequencyAll.score(1f);
        if (allScore / cc.flyingFrequencySeconds > cc.flyingFrequencyPPS  
                && executeActions(player, allScore / cc.flyingFrequencySeconds - cc.flyingFrequencyPPS, 1.0 / cc.flyingFrequencySeconds, cc.flyingFrequencyActions).willCancel()) {
            return true;
        } else {
            return false;
        }
    }

}
