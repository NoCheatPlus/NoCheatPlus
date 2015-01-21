package fr.neatmonster.nocheatplus.net.protocollib;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.net.NetConfig;
import fr.neatmonster.nocheatplus.net.NetConfigCache;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.time.monotonic.Monotonic;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Prevent extremely fast ticking by just sending packets that don't do anything
 * new and also don't trigger moving events in CraftBukkit.
 * 
 * @author dev1mc
 *
 */
public class FlyingFrequency extends PacketAdapter implements JoinLeaveListener {

    public static final double minMoveDistSq = 1f / 256; // PlayerConnection magic.

    public static final float minLookChange = 10f;

    // TODO: Most efficient registration + optimize (primary thread or asynchronous).

    private class FFData {
        public static final int numBooleans = 3;
        public static final int indexOnGround = 0;
        public static final int indexhasPos = 1;
        public static final int indexhasLook = 2;

        public final ActionFrequency all;
        // Last move on-ground.
        public boolean onGround = false;
        public long timeOnGround = 0;
        public long timeNotOnGround = 0;

        public FFData(int seconds) {
            all = new ActionFrequency(seconds, 1000L);
        }
    }

    private final Map<String, FFData> freqMap = new HashMap<String, FFData>();  
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

    private FFData getData(final String name, final NetConfig cc) {
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

        final FFData data = getData(player.getName(), cc);
        final long t = System.currentTimeMillis();
        // Counting all packets.
        data.all.add(t, 1f);
        final float allScore = data.all.score(1f);
        if (allScore > cc.flyingFrequencyMaxPackets) {
            counters.add(idSilent, 1); // Until it is sure if we can get these async.
            event.setCancelled(true);
            return;
        }

        // Cancel redundant packets, when frequency is high anyway.
        if (cancelRedundant && cc.flyingFrequencyCancelRedundant && checkRedundantPackets(player, event, allScore, data, cc)) {
            event.setCancelled(true);
        }

    }

    private boolean checkRedundantPackets(final Player player, final PacketEvent event, final float allScore, final FFData data, final NetConfig cc) {
        // TODO: Consider quick return conditions.
        // TODO: Debug logging (better with integration into DataManager).
        // TODO: Consider to compare to moving data directly, skip keeping track extra.

        final PacketContainer packet = event.getPacket();
        final List<Boolean> booleans = packet.getBooleans().getValues();
        if (booleans.size() != FFData.numBooleans) {
            return packetMismatch();
        }

        final MovingData mData = MovingData.getData(player);
        if (mData.toX == Double.MAX_VALUE && mData.toYaw == Float.MAX_VALUE) {
            // Can not check.
            return false;
        }
        final boolean hasPos = booleans.get(FFData.indexhasPos);
        final boolean hasLook = booleans.get(FFData.indexhasLook);
        final boolean onGround = booleans.get(FFData.indexOnGround).booleanValue();
        boolean onGroundSkip = false;

        // Allow at least one on-ground change per state and second.
        // TODO: Consider to verify on ground somehow (could tell MovingData the state).
        if (onGround != data.onGround) {
            // Regard as not redundant only if sending the same state happened at least a second ago. 
            final long time = Monotonic.millis();
            final long lastTime;
            if (onGround) {
                lastTime = data.timeOnGround;
                data.timeOnGround = time;
            } else {
                lastTime = data.timeNotOnGround;
                data.timeNotOnGround = time;
            }
            if (time - lastTime > 1000) {
                // Override 
                onGroundSkip = true;
            }
        }
        data.onGround = onGround;

        if (hasPos) {
            final List<Double> doubles = packet.getDoubles().getValues();
            if (doubles.size() != 3) {
                return packetMismatch();
            }
            final double x = doubles.get(0).doubleValue();
            final double y = doubles.get(1).doubleValue();
            final double z = doubles.get(2).doubleValue();
            if (CheckUtils.isBadCoordinate(x, y, z)) {
                // TODO: Alert, counters, kick.
                return true;
            }
            if (TrigUtil.distanceSquared(x, y, z, mData.toX, mData.toY, mData.toZ) > minMoveDistSq) {
                return false;
            }
        }

        if (hasLook) {
            final List<Float> floats = packet.getFloat().getValues();
            if (floats.size() != 2) {
                return packetMismatch();
            }
            final float yaw = floats.get(0).floatValue();
            final float pitch = floats.get(1).floatValue();
            // TODO: Consider to detect bad pitch too.
            if (CheckUtils.isBadCoordinate(yaw, pitch)) {
                // TODO: Alert, counters, kick.
                return true;
            }
            if (Math.abs(TrigUtil.yawDiff(yaw, mData.toYaw)) > minLookChange || Math.abs(TrigUtil.yawDiff(pitch, mData.toPitch)) > minLookChange) {
                return false;
            }
        }

        if (onGroundSkip) {
            return false;
        }

        // TODO: Could check first bucket or even just 50 ms to last packet.
        if (allScore / cc.flyingFrequencySeconds > 20f) {
            counters.add(idRedundant, 1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Log warning to console, halt checking for redundant packets.
     * @return Always returns false;
     */
    private boolean packetMismatch() {
        cancelRedundant = false;
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, "[NoCheatPlus] Data mismatch: disable cancelling of redundant packets.");
        return false;
    }

}
