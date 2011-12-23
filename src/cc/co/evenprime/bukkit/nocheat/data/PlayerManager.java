package cc.co.evenprime.bukkit.nocheat.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.player.NoCheatPlayerImpl;

/**
 * Provide secure access to player-specific data objects for various checks or
 * check groups.
 */
public class PlayerManager {

    // Store data between Events
    private final Map<String, NoCheatPlayerImpl> players;
    private final NoCheat                        plugin;

    public PlayerManager(NoCheat plugin) {
        this.players = new HashMap<String, NoCheatPlayerImpl>();
        this.plugin = plugin;
    }

    /**
     * Get a data object of the specified class. If none is stored yet, create
     * one.
     */
    public NoCheatPlayer getPlayer(Player player) {

        NoCheatPlayerImpl p = this.players.get(player.getName().toLowerCase());

        if(p == null) {
            p = new NoCheatPlayerImpl(player, plugin);
            this.players.put(player.getName().toLowerCase(), p);
        }

        p.setLastUsedTime(System.currentTimeMillis());
        p.refresh(player);

        return p;
    }

    /**
     * Reset data that may cause problems after e.g. changing the config
     * 
     */
    public void clearCriticalData() {
        for(NoCheatPlayer b : this.players.values()) {
            b.getDataStore().clearCriticalData();
        }
    }

    public void clearCriticalData(String playerName) {
        NoCheatPlayer p = this.players.get(playerName.toLowerCase());
        if(p != null) {
            p.getDataStore().clearCriticalData();
        }
    }

    public void cleanDataMap() {
        long time = System.currentTimeMillis();
        List<String> removals = new ArrayList<String>(5);

        for(Entry<String, NoCheatPlayerImpl> e : this.players.entrySet()) {
            if(e.getValue().shouldBeRemoved(time)) {
                removals.add(e.getKey());
            }
        }

        for(String key : removals) {
            this.players.remove(key);
        }
    }

    public void getPlayerData(String playerName, Map<String, Object> map) {

        NoCheatPlayer player = this.players.get(playerName.toLowerCase());

        if(player != null) {
            player.getDataStore().collectData(map);

        }
    }

}
