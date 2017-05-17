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
package fr.neatmonster.nocheatplus.checks.blockbreak;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.stats.Timings;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

/**
 * Player specific data for the block break checks.
 */
public class BlockBreakData extends ACheckData {

    /** The factory creating data. */
    public static final CheckDataFactory factory = new CheckDataFactory() {
        @Override
        public final ICheckData getData(final Player player) {
            return BlockBreakData.getData(player);
        }

        @Override
        public ICheckData getDataIfPresent(UUID playerId, String playerName) {
            return BlockBreakData.playersMap.get(playerName);
        }

        @Override
        public ICheckData removeData(final String playerName) {
            return BlockBreakData.removeData(playerName);
        }

        @Override
        public void removeAllData() {
            clear();
        }
    };

    /** The map containing the data per players. */
    private static final Map<String, BlockBreakData> playersMap = new HashMap<String, BlockBreakData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static BlockBreakData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new BlockBreakData(BlockBreakConfig.getConfig(player)));
        return playersMap.get(player.getName());
    }

    public static ICheckData removeData(final String playerName) {
        return playersMap.remove(playerName);
    }

    public static void clear(){
        playersMap.clear();
    }

    // Violation levels.
    public double  directionVL;
    public double  fastBreakVL;
    public double  frequencyVL;
    public double  noSwingVL;
    public double  reachVL;
    public final ActionFrequency  wrongBlockVL;

    // Shared data.
    public int     clickedX = Integer.MAX_VALUE;
    public int     clickedY;
    public int     clickedZ;
    public int     clickedTick;
    /** Tool that the block was clicked with, null for the case of air. */
    public Material clickedTool = null;

    // TODO: use tick here too  ?
    public long    wasInstaBreak;

    public Timings stats;

    // Data of the fast break check.
    public final ActionFrequency fastBreakPenalties;
    public long    fastBreakBreakTime  = System.currentTimeMillis() - 1000L;
    /** First time interaction with a block. */
    public long    fastBreakfirstDamage = System.currentTimeMillis();

    public final ActionFrequency frequencyBuckets;
    public int     frequencyShortTermCount;
    public int     frequencyShortTermTick;

    // Data of the no swing check.
    public boolean noSwingArmSwung     = true;

    // Data of the reach check.
    public double  reachDistance;


    public BlockBreakData(final BlockBreakConfig cc) {
        super(cc);
        setStats();
        fastBreakPenalties = new ActionFrequency(cc.fastBreakBuckets, cc.fastBreakBucketDur);
        frequencyBuckets = new ActionFrequency(cc.frequencyBuckets, cc.frequencyBucketDur);
        wrongBlockVL = new ActionFrequency(6, 20000);
    }

    @Override
    public void setDebug(boolean debug) {
        super.setDebug(debug);
        setStats();
    }

    private void setStats() {
        if (getDebug()) {
            if (stats == null) {
                stats = new Timings("NCP/FASTBREAK");
            }
        } else {
            stats = null;
        }
    }

    /**
     * Meant to record the first click/damage on a block (not subsequent clicking), forces internals update.
     * @param block
     * @param tick
     * @param now
     * @param mat 
     */
    public void setClickedBlock(Block block, int tick, long now, Material tool) {
        fastBreakfirstDamage = now;
        // Also set last clicked blocks position.
        clickedX = block.getX();
        clickedY = block.getY();
        clickedZ = block.getZ();
        clickedTick = tick;
        clickedTool = tool == Material.AIR ? null : tool;
    }

    /**
     * Reset clicked block (as if not clicked anything before).
     */
    public void resetClickedBlock() {
        clickedX = Integer.MAX_VALUE;
        clickedTick = 0;
        fastBreakfirstDamage = 0;
        clickedTool = null;
    }

    public boolean toolChanged(ItemStack stack) {
        return toolChanged(stack == null ? null: stack.getType());
    }

    public boolean toolChanged(Material mat) {
        if (BlockProperties.isAir(mat)) {
            return !BlockProperties.isAir(clickedTool);
        } else {
            return clickedTool != mat;
        }
    }

}
