package fr.neatmonster.nocheatplus.checks.net.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.net.EqualsRotate;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class EqualsRotateAdapter extends BaseAdapter {

    private final EqualsRotate EQUALSROTATE = new EqualsRotate();

    public EqualsRotateAdapter(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, PacketType.Play.Client.POSITION_LOOK, PacketType.Play.Client.LOOK);
        if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_EQALSROTATE_ACTIVE)) {
            NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(EqualsRotate.class.getSimpleName()));
        }
        NCPAPIProvider.getNoCheatPlusAPI().addComponent(EQUALSROTATE);
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {
        final Player player = event.getPlayer();
        if(player == null)return;
        final NetData netData = dataFactory.getData(player);
        final NetConfig cc = configFactory.getConfig(player);
        final StructureModifier<Float> floats = event.getPacket().getFloat();
        final float yaw = floats.read(0);
        final float pitch = floats.read(1);
        if(cc.equalsRotateActive && EQUALSROTATE.check(player, netData, cc, yaw, pitch)){
            event.setCancelled(true);
        }
    }
}
