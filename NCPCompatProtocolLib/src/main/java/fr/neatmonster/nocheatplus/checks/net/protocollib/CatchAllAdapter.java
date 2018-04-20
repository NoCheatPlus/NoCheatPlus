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

import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.checks.net.PacketFrequency;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * Pre-1.9.
 * 
 * @author asofold
 *
 */
public class CatchAllAdapter extends BaseAdapter {

    /**
     * Somehow determine types to monitor.
     * 
     * @return
     */
    private static Iterable<? extends PacketType> getPacketTypes() {
        // TODO: Config ?
        Set<PacketType> types = new LinkedHashSet<PacketType>();
        for (PacketType type : PacketType.Play.Client.getInstance().values()) {
            if (type.isSupported()) {
                types.add(type);
            }
        }
        //        // relax.
        //        types.add(PacketType.Play.Server.POSITION);
        return types;
    }

    private final PacketFrequency packetFrequency;

    public CatchAllAdapter(Plugin plugin) {
        super(plugin, ListenerPriority.LOWEST, getPacketTypes());
        packetFrequency = new PacketFrequency();
        NCPAPIProvider.getNoCheatPlusAPI().addComponent(packetFrequency);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        final Player player = event.getPlayer();
        if (player == null) {
            counters.add(ProtocolLibComponent.idNullPlayer, 1);
            // TODO: Is this a problem, as the server has the player so it could break a block)?
            return;
        }
        final IPlayerData pData = DataManager.getPlayerDataSafe(player);
        if (pData == null) {
            StaticLog.logWarning("Failed to fetch player data with " + event.getPacketType() + " for: " + player.toString());
            return;
        }
        if (packetFrequency.isEnabled(player, pData)) {
            final NetConfig cc = pData.getGenericInstance(NetConfig.class);
            final NetData data = pData.getGenericInstance(NetData.class);
            if (packetFrequency.check(player, data, cc)) {
                event.setCancelled(true);
            }
        }
    }

    //    @Override
    //    public void onPacketSending(PacketEvent event) {
    //        final Player player = event.getPlayer();
    //        final NetConfig cc = configFactory.getConfig(player);
    //        if (cc.packetFrequencyActive) {
    //            packetFrequency.relax(event.getPlayer(), dataFactory.getData(player), cc);
    //        }
    //    }

}
