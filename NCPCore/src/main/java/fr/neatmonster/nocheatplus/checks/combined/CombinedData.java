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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.PenaltyTime;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;

public class CombinedData extends ACheckData {

    /** The factory creating data. */
    public static final CheckDataFactory factory = new CheckDataFactory() {
        @Override
        public final ICheckData getData(final Player player) {
            return CombinedData.getData(player);
        }

        @Override
        public ICheckData removeData(final String playerName) {
            return CombinedData.removeData(playerName);
        }

        @Override
        public void removeAllData() {
            clear();
        }
    };

    private static final Map<String, CombinedData> playersMap = new HashMap<String, CombinedData>();

    public static CombinedData getData(final Player player) {
        final String playerName = player.getName(); 
        CombinedData data = playersMap.get(playerName);
        if (data == null){
            data = new CombinedData(CombinedConfig.getConfig(player));
            playersMap.put(playerName, data);
        }
        return data;
    }

    public static ICheckData removeData(final String playerName) {
        return playersMap.remove(playerName);
    }

    public static void clear(){
        playersMap.clear();
    }

    // VLs
    public double bedLeaveVL = 0;
    public double improbableVL = 0;
    public double munchHausenVL = 0;

    // Invulnerable management:
    /** This is the tick from which on the player is vulnerable again. */
    public int            invulnerableTick = Integer.MIN_VALUE;

    // Yawrate check.
    public float lastYaw;
    public long  lastYawTime;
    public float sumYaw;
    public final ActionFrequency yawFreq = new ActionFrequency(3, 333);

    // General penalty time. Used for fighting mainly, but not only close combat (!), set by yawrate check.
    public final PenaltyTime timeFreeze = new PenaltyTime();

    // Bedleave check
    public boolean wasInBed = false;

    // Improbable check
    public final ActionFrequency improbableCount = new ActionFrequency(20, 3000);

    // General data
    public String lastWorld = "";
    public long lastJoinTime;
    public long lastLogoutTime;
    public long lastMoveTime;

    public CombinedData(final CombinedConfig config){
        super(config);
    }

}
