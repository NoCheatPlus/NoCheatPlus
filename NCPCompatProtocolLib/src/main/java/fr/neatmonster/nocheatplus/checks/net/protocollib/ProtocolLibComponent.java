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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetConfigCache;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.checks.net.NetDataFactory;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.components.registry.feature.IDisableListener;
import fr.neatmonster.nocheatplus.components.registry.feature.INotifyReload;
import fr.neatmonster.nocheatplus.components.registry.feature.JoinLeaveListener;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Quick and dirty ProtocolLib setup.
 * 
 * @author asofold
 *
 */
public class ProtocolLibComponent implements IDisableListener, INotifyReload, JoinLeaveListener, Listener {

    // TODO: Static reference is problematic (needs a static and accessible Counters instance?). 
    public static final int idNullPlayer = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class).registerKey("packet.flying.nullplayer");

    private final List<PacketAdapter> registeredPacketAdapters = new LinkedList<PacketAdapter>();

    protected final NetConfigCache configFactory = (NetConfigCache) CheckType.NET.getConfigFactory();
    protected final NetDataFactory dataFactory = (NetDataFactory) CheckType.NET.getDataFactory();

    public ProtocolLibComponent(Plugin plugin) {
        register(plugin);
    }

    private void register(Plugin plugin) {
        StaticLog.logInfo("Adding packet level hooks for ProtocolLib (MC " + ProtocolLibrary.getProtocolManager().getMinecraftVersion().getVersion() + ")...");
        //Special purpose.
        if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET + ConfPaths.SUB_DEBUG) || ConfigManager.isTrueForAnyConfig(ConfPaths.CHECKS_DEBUG) ) {
            // (Debug logging. Only activates if debug is set for checks or checks.net, not on the fly.)
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.DebugAdapter", plugin);
        }
        // Actual checks.
        if (ServerVersion.compareMinecraftVersion("1.6.4") <= 0) {
            // Don't use this listener.
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Disable EntityUseAdapter due to incompatibilities. Use fight.speed instead of net.attackfrequency.");
        }
        else if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_ATTACKFREQUENCY_ACTIVE)) {
            // (Also sets lastKeepAliveTime, if enabled.)
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.UseEntityAdapter", plugin);
        }
        if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_FLYINGFREQUENCY_ACTIVE)) {
            // (Also sets lastKeepAliveTime, if enabled.)
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.MovingFlying", plugin);
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.OutgoingPosition", plugin);
        }
        if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIVE) || ConfigManager.isTrueForAnyConfig(ConfPaths.FIGHT_GODMODE_CHECK)) {
            // (Set lastKeepAlive if this or fight.godmode is enabled.)
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.KeepAliveAdapter", plugin);
        }
        if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_SOUNDDISTANCE_ACTIVE)) {
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.SoundDistance", plugin);
        }
        if (ConfigManager.isAlmostTrueForAnyConfig(ConfPaths.NET_PACKETFREQUENCY_ACTIVE, ServerVersion.compareMinecraftVersion("1.9") < 0, false)) {
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.CatchAllAdapter", plugin);
        }
        if (!registeredPacketAdapters.isEmpty()) {
            List<String> names = new ArrayList<String>(registeredPacketAdapters.size());
            for (PacketAdapter adapter : registeredPacketAdapters) {
                names.add(adapter.getClass().getSimpleName());
            }
            StaticLog.logInfo("Available (and activated) packet level hooks: " + StringUtil.join(names, " | "));
            NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("packet-listeners", names);
        } else {
            StaticLog.logInfo("No packet level hooks activated.");
        }
    }

    @SuppressWarnings("unchecked")
    private void register(String name, Plugin plugin) {
        Throwable t = null;
        try {
            Class<?> clazz = Class.forName(name);
            register((Class<? extends PacketAdapter>) clazz, plugin);
            return;
        } catch (ClassNotFoundException e) {
            t = e;
        } catch (ClassCastException e) {
            t = e;
        }
        StaticLog.logWarning("Could not register packet level hook: " + name);
        StaticLog.logWarning(t);
    }

    private void register(Class<? extends PacketAdapter> clazz, Plugin plugin) {
        try {
            // Construct a new instance using reflection.
            PacketAdapter adapter = clazz.getDeclaredConstructor(Plugin.class).newInstance(plugin);
            ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
            registeredPacketAdapters.add(adapter);
        } catch (Throwable t) {
            StaticLog.logWarning("Could not register packet level hook: " + clazz.getSimpleName());
            StaticLog.logWarning(t);
        }
    }

    @Override
    public void onDisable() {
        unregister();
    }

    @Override
    public void onReload() {
        unregister();
        CheckType.NET.getDataFactory().removeAllData(); // Currently needed for FlyingFrequency.
        register(Bukkit.getPluginManager().getPlugin("NoCheatPlus")); // Store instead ?
    }

    private void unregister() {
        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        final NoCheatPlusAPI api = NCPAPIProvider.getNoCheatPlusAPI();
        for (PacketAdapter adapter : registeredPacketAdapters) {
            try {
                protocolManager.removePacketListener(adapter);
                api.removeComponent(adapter); // Bit heavy, but consistent.
            } catch (Throwable t) {
                StaticLog.logWarning("Failed to unregister packet level hook: " + adapter.getClass().getName());
            }// TODO Auto-generated method stub

        }
        registeredPacketAdapters.clear();
    }

    @Override
    public void playerJoins(final Player player) {
        if (!registeredPacketAdapters.isEmpty()) {
            dataFactory.getData(player).onJoin(player);
        }
    }

    @Override
    public void playerLeaves(final Player player) {
        if (!registeredPacketAdapters.isEmpty()) {
            dataFactory.getData(player).onLeave(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (!registeredPacketAdapters.isEmpty()) {
            final Player player = event.getPlayer();
            final NetData data = dataFactory.getData(player);
            data.onJoin(player);
            final Location loc = event.getRespawnLocation();
            data.teleportQueue.onTeleportEvent(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        // TODO: Might move to MovingListener.
        // TODO: Might still add cancelled UNKNOWN events. TEST IT
        final Location to = event.getTo();
        if (to == null) {
            return;
        }
        final Player player = event.getPlayer();
        final NetConfig cc = configFactory.getConfig(player);
        final NetData data = dataFactory.getData(player);
        if (cc.flyingFrequencyActive) {
            // Register expected location for comparison with outgoing packets.
            data.teleportQueue.onTeleportEvent(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
        }
        data.clearFlyingQueue();
    }

}
