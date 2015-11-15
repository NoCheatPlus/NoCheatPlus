package fr.neatmonster.nocheatplus.checks.net.protocollib;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.checks.net.model.DataPacketFlying;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Prevent extremely fast ticking by just sending packets that don't do anything
 * new and also don't trigger moving events in CraftBukkit. Also update lastKeepAliveTime.
 * 
 * @author dev1mc
 *
 */
public class FlyingFrequency extends BaseAdapter {

    // Setup for flying packets.
    public static final int numBooleans = 3;
    public static final int indexOnGround = 0;
    public static final int indexhasPos = 1;
    public static final int indexhasLook = 2;
    public static final int indexX = 0;
    public static final int indexY = 1;
    public static final int indexZ = 2;
    public static final int indexYaw = 0;
    public static final int indexPitch = 1;

    // Thresholds for firing moving events (CraftBukkit).
    public static final double minMoveDistSq = 1f / 256; // PlayerConnection magic.
    public static final float minLookChange = 10f;

    /** Dummy check for bypass checking and actions execution. */
    private final Check frequency = new Check(CheckType.NET_FLYINGFREQUENCY) {};

    private final int idHandled = counters.registerKey("packet.flying.handled");
    private final int idAsyncFlying = counters.registerKey("packet.flying.asynchronous");

    /** Set to true, if a packet can't be interpreted, assuming compatibility to be broken. */
    private boolean packetMismatch = false;

