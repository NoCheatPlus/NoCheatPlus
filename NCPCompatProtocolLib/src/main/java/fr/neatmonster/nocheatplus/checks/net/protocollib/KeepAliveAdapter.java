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

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.net.KeepAliveFrequency;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.PlayerData;

/**
 * Limit keep alive packet frequency, set lastKeepAliveTime (even if disabled,
 * in case fight.godmode is enabled).
 * 
 * @author asofold
 *
 */
public class KeepAliveAdapter extends BaseAdapter {

    /** Dummy check for bypass checking and actions execution. */
    private final KeepAliveFrequency frequencyCheck = new KeepAliveFrequency();

    public KeepAliveAdapter(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, PacketType.Play.Client.KEEP_ALIVE);

        // Add feature tags for checks.
        if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIVE)) {
            NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(KeepAliveFrequency.class.getSimpleName()));
        }
        NCPAPIProvider.getNoCheatPlusAPI().addComponent(frequencyCheck);
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {
        final long time =  System.currentTimeMillis();
        final Player player = event.getPlayer();
        if (player == null) {
            counters.add(ProtocolLibComponent.idNullPlayer, 1);
            event.setCancelled(true);
            return;
        }

        // Always update last received time.
        final PlayerData pData = DataManager.getPlayerData(player);
        final NetData data = dataFactory.getData(player);
        data.lastKeepAliveTime = time;
        final NetConfig cc = configFactory.getConfig(player);

        // Run check(s).
        // TODO: Match vs. outgoing keep alive requests.
        // TODO: Better modeling of actual packet sequences (flying vs. keep alive vs. request/ping).
        // TODO: Better integration with god-mode check / trigger reset ndt.
        if (cc.keepAliveFrequencyActive && frequencyCheck.check(player, time, data, cc, pData)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        // TODO: Maybe detect if keep alive wasn't asked for + allow cancel.
    }

}
