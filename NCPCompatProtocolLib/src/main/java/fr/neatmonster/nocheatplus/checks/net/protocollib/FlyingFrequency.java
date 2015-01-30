package fr.neatmonster.nocheatplus.checks.net.protocollib;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetConfigCache;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.checks.net.NetDataFactory;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.time.monotonic.Monotonic;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Prevent extremely fast ticking by just sending packets that don't do anything
 * new and also don't trigger moving events in CraftBukkit.
 * 
 * @author dev1mc
 *
 */
public class FlyingFrequency extends PacketAdapter {

    // Setup for flying packets.
    public static final int numBooleans = 3;
    public static final int indexOnGround = 0;
    public static final int indexhasPos = 1;
    public static final int indexhasLook = 2;

    // Thresholds for firing moving events (CraftBukkit).
    public static final double minMoveDistSq = 1f / 256; // PlayerConnection magic.
    public static final float minLookChange = 10f;

    /** Dummy check to access hasBypass for FlyingFrequency. */
    private final Check frequency = new Check(CheckType.NET_FLYINGFREQUENCY) {
        // Dummy check to access hasBypass.
    };

    private final Counters counters = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class);
    private final int idNullPlayer = counters.registerKey("packet.flying.nullplayer");

    private boolean cancelRedundant = true;

    private final NetConfigCache configs;
    private final NetDataFactory dataFactory;

    public FlyingFrequency(Plugin plugin) {
        // PacketPlayInFlying[3, legacy: 10]
        super(plugin, PacketType.Play.Client.FLYING); // TODO: How does POS and POS_LOOK relate/translate?
        this.configs = (NetConfigCache) CheckType.NET.getConfigFactory();
        this.dataFactory = (NetDataFactory) CheckType.NET.getDataFactory();
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

        final NetData data = dataFactory.getData(player);
        final long time =  Monotonic.millis();
        // Counting all packets.
        // TODO: Consider using the NetStatic check.
        data.flyingFrequencyAll.add(time, 1f);
        final float allScore = data.flyingFrequencyAll.score(1f);
        if (allScore / cc.flyingFrequencySeconds > cc.flyingFrequencyPPS && !frequency.hasBypass(player) && frequency.executeActions(player, allScore / cc.flyingFrequencySeconds - cc.flyingFrequencyPPS, 1.0 / cc.flyingFrequencySeconds, cc.flyingFrequencyActions, true)) {
            event.setCancelled(true);
            return;
        }

        // Cancel redundant packets, when frequency is high anyway.
        if (cancelRedundant && cc.flyingFrequencyRedundantActive && checkRedundantPackets(player, event, allScore, time, data, cc) ) {
            event.setCancelled(true);
        }

    }

    private boolean checkRedundantPackets(final Player player, final PacketEvent event, final float allScore, final long time, final NetData data, final NetConfig cc) {
        // TODO: Consider quick return conditions.
        // TODO: Debug logging (better with integration into DataManager).
        // TODO: Consider to compare to moving data directly, skip keeping track extra.

        final PacketContainer packet = event.getPacket();
        final List<Boolean> booleans = packet.getBooleans().getValues();
        if (booleans.size() != FlyingFrequency.numBooleans) {
            return packetMismatch();
        }

        final MovingData mData = MovingData.getData(player);
        if (mData.toX == Double.MAX_VALUE && mData.toYaw == Float.MAX_VALUE) {
            // Can not check.
            return false;
        }
        final boolean hasPos = booleans.get(FlyingFrequency.indexhasPos).booleanValue();
        final boolean hasLook = booleans.get(FlyingFrequency.indexhasLook).booleanValue();
        final boolean onGround = booleans.get(FlyingFrequency.indexOnGround).booleanValue();
        boolean onGroundSkip = false;

        // Allow at least one on-ground change per state and second.
        // TODO: Consider to verify on ground somehow (could tell MovingData the state).
        if (onGround != data.flyingFrequencyOnGround) {
            // Regard as not redundant only if sending the same state happened at least a second ago.
            final long lastTime;
            if (onGround) {
                lastTime = data.flyingFrequencyTimeOnGround;
                data.flyingFrequencyTimeOnGround = time;
            } else {
                lastTime = data.flyingFrequencyTimeNotOnGround;
                data.flyingFrequencyTimeNotOnGround = time;
            }
            if (time - lastTime > 1000) {
                // Override 
                onGroundSkip = true;
            }
        }
        data.flyingFrequencyOnGround = onGround;

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

        // Packet is redundant, if more than 20 packets per second arrive.
        if (allScore / cc.flyingFrequencySeconds > 20f && !frequency.hasBypass(player)) {
            // (Must re-check bypass here.)
            data.flyingFrequencyRedundantFreq.add(time, 1f);
            if (frequency.executeActions(player, data.flyingFrequencyRedundantFreq.score(1f) / cc.flyingFrequencyRedundantSeconds, 1.0 / cc.flyingFrequencyRedundantSeconds, cc.flyingFrequencyRedundantActions, true)) {
                return true;
            }
        }
        return false;
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
