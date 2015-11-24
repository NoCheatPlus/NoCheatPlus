package fr.neatmonster.nocheatplus.checks.net.protocollib;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.net.AttackFrequency;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;

public class UseEntityAdapter extends BaseAdapter {

    private final AttackFrequency attackFrequency;

    public UseEntityAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Client.USE_ENTITY);

        // Add feature tags for checks.
        if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_ATTACKFREQUENCY_ACTIVE)) {
            NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(AttackFrequency.class.getSimpleName()));
        }
        attackFrequency = new AttackFrequency();
        NCPAPIProvider.getNoCheatPlusAPI().addComponent(attackFrequency);
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {
        final long time = System.currentTimeMillis();
        final Player player = event.getPlayer();
        final NetConfig cc = configFactory.getConfig(player);
        final NetData data = dataFactory.getData(player);

        // Always set last received time.
        data.lastKeepAliveTime = time;

        // Quick return, if no checks are active.
        if (!cc.attackFrequencyActive) {
            return;
        }

        final PacketContainer packet = event.getPacket();
        final StructureModifier<EntityUseAction> actions = packet.getEntityUseActions();
        if (actions.size() != 1) {
            // TODO: Log warning once.
            return;
        }
        final EntityUseAction action = actions.read(0);

        boolean cancel = false;
        if (action == EntityUseAction.ATTACK && attackFrequency.isEnabled(player, data, cc) && attackFrequency.check(player, time, data, cc)) {
            cancel = true;
        }
        
        // MIGHT: use entity, use block both on packet level?

        if (cancel) {
            event.setCancelled(true);
        }
    }

}
