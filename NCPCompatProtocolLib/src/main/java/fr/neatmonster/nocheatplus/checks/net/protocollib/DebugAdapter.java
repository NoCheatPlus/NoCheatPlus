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

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.players.DataManager;

public class DebugAdapter extends BaseAdapter {

    public DebugAdapter(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, new PacketType[] {
                PacketType.Play.Client.BLOCK_PLACE,
                PacketType.Play.Client.BLOCK_DIG,
        });
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        final Player player = event.getPlayer();
        if (DataManager.getPlayerDataSafe(player).isDebugActive(CheckType.NET)) {
            debug(player, "packet: " + event.getPacketType());
        }
    }

}
