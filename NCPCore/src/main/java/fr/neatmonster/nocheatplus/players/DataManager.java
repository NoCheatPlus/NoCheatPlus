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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.compat.versions.BukkitVersion;
import fr.neatmonster.nocheatplus.compat.versions.GenericVersion;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.components.data.ICanHandleTimeRunningBackwards;
import fr.neatmonster.nocheatplus.components.registry.ComponentRegistry;
import fr.neatmonster.nocheatplus.components.registry.feature.ComponentWithName;
import fr.neatmonster.nocheatplus.components.registry.feature.ConsistencyChecker;
import fr.neatmonster.nocheatplus.components.registry.feature.IDisableListener;
import fr.neatmonster.nocheatplus.components.registry.feature.IHaveCheckType;
import fr.neatmonster.nocheatplus.components.registry.feature.INeedConfig;
import fr.neatmonster.nocheatplus.components.registry.feature.IRemoveData;
import fr.neatmonster.nocheatplus.components.registry.order.SetupOrder;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.hooks.APIUtils;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.IdUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Central access point for a lot of functionality for managing data, especially
 * removing data for cleanup.<br>
 * Originally intended as temporary or intermediate design, this might help
 * reorganizing the API at some point.<br>
 * However i could not yet find a pleasing way for generic configuration access
 * for a centralized data management (all in one), so this might just be a
 * workarounds class for coping with the current design, until somehow resolved
 * in another way.
 * <hr>
 * ComponentRegistry:
 * <li>Supported: IRemoveData</li>
 * 
 * @author mc_dev
 *
 */
@SetupOrder(priority = -80)
public class DataManager implements Listener, INeedConfig, ComponentRegistry<IRemoveData>, ComponentWithName, ConsistencyChecker, IDisableListener{

    private static DataManager instance = null;

    // Not static
    private int foundInconsistencies = 0;

    // TODO: Switch to UUIDs as keys, get data by uuid when possible, use PlayerMap for getting
    /** PlayerData storage. */
    private final Map<String, PlayerData> playerData = new LinkedHashMap<String, PlayerData>(100);

    /**
     * Access order for playerName (exact) -> ms time of logout.
     * <hr>
     * Later this might hold central player data objects instead of the long
     * only.
     */
    private final Map<String, Long> lastLogout = new LinkedHashMap<String, Long>(50, 0.75f, true);

    /**
     * Keeping track of online players. Currently id/name mappings are not kept
     * on logout, but might be later.
     */
    // TODO: Switch to UUIDs as keys, get data by uuid when possible, use PlayerMap for getting the UUID.
    private final PlayerMap playerMap;

    /**
     * IRemoveData instances.
     */
    // TODO: might use a map for those later (extra or not).
    private final ArrayList<IRemoveData> iRemoveData = new ArrayList<IRemoveData>();

    /**
     * Execution histories of the checks.
     */
    private final Map<CheckType, Map<String, ExecutionHistory>> executionHistories = new HashMap<CheckType, Map<String,ExecutionHistory>>();

    /** Flag if data expiration is active at all. */
    private boolean doExpireData = false;

    /**
     * Duration in milliseconds for expiration of logged off players data. In
     * the config minutes are used as unit.
     */
    private long durExpireData = 0;

    /** Data and execution history. */
    private boolean deleteData = true;
    /** Violation history and execution history. */
    private boolean deleteHistory = false;

    /**
     * Sets the static instance reference.
     */
    public DataManager() {
        instance = this;
        if (ServerVersion.isMinecraftVersionUnknown()) {
            // True hacks.
            BukkitVersion.init();
        }
        final String version = ServerVersion.getMinecraftVersion();
        if (GenericVersion.compareVersions(version, "1.8") >= 0 || version.equals("1.7.10") && Bukkit.getServer().getVersion().toLowerCase().indexOf("spigot") != -1) {
            // Safe to assume Spigot, don't store Player instances.
            playerMap = new PlayerMap(false);
        } else {
            // Likely an older version without efficient mapping.
            playerMap = new PlayerMap(true);
        }
    }

