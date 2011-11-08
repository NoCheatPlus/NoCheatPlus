package cc.co.evenprime.bukkit.nocheat.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.player.NoCheatPlayerImpl;

/**
 * Provide secure access to player-specific data objects for various checks or
 * check groups.
 */
public class PlayerManager {

    // Store data between Events
    private final Map<String, NoCheatPlayer> players;
    private final NoCheat                    plugin;
    private final List<String>               removals;

    public PlayerManager(NoCheat plugin) {
        this.players = new HashMap<String, NoCheatPlayer>();
        this.removals = new ArrayList<String>(5);
        this.plugin = plugin;
    }

    /**
     * Get a data object of the specified class. If none is stored yet, create
     * one.
     */
    public NoCheatPlayer getPlayer(String playerName) {

        NoCheatPlayer p = this.players.get(playerName);

        if(p == null) {
            // TODO: Differentiate which player"type" should be created, e.g. based on bukkit version
            p = new NoCheatPlayerImpl(playerName, plugin, new BaseData());
            this.players.put(playerName, p);
        }

        return p;
    }

    /**
     * Reset data that may cause problems after e.g. changing the config
     * 
     */
    public void clearCriticalData() {
        for(NoCheatPlayer b : this.players.values()) {
            b.getData().clearCriticalData();
        }
    }

    /**
     * check if some data hasn't been used for a while and remove it
     * 
     */
    public void cleanDataMap() {
        synchronized(removals) {
            long time = System.currentTimeMillis();
            try {
                for(Entry<String, NoCheatPlayer> p : this.players.entrySet()) {
                    if(p.getValue().getData().shouldBeRemoved(time)) {
                        removals.add(p.getKey());
                    }
                }

                for(String p : removals) {
                    this.players.remove(p);
                }

                removals.clear();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clearCriticalData(String playerName) {
        NoCheatPlayer p = this.players.get(playerName);
        if(p != null) {
            p.getData().clearCriticalData();
        }
    }

}
