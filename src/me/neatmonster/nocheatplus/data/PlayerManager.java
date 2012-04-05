package me.neatmonster.nocheatplus.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.NoCheatPlusPlayerImpl;

import org.bukkit.entity.Player;

/**
 * Provide secure access to player-specific data objects for various checks or
 * check groups.
 */
public class PlayerManager {

    // Store data between Events
    private final Map<String, NoCheatPlusPlayerImpl> players;
    private final NoCheatPlus                        plugin;

    public PlayerManager(final NoCheatPlus plugin) {
        players = new HashMap<String, NoCheatPlusPlayerImpl>();
        this.plugin = plugin;
    }

    public void cleanDataMap() {
        final long time = System.currentTimeMillis();
        final List<String> removals = new ArrayList<String>(5);

        for (final Entry<String, NoCheatPlusPlayerImpl> e : players.entrySet())
            if (e.getValue().shouldBeRemoved(time))
                removals.add(e.getKey());

        for (final String key : removals)
            players.remove(key);
    }

    /**
     * Get a data object of the specified class. If none is stored yet, create
     * one.
     */
    public NoCheatPlusPlayer getPlayer(final Player player) {

        NoCheatPlusPlayerImpl p = players.get(player.getName().toLowerCase());

        if (p == null) {
            p = new NoCheatPlusPlayerImpl(player, plugin);
            players.put(player.getName().toLowerCase(), p);
        }

        p.setLastUsedTime(System.currentTimeMillis());
        p.refresh(player);

        return p;
    }

    public Map<String, Object> getPlayerData(final String playerName) {

        final NoCheatPlusPlayer player = players.get(playerName.toLowerCase());

        if (player != null)
            return player.getDataStore().collectData();

        return new HashMap<String, Object>();
    }

}