    /**
     * Check the logged out players for if any data can be removed.<br>
     * Currently only "dumb" full removal is performed. Later it is thinkable to
     * remove "as much as reasonable".
     */
    public void checkExpiration() {
        if (!doExpireData || durExpireData <= 0) {
            return;
        }
        final long now = System.currentTimeMillis();
        final Set<CheckDataFactory> factories = new LinkedHashSet<CheckDataFactory>();
        final Set<Entry<String, Long>> entries = lastLogout.entrySet();
        final Iterator<Entry<String, Long>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            final Entry<String, Long> entry = iterator.next();
            final long ts = entry.getValue();
            if (now - ts <= durExpireData) {
                break;
            }
            final String playerName = entry.getKey();
            if (deleteData) {
                factories.clear();
                for (final CheckType type : CheckType.values()) {
                    final CheckDataFactory factory = type.getDataFactory();
                    if (factory != null) {
                        factories.add(factory);
                    }
                }
                for (final CheckDataFactory factory : factories) {
                    factory.removeData(playerName);
                }
                clearComponentData(CheckType.ALL, playerName);
                playerData.remove(playerName.toLowerCase()); // TODO
            }
            if (deleteData || deleteHistory) {
                removeExecutionHistory(CheckType.ALL, playerName);
            }
            if (deleteHistory) {
                ViolationHistory.removeHistory(playerName);
            }
            iterator.remove();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        lastLogout.remove(player.getName());
        CombinedData.getData(player).lastJoinTime = System.currentTimeMillis(); 
        addOnlinePlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        onPlayerLeave(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(final PlayerKickEvent event) {
        onPlayerLeave(event.getPlayer());
    }

    /**
     * Quit or kick.
     * @param player
     */
    private final void onPlayerLeave(final Player player) {
        final long now = System.currentTimeMillis();
        lastLogout.put(player.getName(), now);
        CombinedData.getData(player).lastLogoutTime = now;
        removeOnlinePlayer(player);
    }

    @Override
    public void onReload() {
        // present.
        adjustSettings();
    }

    /**
     * Fetch settings from the current default config.
     */
    private void adjustSettings() {
        final ConfigFile config = ConfigManager.getConfigFile();
        doExpireData = config.getBoolean(ConfPaths.DATA_EXPIRATION_ACTIVE);
        durExpireData = config.getLong(ConfPaths.DATA_EXPIRATION_DURATION, 1, 1000000, 60) * 60000L; // in minutes
        deleteData = config.getBoolean(ConfPaths.DATA_EXPIRATION_DATA, true); // hidden.
        deleteHistory = config.getBoolean(ConfPaths.DATA_EXPIRATION_HISTORY);
    }

    /**
     * Used by checks to register the history for external access.<br>
     * NOTE: This method is not really meant ot be used from outside NCP.
     * 
     * @param type
     * @param histories
     */
    public static void registerExecutionHistory(CheckType type, Map<String, ExecutionHistory> histories) {
        instance.executionHistories.put(type, histories);
    }

    /**
     * Access method to the the execution history for check type for a player.
     * 
     * @param type
     * @param playerName
     *            Exact case for player name.
     * @return null if not present.
     */
    public static ExecutionHistory getExecutionHistory(final CheckType type, final String playerName) {
        final Map<String, ExecutionHistory> map = instance.executionHistories.get(type);
        if (map != null) {
            return map.get(playerName);
        }
        return null;
    }

    /**
     * Remove the execution history for a player for the given check type.
     * 
     * @param type
     * @param playerName
     * @return
     */
    public static boolean removeExecutionHistory(final CheckType type, final String playerName) {
        boolean removed = false;
        // TODO: design ...
        for (final CheckType refType : APIUtils.getWithChildren(type)) {
            final Map<String, ExecutionHistory> map = instance.executionHistories.get(refType);
            if (map != null && map.remove(playerName) != null) {
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Removes all data and history for a player.
     * 
     * @deprecated Use clearData instead, this likely to be removed later.
     * @param checkType
     */
    public static void clear(final CheckType checkType) {
        clearData(checkType);
    }

    /**
     * Remove data and history of all players for the given check type and sub
     * checks.
     * 
     * @param checkType
     */
    public static void clearData(final CheckType checkType) {
        final Set<CheckDataFactory> factories = new HashSet<CheckDataFactory>();
        for (final CheckType type : APIUtils.getWithChildren(checkType)) {
            final Map<String, ExecutionHistory> map = instance.executionHistories.get(type);
            if (map != null) {
                map.clear();
            }
            final CheckDataFactory factory = type.getDataFactory();
            if (factory != null) {
                factories.add(factory);
            }
        }
        for (final CheckDataFactory factory : factories) {
            factory.removeAllData();
        }
        for (final IRemoveData rmd : instance.iRemoveData) {
            if (checkType == CheckType.ALL) {
                // Not sure this is really good, though.
                rmd.removeAllData();
            }
            else if (rmd instanceof IHaveCheckType) {
                final CheckType refType = ((IHaveCheckType) rmd).getCheckType();
                if (refType == checkType || APIUtils.isParent(checkType, refType)) {
                    rmd.removeAllData();
                }
            }
        }
        ViolationHistory.clear(checkType);
        if (checkType == CheckType.ALL) {
            instance.playerData.clear(); // TODO
        }
    }

    /**
     * Adjust to the system time having run backwards. This is much like
     * clearData(CheckType.ALL), with the exception of calling
     * ICanHandleTimeRunningBackwards.handleTimeRanBackwards for data instances
     * which implement this.
     */
    public static void handleSystemTimeRanBackwards() {
        // Collect data factories and clear execution history.
        final Set<CheckDataFactory> factories = new HashSet<CheckDataFactory>();
        for (final CheckType type : APIUtils.getWithChildren(CheckType.ALL)) {
            final Map<String, ExecutionHistory> map = instance.executionHistories.get(type);
            if (map != null) {
                map.clear();
            }
            final CheckDataFactory factory = type.getDataFactory();
            if (factory != null) {
                factories.add(factory);
            }
        }
        for (final CheckDataFactory factory : factories) {
            if (factory instanceof ICanHandleTimeRunningBackwards) {
                ((ICanHandleTimeRunningBackwards) factory).handleTimeRanBackwards();
            } else {
                factory.removeAllData();
            }
        }
        for (final IRemoveData rmd : instance.iRemoveData) {
            if (rmd instanceof ICanHandleTimeRunningBackwards) {
                ((ICanHandleTimeRunningBackwards) rmd).handleTimeRanBackwards();
            } else {
                rmd.removeAllData();
            }
        }
        ViolationHistory.clear(CheckType.ALL);
        // (Not removing PlayerData instances.)
    }

    /**
     * Restore the default debug flags within player data, as given in
     * corresponding configurations. This only yields the correct result, if the
     * the data uses the same configuration for initialization which is
     * registered under the same check type.
     */
    public static void restoreDefaultDebugFlags() {
        final Player[] players = BridgeMisc.getOnlinePlayers();
        for (final CheckType checkType : CheckType.values()) {
            final CheckConfigFactory configFactory = checkType.getConfigFactory();
            if (configFactory == null) {
                continue;
            }
            final CheckDataFactory dataFactory = checkType.getDataFactory();
            if (dataFactory == null) {
                continue;
            }
            for (int i = 0; i < players.length; i++) {
                final Player player = players[i];
                final ICheckConfig config = configFactory.getConfig(player);
                if (config == null) {
                    continue;
                }
                final ICheckData data = dataFactory.getData(player);
                if (data == null) {
                    continue;
                }
                if (config.getDebug() != data.getDebug()) {
                    data.setDebug(config.getDebug());
                }
            }
        }
    }

    /**
     * Remove the player data for a given player and a given check type.
     * CheckType.ALL and null will be interpreted as removing all data.<br>
     * 
     * @param playerName
     *            Exact player name.
     * @param checkType
     *            Check type to remove data for, null is regarded as ALL.
     * @return If any data was present.
     */
    public static boolean removeData(final String playerName, CheckType checkType) {
        if (checkType == null) {
            checkType = CheckType.ALL;
        }
        boolean had = false;

        // Check extended registered components.
        if (clearComponentData(checkType, playerName)) {
            had = true;
        }

        // Collect factories.
        final Set<CheckDataFactory> factories = new HashSet<CheckDataFactory>();
        for (CheckType otherType : APIUtils.getWithChildren(checkType)) {
            final CheckDataFactory otherFactory = otherType.getDataFactory();
            if (otherFactory != null) {
                factories.add(otherFactory);
            }
        }
        // Remove data.
        for (final CheckDataFactory otherFactory : factories) {
            if (otherFactory.removeData(playerName) != null) {
                had = true;
            }
        }

        if (checkType == CheckType.ALL) {
            instance.playerData.remove(playerName.toLowerCase());
        }

        return had;
    }

    /**
     * Clear player related data, only for registered components (not execution
     * history, violation history, normal check data).<br>
     * That should at least go for chat engine data.
     * 
     * @param CheckType
     * @param PlayerName
     * @return If something was removed.
     */
    public static boolean clearComponentData(final CheckType checkType, final String PlayerName) {
        boolean removed = false;
        for (final IRemoveData rmd : instance.iRemoveData) {
            if (checkType == CheckType.ALL) {
                // Not sure this is really good, though.
                if (rmd.removeData(PlayerName) != null) {
                    removed = true;
                }
            }
            else if (rmd instanceof IHaveCheckType) {
                final CheckType refType = ((IHaveCheckType) rmd).getCheckType();
                if (refType == checkType || APIUtils.isParent(checkType, refType)) {
                    if (rmd.removeData(PlayerName) != null) {
                        removed = true;
                    }
                }
            }
        }
        return removed;
    }

    /**
     * Clear all cached CheckConfig instances.<br>
     * This does not cleanup ConfigManager, i.e. stored yml-versions.
     */
    public static void clearConfigs() {
        final Set<CheckConfigFactory> factories = new LinkedHashSet<CheckConfigFactory>();
        for (final CheckType checkType : CheckType.values()) {
            final CheckConfigFactory factory = checkType.getConfigFactory();
            if (factory != null) {
                factories.add(factory);
            }
        }
        for (final CheckConfigFactory factory : factories) {
            factory.removeAllConfigs();
        }
    }

    /**
     * This gets an online player by exact player name or lower-case player name
     * only [subject to change].
     * 
     * @param playerName
     * @return
     */
    public static Player getPlayerExact(final String playerName) {
        return instance.playerMap.getPlayerExact(playerName);
    }

    /**
     * Retrieve the UUID for a given input (name or UUID string of with or
     * without '-'). Might later also query a cache, if appropriate. Convenience
     * method for use with commands.
     * 
     * @param input
     * @return
     */
    public static UUID getUUID(final String input) {
        // TODO: Use player map.
        final Player player = getPlayer(input);
        if (player != null) {
            return player.getUniqueId();
        }
        return IdUtil.UUIDFromStringSafe(input);
    }

    /**
     * Get an online player by UUID.
     * 
     * @param id
     * @return
     */
    public static Player getPlayer(final UUID id) {
        return instance.playerMap.getPlayer(id);
    }

    /**
     * This gets the online player with the exact name, but transforms the input
     * to lower case.
     * 
     * @param playerName
     * @return
     */
    public static Player getPlayer(final String playerName) {
        return instance.playerMap.getPlayer(playerName);
    }

    @Override
    public boolean addComponent(IRemoveData obj) {
        if (iRemoveData.contains(obj)) {
            return false;
        }
        else {
            iRemoveData.add((IRemoveData) obj);
            return true;
        }
    }

    @Override
    public void removeComponent(IRemoveData obj) {
        iRemoveData.remove((IRemoveData) obj);
    }

    /**
     * Initializing with online players.
     */
    public void onEnable() {
        for (final Player player : BridgeMisc.getOnlinePlayers()) {
            addOnlinePlayer(player);
        }
    }

    /**
     * Add mappings for player names variations.
     * @param player
     */
    private void addOnlinePlayer(final Player player) {
        playerMap.updatePlayer(player);
    }

    /**
     * Remove mappings for player names variations.
     * @param player
     */
    private void removeOnlinePlayer(final Player player) {
        // TODO: Consider to only remove the Player instance?
        playerMap.remove(player);
    }

    /**
     * Cleanup method, removes all data and config, but does not call
     * ConfigManager.cleanup.
     */
    @Override
    public void onDisable() {
        clearData(CheckType.ALL);
        iRemoveData.clear();
        clearConfigs();
        lastLogout.clear();
        executionHistories.clear();
        playerMap.clear();
        // Finally alert (summary) if inconsistencies found.
        if (foundInconsistencies > 0) {
            StaticLog.logWarning("DataMan found " + foundInconsistencies + " inconsistencies (warnings suppressed).");
            foundInconsistencies = 0;
        }
    }

    @Override
    public String getComponentName() {
        return "NoCheatPlus_DataManager";
    }

    @Override
    public void checkConsistency(final Player[] onlinePlayers) {
        // Check online player tracking consistency.
        int missing = 0;
        int changed = 0;
        for (int i = 0; i < onlinePlayers.length; i++) {
            final Player player = onlinePlayers[i];
            final UUID id = player.getUniqueId();
            //			if (player.isOnline()) {
            // TODO: Add a consistency check method !?
            if (!playerMap.hasPlayerInfo(id)) {
                missing ++;
                // TODO: Add the player [problem: messy NPC plugins?]?
            }
            if (playerMap.storesPlayerInstances() && player != playerMap.getPlayer(id)) {
                changed ++;
                // Update the reference.
                addOnlinePlayer(player);
                //				}
            }
        }

        // TODO: Consider checking lastLogout for too long gone players.

        // TODO: Later the map size will not work, if we keep name/id mappings after logout. Other checking methods are possible.
        final int storedSize = this.playerMap.size();
        if (missing != 0 || changed != 0 || onlinePlayers.length != storedSize) {
            foundInconsistencies ++;
            if (!ConfigManager.getConfigFile().getBoolean(ConfPaths.DATA_CONSISTENCYCHECKS_SUPPRESSWARNINGS)) {
                final List<String> details = new LinkedList<String>();
                if (missing != 0) {
                    details.add("missing online players (" + missing + ")");
                }
                if (onlinePlayers.length != storedSize) {
                    // TODO: Consider checking for not online players and remove them.
                    details.add("wrong number of online players (" + storedSize + " instead of " + onlinePlayers.length + ")");
                }
                if (changed != 0) {
                    details.add("changed player instances (" + changed + ")");
                }

                StaticLog.logWarning("DataMan inconsistencies: " + StringUtil.join(details, " | "));
            }
        }
    }

    /**
     * Convenience method, also hiding how player data is stored for a Player
     * instance - always creates a PlayerData instance, if not already present.
     * 
     * @param player
     * @return
     */
    public static PlayerData getPlayerData(final Player player) {
        return getPlayerData(player.getName(), true);
    }

    /**
     * 
     * @param playerName
     * @param create
     * @return
     */
    public static PlayerData getPlayerData(final String playerName, final boolean create) {
        final String lcName = playerName.toLowerCase(); // TODO: Store by both lower case and exact case (also store the Player reference).
        final PlayerData data = instance.playerData.get(lcName);
        if (data != null) {
            return data;
        }
        else if (!create) {
            return null;
        }
        else {
            final PlayerData newData = new PlayerData(lcName);
            instance.playerData.put(lcName, newData);
            return newData;
        }
    }

    /**
     * Check if player instances are stored for efficiency (legacy).
     * 
     * @return
     */
    public boolean storesPlayerInstances() {
        return playerMap.storesPlayerInstances();
    }

}
