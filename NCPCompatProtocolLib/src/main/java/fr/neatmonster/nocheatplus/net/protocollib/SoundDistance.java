package fr.neatmonster.nocheatplus.net.protocollib;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
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

    /** Maximum distance for thunder effects (squared). */
    private final double distSq;

    public SoundDistance(Plugin plugin) {
        super(plugin, PacketType.Play.Server.NAMED_SOUND_EFFECT);
        ConfigFile config = ConfigManager.getConfigFile();
        double dist = config.getDouble(ConfPaths.NET_SOUNDDISTANCE_MAXDISTANCE);
        distSq = dist * dist;
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        final PacketContainer packetContainer = event.getPacket();

        // Compare sound effect name.
        if (!contains(packetContainer.getStrings().read(0))) {
            return;
        }

        final Player player = event.getPlayer();
        final Location loc = player.getLocation(); // TODO: Use getLocation(useLoc) [synced if async].

        // Compare distance of player to the weather location.
        final StructureModifier<Integer> ints = packetContainer.getIntegers();
        if (TrigUtil.distanceSquared(ints.read(0) / 8, ints.read(2) / 8, loc.getX(), loc.getZ()) > distSq) {
            // TODO: Get from a NetConfig (optimized).
            if (ConfigManager.getConfigFile(player.getWorld().getName()).getBoolean(ConfPaths.NET_SOUNDDISTANCE_ACTIVE)) {
                event.setCancelled(true);
            }
        }
    }

}
