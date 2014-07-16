package fr.neatmonster.nocheatplus.net.protocollib;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import fr.neatmonster.nocheatplus.utilities.TrigUtil;

public class WeatherDistance extends PacketAdapter {
	
	// TODO: Will not be effective with 512 radius, if they add the patch by @Amranth.
	// TODO: For lower distances more packets might need to be intercepted.
	
	/** Maximum distance for thunder effects (squared). */
    private static final double distSq = 512.0 * 512.0; // TODO: Maybe configurable.

	public WeatherDistance(Plugin plugin) {
        super(plugin, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        final PacketContainer packetContainer = event.getPacket();
        final Player player = event.getPlayer();
        
        // Compare sound effect name.
        // TODO: wither-spawn-sound-radius, dragon-death-sound-radius, other ?
        if (!packetContainer.getStrings().read(0).equals("ambient.weather.thunder")) {
        	return;
        }
        
        final Location loc = player.getLocation(); // TODO: Use getLocation(useLoc) [synced if async].
        
        // Compare distance of player to the weather location.
        final StructureModifier<Integer> ints = packetContainer.getIntegers();
        if (TrigUtil.distanceSquared(ints.read(0) / 8, ints.read(1) / 8, ints.read(2) / 8, loc.getX(), loc.getY(), loc.getZ()) > distSq) {
            event.setCancelled(true);
        }
    }
    
}
