package fr.neatmonster.nocheatplus.checks.net.protocollib;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.net.NetConfigCache;
import fr.neatmonster.nocheatplus.checks.net.NetDataFactory;
import fr.neatmonster.nocheatplus.components.IDebugPlayer;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/**
 * Convenience base class for PacketAdapter creation with using config, data, counters.
 * @author asofold
 *
 */
public abstract class BaseAdapter extends PacketAdapter implements IDebugPlayer {

    protected final Counters counters = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class);
    protected final NetConfigCache configFactory = (NetConfigCache) CheckType.NET.getConfigFactory();
    protected final NetDataFactory dataFactory = (NetDataFactory) CheckType.NET.getDataFactory();

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
