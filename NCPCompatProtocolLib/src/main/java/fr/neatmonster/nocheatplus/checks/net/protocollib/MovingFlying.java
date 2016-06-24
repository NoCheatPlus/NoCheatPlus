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
package fr.neatmonster.nocheatplus.checks.net.protocollib;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.net.FlyingFrequency;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.checks.net.model.DataPacketFlying;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/**
 * Run checks related to moving (pos/look/flying). Skip packets that shouldn't
 * get processed anyway due to a teleport. Also update lastKeepAliveTime.
 * 
 * @author dev1mc
 *
 */
public class MovingFlying extends BaseAdapter {

    // Setup for flying packets.
    public static final int indexOnGround = 0;
    public static final int indexhasPos = 1;
    public static final int indexhasLook = 2;
    public static final int indexX = 0;
    public static final int indexY = 1;
    public static final int indexZ = 2;
    /** 1.7.10 */
    public static final int indexStance = 3;
    public static final int indexYaw = 0;
    public static final int indexPitch = 1;

    /** Dummy check for bypass checking and actions execution. */
    private final FlyingFrequency flyingFrequency = new FlyingFrequency();

    private final int idHandled = counters.registerKey("packet.flying.handled");
    private final int idAsyncFlying = counters.registerKey("packet.flying.asynchronous");

    /** Set to true, if a packet can't be interpreted, assuming compatibility to be broken. */
    private boolean packetMismatch = false;

    public MovingFlying(Plugin plugin) {
        // PacketPlayInFlying[3, legacy: 10]
        super(plugin, ListenerPriority.LOW, new PacketType[] {
                PacketType.Play.Client.FLYING,
                PacketType.Play.Client.LOOK,
                PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK
        });

        // Add feature tags for checks.
        if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_FLYINGFREQUENCY_ACTIVE)) {
            NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(FlyingFrequency.class.getSimpleName()));
        }
        NCPAPIProvider.getNoCheatPlusAPI().addComponent(flyingFrequency);
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
        // Always update last received time.
        final NetData data = dataFactory.getData(player);
        data.lastKeepAliveTime = time; // Update without much of a contract.
        if (!cc.flyingFrequencyActive) {
            return;
        }

        counters.add(idHandled, 1);

        final boolean primaryThread = !event.isAsync();
        if (!primaryThread) {
            // Count all asynchronous events.
            counters.addSynchronized(idAsyncFlying, 1);
            // TODO: Detect game phase for the player and warn if it is PLAY.
        }

        // Interpret the packet content.
        final DataPacketFlying packetData = packetMismatch ? null : interpretPacket(event, time);

        // Early return tests, if the packet can be interpreted.
        boolean skipFlyingFrequency = false;
        if (packetData != null) {
            // Prevent processing packets with obviously malicious content.
            if (isInvalidContent(packetData)) {
                // TODO: More specific, log and kick or log once [/limited] ?
                event.setCancelled(true);
                if (data.debug) {
                    debug(player, "Flying packet with malicious content.");
                }
                return;
            }
            switch(data.teleportQueue.processAck(packetData)) {
                case WAITING: {
                    if (data.debug) {
                        debug(player, "Incoming packet, still waiting for ACK on outgoing position.");
                    }
                }
                case ACK: {
                    // Skip processing ACK packets, no cancel.
                    skipFlyingFrequency = true;
                    if (data.debug) {
                        debug(player, "Incoming packet, interpret as ACK for outgoing position.");
                    }
                }
                default: {
                    // Continue.
                    data.addFlyingQueue(packetData); // TODO: Not the optimal position, perhaps.
                }
            }
        }

        // TODO: Counters for hasPos, hasLook, both, none.

        // Actual packet frequency check.
        // TODO: Consider using the NetStatic check.
        if (!skipFlyingFrequency && flyingFrequency.check(player, packetData, time, data, cc)) {
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
            debug(player, (packetData == null ? "(Incompatible data)" : packetData.toString()) + (event.isCancelled() ? " CANCEL" : ""));
        }

    }

    private boolean isInvalidContent(final DataPacketFlying packetData) {
        if (packetData.hasPos && CheckUtils.isBadCoordinate(packetData.getX(), packetData.getY(), packetData.getZ())) {
            return true;
        }
        if (packetData.hasLook && CheckUtils.isBadCoordinate(packetData.getYaw(), packetData.getPitch())) {
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
        if (booleans.size() != 3) {
            packetMismatch();
            return null;
        }
        final boolean onGround = booleans.get(MovingFlying.indexOnGround).booleanValue();
        final boolean hasPos = booleans.get(MovingFlying.indexhasPos).booleanValue();
        final boolean hasLook = booleans.get(MovingFlying.indexhasLook).booleanValue();

        if (!hasPos && !hasLook) {
            return new DataPacketFlying(onGround, time);
        }
        final List<Double> doubles;
        final List<Float> floats;

        if (hasPos) {
            doubles = packet.getDoubles().getValues();
            if (doubles.size() != 3 && doubles.size() != 4) {
                // 3: 1.8, 4: 1.7.10 and before (stance).
                packetMismatch();
                return null;
            }
            // TODO: before 1.8: stance (should make possible to reject in isInvalidContent).
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

    /**
     * Log warning to console, stop interpreting packet content.
     */
    private void packetMismatch() {
        packetMismatch = true;
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, "Data mismatch: disable interpretation of flying packets.");
    }

}
