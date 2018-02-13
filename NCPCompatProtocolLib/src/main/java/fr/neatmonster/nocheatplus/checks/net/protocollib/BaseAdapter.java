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
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.debug.IDebugPlayer;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/**
 * Convenience base class for PacketAdapter creation with using config, data, counters.
 * @author asofold
 *
 */
public abstract class BaseAdapter extends PacketAdapter implements IDebugPlayer {

    protected final Counters counters = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class);

    /** Override for specific output on the debug method. */
    protected CheckType checkType = CheckType.NET;

    public BaseAdapter(AdapterParameteters params) {
        super(params);
    }

    public BaseAdapter(Plugin plugin, Iterable<? extends PacketType> types) {
        super(plugin, types);
    }

    public BaseAdapter(Plugin plugin, ListenerPriority listenerPriority, Iterable<? extends PacketType> types, ListenerOptions... options) {
        super(plugin, listenerPriority, types, options);
    }

    public BaseAdapter(Plugin plugin, ListenerPriority listenerPriority, Iterable<? extends PacketType> types) {
        super(plugin, listenerPriority, types);
    }

    public BaseAdapter(Plugin plugin, ListenerPriority listenerPriority, PacketType... types) {
        super(plugin, listenerPriority, types);
    }

    public BaseAdapter(Plugin plugin, PacketType... types) {
        super(plugin, types);
    }

    @Override
    public void debug(final Player player, final String message) {
        CheckUtils.debug(player, checkType, message);
    }

}
