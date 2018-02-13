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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * Map (online) players in various ways. Fall back to Bukkit methods if not
 * mapped.
 * 
 * @author asofold
 *
 */
public final class PlayerMap {

    // TODO: Remove, store in PlayerData (WeakHandle rather), perhaps keep some of the fetching logic.

    /**
     * Carry basic information about players.
     * 
     * @author asofold
     *
     */
    public static final class PlayerInfo {

        public final UUID id;
        public final String exactName;
        public final String lowerCaseName;
        public Player player = null;

        public PlayerInfo (UUID id, String exactName) {
            this.id = id;
            this.exactName = exactName;
            this.lowerCaseName = exactName.toLowerCase();
        }

        public boolean matchesExact(UUID id, String exactName) {
            return this.id.equals(id) && this.exactName.equals(exactName);
        }

    }


    private final boolean storePlayerInstances;
    private final boolean hasGetPlayer_UUID = ReflectionUtil.getMethod(Bukkit.class, "getPlayer", UUID.class) != null;

    // TODO: Map types (copy on write, lazy erase, or just keep ordinary maps?)
    private Map<UUID, PlayerInfo> idInfoMap = new HashMap<UUID, PlayerMap.PlayerInfo>();
    private Map<String, PlayerInfo> exactNameInfoMap = new HashMap<String, PlayerMap.PlayerInfo>();
    private Map<String, PlayerInfo> lowerCaseNameInfoMap = new HashMap<String, PlayerMap.PlayerInfo>();
    // TODO: Consider: Get players by prefix (primary thread only, e.g. for use with commands).
    // TODO: get uuid/name methods?
    // TODO: unlink Player references on remove for better gc?


    public PlayerMap(boolean storePlayerInstances) {
        this.storePlayerInstances = storePlayerInstances;
        if (storePlayerInstances) {
            StaticLog.logInfo("Player instances are stored for efficiency.");
        }
    }

    // Public methods.

    public boolean storesPlayerInstances() {
        return storePlayerInstances;
    }

    public boolean hasPlayerInfo(final UUID id) {
        return idInfoMap.containsKey(id);
    }

    public boolean hasPlayerInfoExact(final String exactName) {
        return exactNameInfoMap.containsKey(exactName);
    }

    public boolean hasPlayerInfo(final String probableName) {
        return hasPlayerInfoLowerCase(probableName.toLowerCase());
    }

    public boolean hasPlayerInfoLowerCase(final String lowerCaseName) {
        return lowerCaseNameInfoMap.containsKey(lowerCaseName);
    }

    public PlayerInfo getPlayerInfo(final UUID id) {
        return idInfoMap.get(id);
    }

    public PlayerInfo getPlayerInfoExact(final String exactName) {
        return exactNameInfoMap.get(exactName);
    }

    public PlayerInfo getPlayerInfo(final String probableName) {
        return getPlayerInfoLowerCase(probableName.toLowerCase());
    }

    public PlayerInfo getPlayerInfoLowerCase(final String lowerCaseName) {
        return lowerCaseNameInfoMap.get(lowerCaseName);
    }

    public Player getPlayer(final UUID id) {
        final PlayerInfo info = idInfoMap.get(id);
        if (info != null) {
            if (info.player != null) {
                return info.player;
            }
            if (storePlayerInstances) {
                info.player = getPlayerBukkit(info);
                return info.player;
            } else  {
                return getPlayerBukkit(info);
            }
        } else {
            return getPlayerBukkit(id);
        }
    }

    @SuppressWarnings("deprecation")
    public Player getPlayerExact(final String exactName) {
        final PlayerInfo info = exactNameInfoMap.get(exactName);
        if (info != null) {
            if (info.player != null) {
                return info.player;
            }
            if (storePlayerInstances) {
                info.player = getPlayerBukkit(info);
                return info.player;
            } else  {
                return getPlayerBukkit(info);
            }
        } else {
            return Bukkit.getPlayerExact(exactName);
        }
    }

    public Player getPlayer(final String probableName) {
        return getPlayerLowerCase(probableName.toLowerCase());
    }

