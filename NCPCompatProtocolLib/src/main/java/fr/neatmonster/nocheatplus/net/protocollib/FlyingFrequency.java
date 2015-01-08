package fr.neatmonster.nocheatplus.net.protocollib;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.net.NetConfig;
import fr.neatmonster.nocheatplus.net.NetConfigCache;
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

    // TODO: Most efficient registration + optimize (primary thread or asynchronous).

    private class FFData {
        public static final int numBooleans = 3;
        public static final int indexOnGround = 0;
        public static final int indexhasPos = 1;
        public static final int indexhasLook = 2;

        public final ActionFrequency all;
        // Last move.
        public final double[] doubles = new double[3]; // x, y, z
        public final float[] floats = new float[2]; // yaw, pitch
        //public final boolean[] booleans = new boolean[3]; // ground, hasPos, hasLook
        public boolean onGround = false;
        public FFData(int seconds) {
            all = new ActionFrequency(seconds, 1000L);
        }
    }

    private final Map<String, FFData> freqMap = new LinkedHashMapCOW<String, FFData>();  
    private final Counters counters = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class);
    private final int idSilent = counters.registerKey("packet.flying.silentcancel");
    private final int idRedundant = counters.registerKey("packet.flying.silentcancel.redundant");
    private final int idNullPlayer = counters.registerKey("packet.flying.nullplayer");

    private boolean cancelRedundant = true;

    private final NetConfigCache configs;

    public FlyingFrequency(NetConfigCache configs, Plugin plugin) {
        // PacketPlayInFlying[3, legacy: 10]
        super(plugin, PacketType.Play.Client.FLYING); // TODO: How does POS and POS_LOOK relate/translate?
        this.configs = configs;
    }

    @Override
    public void playerJoins(Player player) {
        // Ignore.
    }

    @Override
    public void playerLeaves(Player player) {
        freqMap.remove(player.getName());
    }

    private FFData getFreq(final String name, final NetConfig cc) {
        final FFData freq = this.freqMap.get(name);
        if (freq != null) {
            return freq;
        } else {
            final FFData newFreq = new FFData(cc.flyingFrequencySeconds);
            this.freqMap.put(name, newFreq);
            return newFreq;
        }
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {

        final Player player = event.getPlayer();
        if (player == null) {
            // TODO: Need config?
            counters.add(idNullPlayer, 1);
            event.setCancelled(true);
            return;
        }

        final NetConfig cc = configs.getConfig(player.getWorld());
        if (!cc.flyingFrequencyActive) {
            return;
        }

        final FFData freq = getFreq(player.getName(), cc);
        final long t = System.currentTimeMillis();
        // Counting all packets.
        freq.all.add(t, 1f);
        final float allScore = freq.all.score(1f);
        if (allScore > cc.flyingFrequencyMaxPackets) {
            counters.add(idSilent, 1); // Until it is sure if we can get these async.
            event.setCancelled(true);
            return;
        }

        // Cancel redundant packets, when frequency is high anyway.
        if (!cancelRedundant || !cc.flyingFrequencyCancelRedundant) {
            return;
        }
        // TODO: Consider to detect if a moving event would fire (...).
        boolean redundant = true;
        final PacketContainer packet = event.getPacket();
        final List<Boolean> booleans = packet.getBooleans().getValues();
        if (booleans.size() != FFData.numBooleans) {
            cancelRedundant = false;
            return;
        }
        final boolean onGround = booleans.get(FFData.indexOnGround).booleanValue(); 
        if (onGround != freq.onGround) {
            redundant = false;
        }
        freq.onGround = onGround;
        // TODO: Consider to verify on ground somehow.
        if (booleans.get(FFData.indexhasPos)) {
            final List<Double> doubles = packet.getDoubles().getValues();
            if (doubles.size() != freq.doubles.length) {
                cancelRedundant = false;
                return;
            }
            for (int i = 0; i < freq.doubles.length; i++) {
                final double val = doubles.get(i).doubleValue();
                if (val != freq.doubles[i]) {
                    redundant = false;
                    freq.doubles[i] = val;
                }
            }
        }
        if (booleans.get(FFData.indexhasLook)) {
            final List<Float> floats = packet.getFloat().getValues();
            if (floats.size() != freq.floats.length) {
                cancelRedundant = false;
                return;
            }
            for (int i = 0; i < freq.floats.length; i++) {
                final float val = floats.get(i).floatValue();
                if (val != freq.floats[i]) {
                    redundant = false;
                    freq.floats[i] = val;
                }
            }
        }
        if (redundant) {
            // TODO: Could check first bucket or even just 50 ms to last packet.
            if (allScore / cc.flyingFrequencySeconds > 20f) {
                counters.add(idRedundant, 1);
                event.setCancelled(true);
            }
        }
    }

}
