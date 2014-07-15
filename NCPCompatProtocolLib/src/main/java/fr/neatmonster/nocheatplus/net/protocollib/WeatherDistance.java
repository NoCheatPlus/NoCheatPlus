package fr.neatmonster.nocheatplus.net.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WeatherDistance extends PacketAdapter {

    public WeatherDistance(Plugin plugin) {
        super(plugin, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packetContainer = event.getPacket();
        Player player = event.getPlayer();

        String soundEffect = packetContainer.getStrings().read(0);

        if (!soundEffect.equals("ambient.weather.thunder"))
            return;

        double locX = packetContainer.getIntegers().read(0) / 8;
        double locY = packetContainer.getIntegers().read(1) / 8;
        double locZ = packetContainer.getIntegers().read(2) / 8;

        Location weatherLocation = new Location(player.getWorld(), locX, locY, locZ);
        Location location = player.getLocation().clone();

        if (player.getLocation().distance(weatherLocation) > 512.0F) {
            event.setCancelled(true);
        }
    }
}
