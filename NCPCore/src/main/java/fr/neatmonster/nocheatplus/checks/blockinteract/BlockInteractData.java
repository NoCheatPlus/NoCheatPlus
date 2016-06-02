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
package fr.neatmonster.nocheatplus.checks.blockinteract;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * Player specific data for the block interact checks.
 */
public class BlockInteractData extends ACheckData {

    /** The factory creating data. */
    public static final CheckDataFactory factory = new CheckDataFactory() {
        @Override
        public final ICheckData getData(final Player player) {
            return BlockInteractData.getData(player);
        }

        @Override
        public ICheckData removeData(final String playerName) {
            return BlockInteractData.removeData(playerName);
        }

        @Override
        public void removeAllData() {
            clear();
        }
    };

    /** The map containing the data per players. */
    private static final Map<String, BlockInteractData> playersMap = new HashMap<String, BlockInteractData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static BlockInteractData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new BlockInteractData(BlockInteractConfig.getConfig(player)));
        return playersMap.get(player.getName());
    }

    public static ICheckData removeData(final String playerName) {
        return playersMap.remove(playerName);
    }

    public static void clear(){
        playersMap.clear();
    }

    // Violation levels.
    public double directionVL	= 0;
    public double reachVL		= 0;
    public double speedVL		= 0;
    public double visibleVL		= 0;

    // General data
    // Last block interacted with
    public int lastX = Integer.MAX_VALUE;
    public int lastY, lastZ;
    /** null for air */
    public Material lastType = null;
    public long lastTick;
    public Action lastAction = null;

    // Data of the reach check.
    public double reachDistance;

    /** Last reset time. */
    public long speedTime	= 0;
    /** Number of interactions since last reset-time. */
    public int  speedCount	= 0;

    public BlockInteractData(final BlockInteractConfig config) {
        super(config);
    }

    /**
     * Last interacted block.
     * @param block
     */
    public void setLastBlock(Block block, Action action) {
        lastX = block.getX();
        lastY = block.getY();
        lastZ = block.getZ();
        lastType = block.getType();
        if (lastType == Material.AIR) {
            lastType = null;
        }
        lastTick = TickTask.getTick();
        lastAction = action;
    }

    public void resetLastBlock() {
        lastTick = 0;
        lastAction = null;
        lastX = Integer.MAX_VALUE;
        lastType = null;
    }

}
