package fr.neatmonster.nocheatplus.checks.net.protocollib;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetConfigCache;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

public class SoundDistance extends PacketAdapter {

    // TODO: Will not be effective with 512 radius, if they add the patch by @Amranth.
    // TODO: For lower distances more packets might need to be intercepted.

    private static final String[] effectNames = new String[] { // Prefix tree?
        "ambient.weather.thunder",
        "wither-spawn-sound-radius", 
        "dragon-death-sound-radius"
        // other ?
    };

    private static final boolean contains(final String ref) {
        for (int i = 0; i < effectNames.length; i++) {
            if (effectNames[i].equals(ref)) {
                return true;
            }
        }
        return false;
    }

    private final NetConfigCache configs;
    private final Location useLoc = new Location(null, 0, 0, 0);

    public SoundDistance(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, PacketType.Play.Server.NAMED_SOUND_EFFECT);
        this.configs = (NetConfigCache) CheckType.NET.getConfigFactory(); // TODO: DataManager.getConfig(NetConfigCache.class);
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        final PacketContainer packetContainer = event.getPacket();

        // Compare sound effect name.
        if (!contains(packetContainer.getStrings().read(0))) {
            return;
        }

        final Player player = event.getPlayer();
        final NetConfig cc = configs.getConfig(player.getWorld());
        if (!cc.soundDistanceActive) {
            return;
        }

        final Location loc = player.getLocation(useLoc);
        // Compare distance of player to the weather location.
        final StructureModifier<Integer> ints = packetContainer.getIntegers();
        if (TrigUtil.distanceSquared(ints.read(0) / 8, ints.read(2) / 8, loc.getX(), loc.getZ()) > cc.soundDistanceSq) {
            event.setCancelled(true);
        }
        useLoc.setWorld(null);
    }

}
