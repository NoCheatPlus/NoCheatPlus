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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Configurations specific for the block place checks. Every world gets one of these assigned to it, or if a world
 * doesn't get it's own, it will use the "global" version.
 */
public class BlockPlaceConfig extends ACheckConfig {

    /** The factory creating configurations. */
    public static final CheckConfigFactory factory = new CheckConfigFactory() {
        @Override
        public final ICheckConfig getConfig(final Player player) {
            return BlockPlaceConfig.getConfig(player);
        }

        @Override
        public void removeAllConfigs() {
            clear(); // Band-aid.
        }
    };

    /** The map containing the configurations per world. */
    private static final Map<String, BlockPlaceConfig> worldsMap = new HashMap<String, BlockPlaceConfig>();

    /**
     * Clear all the configurations.
     */
    public static void clear() {
        worldsMap.clear();
    }

    /**
     * Gets the configuration for a specified player.
     * 
     * @param player
     *            the player
     * @return the configuration
     */
    public static BlockPlaceConfig getConfig(final Player player) {
        if (!worldsMap.containsKey(player.getWorld().getName()))
            worldsMap.put(player.getWorld().getName(),
                    new BlockPlaceConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
        return worldsMap.get(player.getWorld().getName());
    }

    public final boolean	againstCheck;
    public final ActionList againstActions;

    public final boolean    autoSignCheck;
    public final boolean    autoSignSkipEmpty;
    public final ActionList autoSignActions;

    public final boolean    directionCheck;
    public final ActionList directionActions;

    public final boolean    fastPlaceCheck;
    public final int        fastPlaceLimit;
    public final int        fastPlaceShortTermTicks;
    public final int        fastPlaceShortTermLimit;
    public final ActionList fastPlaceActions;

    public final boolean    noSwingCheck;
    public final Set<Material> noSwingExceptions = new HashSet<Material>();
    public final ActionList noSwingActions;

    public final boolean    reachCheck;
    public final ActionList reachActions;

    public final boolean    speedCheck;
    public final long       speedInterval;
    public final ActionList speedActions;

    /** General activation flag. */
    public final boolean preventBoatsAnywhere;

    /**
     * Instantiates a new block place configuration.
     * 
     * @param config
     * 
     */
    public BlockPlaceConfig(final ConfigFile config) {
        super(config, ConfPaths.BLOCKPLACE);

        againstCheck = config.getBoolean(ConfPaths.BLOCKPLACE_AGAINST_CHECK);
        againstActions = config.getOptimizedActionList(ConfPaths.BLOCKPLACE_AGAINST_ACTIONS, Permissions.BLOCKPLACE_AGAINST);

        autoSignCheck = config.getBoolean(ConfPaths.BLOCKPLACE_AUTOSIGN_CHECK);
        autoSignSkipEmpty = config.getBoolean(ConfPaths.BLOCKPLACE_AUTOSIGN_SKIPEMPTY);
        autoSignActions = config.getOptimizedActionList(ConfPaths.BLOCKPLACE_AUTOSIGN_ACTIONS, Permissions.BLOCKPLACE_AUTOSIGN);


        directionCheck = config.getBoolean(ConfPaths.BLOCKPLACE_DIRECTION_CHECK);
        directionActions = config.getOptimizedActionList(ConfPaths.BLOCKPLACE_DIRECTION_ACTIONS, Permissions.BLOCKPLACE_DIRECTION);

        fastPlaceCheck = config.getBoolean(ConfPaths.BLOCKPLACE_FASTPLACE_CHECK);
        fastPlaceLimit = config.getInt(ConfPaths.BLOCKPLACE_FASTPLACE_LIMIT);
        fastPlaceShortTermTicks = config.getInt(ConfPaths.BLOCKPLACE_FASTPLACE_SHORTTERM_TICKS);
        fastPlaceShortTermLimit = config.getInt(ConfPaths.BLOCKPLACE_FASTPLACE_SHORTTERM_LIMIT);
        fastPlaceActions = config.getOptimizedActionList(ConfPaths.BLOCKPLACE_FASTPLACE_ACTIONS, Permissions.BLOCKPLACE_FASTPLACE);

        noSwingCheck = config.getBoolean(ConfPaths.BLOCKPLACE_NOSWING_CHECK);
        config.readMaterialFromList(ConfPaths.BLOCKPLACE_NOSWING_EXCEPTIONS, noSwingExceptions);
        noSwingActions = config.getOptimizedActionList(ConfPaths.BLOCKPLACE_NOSWING_ACTIONS, Permissions.BLOCKPLACE_NOSWING);

        reachCheck = config.getBoolean(ConfPaths.BLOCKPLACE_REACH_CHECK);
        reachActions = config.getOptimizedActionList(ConfPaths.BLOCKPLACE_REACH_ACTIONS, Permissions.BLOCKPLACE_REACH);

        speedCheck = config.getBoolean(ConfPaths.BLOCKPLACE_SPEED_CHECK);
        speedInterval = config.getLong(ConfPaths.BLOCKPLACE_SPEED_INTERVAL);
        speedActions = config.getOptimizedActionList(ConfPaths.BLOCKPLACE_SPEED_ACTIONS, Permissions.BLOCKPLACE_SPEED);

        preventBoatsAnywhere = config.getBoolean(ConfPaths.BLOCKPLACE_PREVENTMISC_BOATSANYWHERE);
        /*
         * TODO: Placing boats has been possible since 1.4.5-R1.0. Behavior
         * differs, e.g. 1.12 only places boats when clicking the top of a
         * block, while in 1.7.10 the boat is placed on top of a block you click
         * the side of. If exceptions are to be implemented at all, they must
         * contain protection against abuse.
         */
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.ICheckConfig#isEnabled(fr.neatmonster.nocheatplus.checks.CheckType)
     */
    @Override
    public final boolean isEnabled(final CheckType checkType) {
        switch (checkType) {
            case BLOCKPLACE_DIRECTION:
                return directionCheck;
            case BLOCKPLACE_FASTPLACE:
                return fastPlaceCheck;
            case BLOCKPLACE_NOSWING:
                return noSwingCheck;
            case BLOCKPLACE_REACH:
                return reachCheck;
            case BLOCKPLACE_SPEED:
                return speedCheck;
            case BLOCKPLACE_AGAINST:
                return againstCheck;
            case BLOCKPLACE_AUTOSIGN:
                return autoSignCheck;
            default:
                return true;
        }
    }
}