    public FlyingFrequency(Plugin plugin) {
        // PacketPlayInFlying[3, legacy: 10]
        super(plugin, ListenerPriority.LOW, new PacketType[] {
                PacketType.Play.Client.FLYING,
                PacketType.Play.Client.LOOK,
                PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK
        });
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {
        final long time =  System.currentTimeMillis();
        final Player player = event.getPlayer();
        if (player == null) {
            // TODO: Need config?
            counters.add(ProtocolLibComponent.idNullPlayer, 1);
            event.setCancelled(true);
            return;
        }

        final NetConfig cc = configFactory.getConfig(player.getWorld());
        if (!cc.flyingFrequencyActive) {
            return;
        }

        counters.add(idHandled, 1);

        final NetData data = dataFactory.getData(player);
        data.lastKeepAliveTime = time; // Update without much of a contract.

        final boolean primaryThread = !event.isAsync();
        if (!primaryThread) {
            // Count all asynchronous events.
            counters.addSynchronized(idAsyncFlying, 1);
            // TODO: Detect game phase for the player and warn if it is PLAY.
        }

        // Interpret the packet content.
        final DataPacketFlying packetData = packetMismatch ? null : interpretPacket(event, time);

        // Early return tests, if the packet can be interpreted.
        if (packetData != null) {
            // Prevent processing packets with obviously malicious content.
            if (isInvalidContent(packetData)) {
                // TODO: More specific, log and kick or log once [/limited] ?
                event.setCancelled(true);
                if (data.debug) {
                    NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " sends a flying packet with malicious content.");
                }
                return;
            }
            if (cc.flyingFrequencyStrayPacketsCancel) {
                switch(data.teleportQueue.processAck(packetData)) {
                    case CANCEL: {
                        // TODO: Configuration for cancel (or implement skipping violation level escalation)?
                        event.setCancelled(true);
                        if (data.debug) {
                            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " wait for ACK on teleport, cancel packet: " + packetData);
                        }
                        return;
                    }
                    case ACK: {
                        // Skip processing ACK packets, no cancel.
                        if (data.debug) {
                            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " interpret as ACK for a teleport: " + packetData);
                        }
                        return;
                    }
                    default: {
                        // Continue.
                    }
                }
            }
        }

        // TODO: Counters for hasPos, hasLook, both, none.

        // Actual packet frequency check.
        // TODO: Consider using the NetStatic check.
        data.flyingFrequencyAll.add(time, 1f);
        final float allScore = data.flyingFrequencyAll.score(1f);
        if (allScore / cc.flyingFrequencySeconds > cc.flyingFrequencyPPS && !frequency.hasBypass(player) && frequency.executeActions(player, allScore / cc.flyingFrequencySeconds - cc.flyingFrequencyPPS, 1.0 / cc.flyingFrequencySeconds, cc.flyingFrequencyActions)) {
            event.setCancelled(true);
            return;
        }

        // TODO: Run other checks based on the packet content.

        // Cancel redundant packets, when frequency is high anyway.
        // TODO: Recode to detect cheating in a more reliable way, normally this is not the primary thread.
        //        if (primaryThread && !packetMismatch && cc.flyingFrequencyRedundantActive && checkRedundantPackets(player, packetData, allScore, time, data, cc)) {
        //            event.setCancelled(true);
        //        }

        if (data.debug) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " " + packetData + (event.isCancelled() ? " CANCEL" : ""));
        }

    }

    private boolean isInvalidContent(final DataPacketFlying packetData) {
        if (packetData.hasPos && CheckUtils.isBadCoordinate(packetData.x, packetData.y, packetData.z)) {
            return true;
        }
        if (packetData.hasLook && CheckUtils.isBadCoordinate(packetData.yaw, packetData.pitch)) {
            return true;
        }
        return false;
    }

    /**
     * Interpret the packet content and do with it whatever is suitable.
     * @param player
     * @param event
     * @param allScore
     * @param time
     * @param data
     * @param cc
     * @return Packet data if successful, or null on packet mismatch.
     */
    private DataPacketFlying interpretPacket(final PacketEvent event, final long time) {

        final PacketContainer packet = event.getPacket();
        final List<Boolean> booleans = packet.getBooleans().getValues();
        if (booleans.size() != FlyingFrequency.numBooleans) {
            packetMismatch();
            return null;
        }
        final boolean hasPos = booleans.get(FlyingFrequency.indexhasPos).booleanValue();
        final boolean hasLook = booleans.get(FlyingFrequency.indexhasLook).booleanValue();
        final boolean onGround = booleans.get(FlyingFrequency.indexOnGround).booleanValue();

        if (!hasPos && !hasLook) {
            return new DataPacketFlying(onGround, time);
        } else {
            final List<Double> doubles;
            final List<Float> floats;

            if (hasPos) {
                doubles = packet.getDoubles().getValues();
                if (doubles.size() != 3) {
                    packetMismatch();
                    return null;
                }
            }
            else {
                doubles = null;
            }

            if (hasLook) {
                floats = packet.getFloat().getValues();
                if (floats.size() != 2) {
                    packetMismatch();
                    return null;
                }
            }
            else {
                floats = null;
            }
            if (hasPos && hasLook) {
                return new DataPacketFlying(onGround, doubles.get(indexX), doubles.get(indexY), doubles.get(indexZ), floats.get(indexYaw), floats.get(indexPitch), time);
            }
            else if (hasLook) {
                return new DataPacketFlying(onGround, floats.get(indexYaw), floats.get(indexPitch), time);
            }
            else if (hasPos) {
                return new DataPacketFlying(onGround, doubles.get(indexX), doubles.get(indexY), doubles.get(indexZ), time);
            }
            else {
                throw new IllegalStateException("Can't be, it can't be!");
            }
        }
    }

    @SuppressWarnings("unused")
    private boolean checkRedundantPackets(final Player player, final DataPacketFlying packetData, final float allScore, final long time, final NetData data, final NetConfig cc) {
        // TODO: Debug logging (better with integration into DataManager).
        // TODO: Consider to compare to moving data directly, skip keeping track extra.

        final MovingData mData = MovingData.getData(player);
        if (mData.toX == Double.MAX_VALUE && mData.toYaw == Float.MAX_VALUE) {
            // Can not check.
            return false;
        }

        boolean onGroundSkip = false;

        // Allow at least one on-ground change per state and second.
        // TODO: Consider to verify on ground somehow (could tell MovingData the state).
        if (packetData.onGround != data.flyingFrequencyOnGround) {
            // Regard as not redundant only if sending the same state happened at least a second ago.
            final long lastTime;
            if (packetData.onGround) {
                lastTime = data.flyingFrequencyTimeOnGround;
                data.flyingFrequencyTimeOnGround = time;
            } else {
                lastTime = data.flyingFrequencyTimeNotOnGround;
                data.flyingFrequencyTimeNotOnGround = time;
            }
            if (time < lastTime || time - lastTime > 1000) {
                // Override 
                onGroundSkip = true;
            }
        }
        data.flyingFrequencyOnGround = packetData.onGround;

        if (packetData.hasPos) {
            if (TrigUtil.distanceSquared(packetData.x, packetData.y, packetData.z, mData.toX, mData.toY, mData.toZ) > minMoveDistSq) {
                return false;
            }
        }

        if (packetData.hasLook) {
            if (Math.abs(TrigUtil.yawDiff(packetData.yaw, mData.toYaw)) > minLookChange || Math.abs(TrigUtil.yawDiff(packetData.pitch, mData.toPitch)) > minLookChange) {
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
            if (frequency.executeActions(player, data.flyingFrequencyRedundantFreq.score(1f) / cc.flyingFrequencyRedundantSeconds, 1.0 / cc.flyingFrequencyRedundantSeconds, cc.flyingFrequencyRedundantActions)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Log warning to console, stop interpreting packet content.
     */
    private void packetMismatch() {
        packetMismatch = true;
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, "Data mismatch: disable interpretation of flying packets.");
    }

}
