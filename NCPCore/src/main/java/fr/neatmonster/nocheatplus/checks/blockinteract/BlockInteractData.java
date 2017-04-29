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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;

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
        public ICheckData getDataIfPresent(UUID playerId, String playerName) {
            return BlockInteractData.playersMap.get(playerName);
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
    /** Set to Integer.MAX_VALUE for reset. */
    private int lastX = Integer.MAX_VALUE;
    private int lastY, lastZ;
    /** null for air */
    public Material lastType = null;
    public int lastTick;
    public Action lastAction = null;

    // Data of the reach check.
    public double reachDistance;

    /** Last reset time. */
    public long speedTime	= 0;
    /** Number of interactions since last reset-time. */
    public int  speedCount	= 0;

    /** Cancel is set, times in a row. */
    public int subsequentCancel = 0;

    /**
     * Skipped actions due to rate limiting (debugging rather). May be reset
     * with logging.
     */
    public int rateLimitSkip = 0;

    private final Set<CheckType> passedChecks = new HashSet<CheckType>();

    public BlockInteractData(final BlockInteractConfig config) {
        super(config);
    }

    /**
     * Last interacted block.
     * @param block
     */
    public void setLastBlock(final Block block, final Action action) {
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

    /**
     * Full state comparison.
     * @param material
     * @param action
     * @param tick
     * @param block
     * @return
     */
    public boolean matchesLastBlock(final Material material, final Action action, final int tick, final Block block) {
        return lastX != Integer.MAX_VALUE && material == lastType && matchesLastBlock(action, tick, block);
    }

    /**
     * Compare action, tick and block.
     * 
     * @param action
     * @param tick
     * @param block
     * @return
     */
    public boolean matchesLastBlock(final Action action, final int tick, final Block block) {
        return lastX != Integer.MAX_VALUE && tick == lastTick && matchesLastBlock(action, block);
    }

    /**
     * Compare only action and coordinates.
     * 
     * @param action
     * @param block
     * @return
     */
    public boolean matchesLastBlock(final Action action, final Block block) {
        return lastX != Integer.MAX_VALUE && action == lastAction && matchesLastBlock(block);
    }

    /**
     * Compare only tick and coordinates.
     * 
     * @param action
     * @param block
     * @return
     */
    public boolean matchesLastBlock(final int tick, final Block block) {
        return lastX != Integer.MAX_VALUE && tick == lastTick && matchesLastBlock(block);
    }


    /**
     * Compare the block coordinates.
     * 
     * @param block
     * @return
     */
    public boolean matchesLastBlock(final Block block) {
        // Skip the MAX_VALUE check.
        return lastX == block.getX() && lastY == block.getY() && lastZ == block.getZ();
    }

    /**
     * The Manhattan distance to the set last block, Integer.MAX_VALUE is
     * returned if none is set.
     * 
     * @param block
     * @return The Manhattan distance if appropriate - if no block is set,
     *         Integer.MAX_VALUE is returned.
     */
    public int manhattanLastBlock(final Block block) {
        return lastX == Integer.MAX_VALUE ? Integer.MAX_VALUE : TrigUtil.manhattan(lastX, lastY, lastZ,
                block.getX(), block.getY(), block.getZ());
    }

    /**
     * Resets the last block (and passed checks).
     */
    public void resetLastBlock() {
        lastTick = 0;
        lastAction = null;
        lastX = Integer.MAX_VALUE;
        lastType = null;
        resetPassedChecks();
    }

    /**
     * Reset passed checks (concern the last block interacted with).
     */
    public void resetPassedChecks() {
        passedChecks.clear();
    }

    public void addPassedCheck(final CheckType checkType) {
        passedChecks.add(checkType);
    }

    /**
     * Check if this check type was set as passed for the last block.
     * 
     * @param checkType
     * @return
     */
    public boolean isPassedCheck(final CheckType checkType) {
        return passedChecks.contains(checkType);
    }

    /**
     * Adjustments, disregarding who/what cancelled the event.
     */
    public void onCancelledBlockInteractEvent() {
        resetLastBlock();
    }

}
