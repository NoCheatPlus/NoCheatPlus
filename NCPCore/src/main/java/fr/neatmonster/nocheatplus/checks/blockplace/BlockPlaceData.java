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
package fr.neatmonster.nocheatplus.checks.blockplace;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;

/**
 * Player specific dataFactory for the block place checks.
 */
public class BlockPlaceData extends ACheckData {

    /** The factory creating data. */
    public static final CheckDataFactory factory = new CheckDataFactory() {
        @Override
        public final ICheckData getData(final Player player) {
            return BlockPlaceData.getData(player);
        }

        @Override
        public ICheckData removeData(final String playerName) {
            return BlockPlaceData.removeData(playerName);
        }

        @Override
        public void removeAllData() {
            clear();
        }
    };

    /** The map containing the data per players. */
    private static final Map<String, BlockPlaceData> playersMap = new HashMap<String, BlockPlaceData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static BlockPlaceData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new BlockPlaceData(BlockPlaceConfig.getConfig(player)));
        return playersMap.get(player.getName());
    }

    public static ICheckData removeData(final String playerName) {
        return playersMap.remove(playerName);
    }

    public static void clear(){
        playersMap.clear();
    }

    // Violation levels.
    public double againstVL = 0;
    public double autoSignVL = 0;
    public double directionVL = 0;
    public double fastPlaceVL = 0;
    public double noSwingVL = 0;
    public double reachVL = 0;
    public double speedVL = 0;

    // AutoSign.
    public long autoSignPlacedTime = 0;
    /** Using Material.SIGN . */
    public long autoSignPlacedHash = 0;

    // Data of the fast place check.
    public final ActionFrequency fastPlaceBuckets	= new ActionFrequency(2, 1000);
    public int fastPlaceShortTermTick				= 0;
    public int fastPlaceShortTermCount				= 0;

    // Data of the no swing check.
    public boolean noSwingArmSwung = true;

    // Data of the reach check.
    public double  reachDistance;

    // Data of the speed check;
    public boolean speedLastRefused;
    public long    speedLastTime;

    public BlockPlaceData(final BlockPlaceConfig config) {
        super (config);
    }

}
