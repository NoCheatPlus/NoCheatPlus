package fr.neatmonster.nocheatplus.net.protocollib;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.ds.corw.LinkedHashMapCOW;

/**
 * Prevent extremely fast ticking by just sending packets that don't do anything
 * new and also don't trigger moving events in CraftBukkit.
 * 
 * @author dev1mc
 *
 */
public class FlyingFrequency extends PacketAdapter implements JoinLeaveListener {

    // TODO: Silent cancel count.
    // TODO: Configuration.
    // TODO: Optimized options (receive only, other?).
    // TODO: Forced async version ?

    private final Map<String, ActionFrequency> freqMap = new LinkedHashMapCOW<String, ActionFrequency>();  
    private final Counters counters = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class);
    private final int idSilent = counters.registerKey("packet.flying.silentcancel");
    private final int idNullPlayer = counters.registerKey("packet.flying.nullplayer");

    private final int seconds;
    private final float maxPackets;

    public FlyingFrequency(Plugin plugin) {
        // PacketPlayInFlying[3, legacy: 10]
        super(plugin, PacketType.Play.Client.FLYING); // TODO: How does POS and POS_LOOK relate/translate?
        ConfigFile config = ConfigManager.getConfigFile();
        seconds = Math.max(1, config.getInt(ConfPaths.NET_FLYINGFREQUENCY_SECONDS));
        maxPackets = Math.max(1, config.getInt(ConfPaths.NET_FLYINGFREQUENCY_MAXPACKETS));
    }

    @Override
    public void playerJoins(Player player) {
        getFreq(player.getName());
    }

    @Override
    public void playerLeaves(Player player) {
        freqMap.remove(player.getName());
    }

    private ActionFrequency getFreq(final String name) {
        final ActionFrequency freq = this.freqMap.get(name);
        if (freq != null) {
            return freq;
        } else {
            final ActionFrequency newFreq = new ActionFrequency(seconds, 1000);
            this.freqMap.put(name, newFreq);
            return newFreq;
        }
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {
        final Player player = event.getPlayer();
        if (player == null) {
            if (onNullPlayer(event)) {
                event.setCancelled(true);
            }
            return;
        }
        // TODO: Consider using similar heuristic as CB does for when to count.
        // TODO: Consider detecting "untracked moves" early.
        // TODO: Add more counters/cases (at least has look + has pos individually, maybe none/onground)
        final ActionFrequency freq = getFreq(player.getName());
        freq.add(System.currentTimeMillis(), 1f);
        if (freq.score(1f) > maxPackets) {
            if (onViolation(player)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean onViolation(final Player player) {
        // TODO: Get from a NetConfig (optimized).
        if (ConfigManager.getConfigFile(player.getWorld().getName()).getBoolean(ConfPaths.NET_FLYINGFREQUENCY_ACTIVE)) {
            counters.add(idSilent, 1); // Until it is sure if we can get these async.
            return true;
        }
        else {
            return false;
        }
    }

    private boolean onNullPlayer(final PacketEvent event) {
        // TODO: Config (global?) ?
        counters.add(idNullPlayer, 1);
        return true;
    }

}
