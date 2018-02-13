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

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.worlds.IWorldData;

/**
 * Configurations specific for the block interact checks. Every world gets one of these assigned to it, or if a world
 * doesn't get it's own, it will use the "global" version.
 */
public class BlockInteractConfig extends ACheckConfig {

    public final ActionList directionActions;

    public final ActionList reachActions;

    public final long		speedInterval;
    public final int		speedLimit;
    public final ActionList speedActions;

    public final ActionList visibleActions;

    /**
     * Instantiates a new block interact configuration.
     * 
     * @param data
     *            the data
     */
    public BlockInteractConfig(final IWorldData worldData) {
        super(worldData);
        final ConfigFile data = worldData.getRawConfiguration();
        directionActions = data.getOptimizedActionList(ConfPaths.BLOCKINTERACT_DIRECTION_ACTIONS,
                Permissions.BLOCKINTERACT_DIRECTION);

        reachActions = data.getOptimizedActionList(ConfPaths.BLOCKINTERACT_REACH_ACTIONS, Permissions.BLOCKINTERACT_REACH);

        speedInterval = data.getLong(ConfPaths.BLOCKINTERACT_SPEED_INTERVAL);
        speedLimit = data.getInt(ConfPaths.BLOCKINTERACT_SPEED_LIMIT);
        speedActions = data.getOptimizedActionList(ConfPaths.BLOCKINTERACT_SPEED_ACTIONS, Permissions.BLOCKINTERACT_SPEED);

        visibleActions = data.getOptimizedActionList(ConfPaths.BLOCKINTERACT_VISIBLE_ACTIONS, Permissions.BLOCKINTERACT_VISIBLE);
    }

}
