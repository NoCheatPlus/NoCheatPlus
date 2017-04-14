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
package fr.neatmonster.nocheatplus.checks.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;

/**
 * Player specific dataFactory for the inventory checks.
 */
public class InventoryData extends ACheckData {

    /** The factory creating data. */
    public static final CheckDataFactory factory = new CheckDataFactory() {
        @Override
        public final ICheckData getData(final Player player) {
            return InventoryData.getData(player);
        }

        @Override
        public ICheckData getDataIfPresent(UUID playerId, String playerName) {
            return InventoryData.playersMap.get(playerName);
        }

        @Override
        public ICheckData removeData(final String playerName) {
            return InventoryData.removeData(playerName);
        }

        @Override
        public void removeAllData() {
            clear();
        }
    };

    /** The map containing the data per players. */
    private static final Map<String, InventoryData> playersMap = new HashMap<String, InventoryData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static InventoryData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new InventoryData(InventoryConfig.getConfig(player)));
        return playersMap.get(player.getName());
    }

    public static ICheckData removeData(final String playerName) {
        return playersMap.remove(playerName);
    }

    public static void clear(){
        playersMap.clear();
    }

    // Violation levels.
    public double   dropVL;
    public double   fastClickVL;
    public double   instantBowVL;
    public double   instantEatVL;

    // General.
    public long     lastClickTime = 0;

    // Data of the drop check.
    public int      dropCount;
    public long     dropLastTime;

    // Data of the fast click check.
    //    public boolean  fastClickLastCancelled;
    public final ActionFrequency fastClickFreq = new ActionFrequency(5, 200L);
    public Material fastClickLastCursor = null;
    public Material fastClickLastClicked = null;
    public int fastClickLastCursorAmount = 0;

    // Data of the instant bow check.
    /** Last time right click interact on bow. A value of 0 means 'invalid'.*/
    public long     instantBowInteract = 0;
    public long     instantBowShoot;

    // Data of the instant eat check.
    public Material instantEatFood;
    public long     instantEatInteract;

    public InventoryData(final InventoryConfig config) {
        super(config);
    }

}
