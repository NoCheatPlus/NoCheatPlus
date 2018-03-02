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
package fr.neatmonster.nocheatplus.players;

import java.util.UUID;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.registry.ComponentRegistry;
import fr.neatmonster.nocheatplus.components.registry.factory.IRichFactoryRegistry;
import fr.neatmonster.nocheatplus.components.registry.feature.IRemoveData;

/**
 * Player data specific operations.
 * <hr/>
 * ComponentRegistry:
 * <li>Supported: IRemoveData</li>
 * <hr/>
 * <b>Preset groups and automatic registration.</b><br/>
 * All of the types mentioned in the lists below are preset for grouping. <br/>
 * However only those item types for which a factory gets registered here will
 * be automatically put into existing groups. External types like configuration
 * instances fetched from IWorldData need to be registered explicitly. <br/>
 * <br/>
 * Automatic data type grouping with call support (return value controls removal
 * of the entire data object):
 * <ul>
 * <li>{@link fr.neatmonster.nocheatplus.components.data.IDataOnReload}</li>
 * <li>{@link fr.neatmonster.nocheatplus.components.data.IDataOnWorldUnload}</li>
 * <li>{@link fr.neatmonster.nocheatplus.components.data.IDataOnJoin}</li>
 * <li>{@link fr.neatmonster.nocheatplus.components.data.IDataOnLeave}</li>
 * <li>{@link fr.neatmonster.nocheatplus.components.data.IDataOnWorldChange}</li>
 * <li>{@link fr.neatmonster.nocheatplus.components.data.IDataOnRemoveSubCheckData}</li></li>
 * </ul>
 * <br/>
 * Automatic data type grouping for direct removal (API/reload/commands):
 * <ul>
 * <li>{@link fr.neatmonster.nocheatplus.components.data.IData}</li>
 * <li>{@link fr.neatmonster.nocheatplus.components.config.IConfig}</li>
 * <li>{@link fr.neatmonster.nocheatplus.components.data.ICheckData}</li>
 * <li>{@link fr.neatmonster.nocheatplus.components.config.ICheckConfig}</li>
 * </ul>
 * 
 * @author asofold
 *
 */
public interface IPlayerDataManager extends ComponentRegistry<IRemoveData>, IRichFactoryRegistry<PlayerFactoryArgument> {

    // TODO: Complete (...)

    /**
     * Fetch a PlayerData instance. If none is present and create is set, a new
     * instance will be created.
     * 
     * @param player
     * @param create
     * @return
     */
    public IPlayerData getPlayerData(final Player player, boolean create);

    /**
     * Get a PlayerData instance in any case - always creates a PlayerData
     * instance, if none is present. This method should be preferred, as it
     * hides details.
     * 
     * @param player
     * @return
     */
    public IPlayerData getPlayerData(final Player player);

    /**
     * Get the player data, if present.
     * 
     * @param playerId
     * @return The PlayerData instance if present, null otherwise.
     */
    public IPlayerData getPlayerData(final UUID playerId);

    /**
     * Get the player data, if present.
     * 
     * @param playerName
     * @return The PlayerData instance if present, null otherwise.
     */
    public IPlayerData getPlayerData(final String playerName);

    /**
     * This gets an online player by exact player name or lower-case player name
     * only [subject to change].
     * 
     * @param playerName
     * @return
     */
    public Player getPlayerExact(final String playerName);

    /**
     * Retrieve the UUID for a given input (name or UUID string of with or
     * without '-'). Might later also query a cache, if appropriate. Convenience
     * method for use with commands.
     * 
     * @param input
     * @return
     */
    public UUID getUUID(final String input);

    /**
     * Get the exact player name, stored internally.
     * 
     * @param playerId
     */
    public String getPlayerName(final UUID playerId);

    /**
     * Get an online player by UUID.
     * 
     * @param id
     * @return
     */
    public Player getPlayer(final UUID id);

    /**
     * This gets the online player with the exact name, but transforms the input
     * to lower case.
     * 
     * @param playerName
     * @return
     */
    public Player getPlayer(final String playerName);

    /**
     * Restore the default debug flags within player data, as given in
     * corresponding configurations. This only yields the correct result, if the
     * the data uses the same configuration for initialization which is
     * registered under the same check type.
     */
    public void restoreDefaultDebugFlags();

    /**
     * Remove an instance from the PlayerData-local generic instance storage,
     * for all stored PlayerData instances. Factories and proxy-registries are
     * not touched.
     * 
     * @param registeredFor
     */
    public <T> void removeGenericInstance(Class<T> registeredFor);

    public void clearAllExemptions();

    /**
     * Remove data and history of all players for the given check type and sub
     * checks.
     * 
     * @param checkType
     */
    public void clearData(CheckType checkType);

    /**
     * Convenience method to remove cached types that implement IConfig for all
     * players. Types need to be registered (player data factory or explicitly).
     */
    public void removeCachedConfigs();

}