    @SuppressWarnings("deprecation")
    public Player getPlayerLowerCase(final String lowerCaseName) {
        final PlayerInfo info = lowerCaseNameInfoMap.get(lowerCaseName);
        if (info != null) {
            if (info.player != null) {
                return info.player;
            }
            if (storePlayerInstances) {
                info.player = getPlayerBukkit(info);
                return info.player;
            } else  {
                return getPlayerBukkit(info);
            }
        } else {
            return Bukkit.getPlayer(lowerCaseName);
        }
    }

    public PlayerInfo updatePlayer(final Player player) {
        final UUID id = player.getUniqueId();
        final String exactName = player.getName();
        PlayerInfo info = idInfoMap.get(id);
        if (info != null) {
            if (info.matchesExact(id, exactName)) {
                // Nothing to do, except updating the player instance.
                if (storePlayerInstances) {
                    info.player = player;
                }
                return info;
            } else {
                // Remove the info.
                remove(info);
            }
        }
        // Create and link a new info.
        info = new PlayerInfo(id, exactName);
        if (storePlayerInstances) {
            info.player = player;
        }
        ensureRemoved(info);
        idInfoMap.put(id, info);
        exactNameInfoMap.put(exactName, info);
        lowerCaseNameInfoMap.put(info.lowerCaseName, info);
        return info;
    }

    /**
     * Remove the instance reference, if present at all.
     * 
     * @param id
     */
    public void removePlayerInstance(final UUID id) {
        final PlayerInfo info = idInfoMap.get(id);
        if (info != null) {
            info.player = null;
        }
    }

    public boolean remove(final Player player) {
        return ensureRemoved(new PlayerInfo(player.getUniqueId(), player.getName()));
    }

    public void clear() {
        idInfoMap.clear();
        exactNameInfoMap.clear();
        lowerCaseNameInfoMap.clear();
    }

    public int size() {
        // TODO: Consistency?
        return idInfoMap.size();
    }

    // Private methods.

    private Player getPlayerBukkit(final UUID id) {
        if (hasGetPlayer_UUID) {
            return Bukkit.getPlayer(id);
        } else {
            // HACKS
            final IPlayerData pData = DataManager.getPlayerData(id);
            if (pData != null) {
                return getPlayer(pData.getPlayerName());
            }
            else {
                // Backwards compatibility.
                return scanForPlayer(id);                
            }
        }
    }

    private Player scanForPlayer(final UUID id) {
        // TODO: Add a mapping for id->name for this case?
        for (final Player player : BridgeMisc.getOnlinePlayers()) {
            if (id.equals(player.getUniqueId())) {
                return player;
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    private Player getPlayerBukkit(final PlayerInfo info) {
        if (hasGetPlayer_UUID) {
            return Bukkit.getPlayer(info.id);
        } else {
            return Bukkit.getPlayerExact(info.exactName);
        }
    }

    /**
     * Ensure there are no entries for the given info (for the case of
     * inconsistent combinations).
     * 
     * @param info
     * @return
     */
    private boolean ensureRemoved(final PlayerInfo info) {
        PlayerInfo ref = idInfoMap.get(info.id);
        boolean changed = false;
        if (ref != null) {
            remove(ref);
            changed = true;
        }
        ref = exactNameInfoMap.get(info.exactName);
        if (ref != null) {
            remove(ref);
            changed = true;
        }
        ref = lowerCaseNameInfoMap.get(info.lowerCaseName);
        if (ref != null) {
            remove(ref);
            changed = true;
        }
        return changed;
    }

    /**
     * Remove an existing info from all mappings, only call for consistent
     * states.
     * 
     * @param info
     * @return
     */
    private boolean remove(final PlayerInfo info) {
        boolean altered = false;
        altered |= idInfoMap.remove(info.id) != null;
        altered |= exactNameInfoMap.remove(info.exactName) != null;
        altered |= lowerCaseNameInfoMap.remove(info.lowerCaseName) != null;
        return altered;
    }

}
