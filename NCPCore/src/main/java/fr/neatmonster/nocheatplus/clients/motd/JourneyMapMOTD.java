package fr.neatmonster.nocheatplus.clients.motd;

import fr.neatmonster.nocheatplus.permissions.Permissions;
import org.bukkit.entity.Player;

public class JourneyMapMOTD extends ClientMOTD {

    @Override
    public String onPlayerJoin(String message, Player player, boolean allowAll) {

        if (allowAll) {
            return message;
        }

        String journeyMap = "";

        // Disable JourneyMap's Radar.
        if (!player.hasPermission(Permissions.JOURNEY_RADAR)) {
            journeyMap += "§3 §6 §3 §6 §3 §6 §e";
        }

        // Disable JourneyMap's CaveMap.
        if (!player.hasPermission(Permissions.JOURNEY_CAVE)) {
            journeyMap += "§3 §6 §3 §6 §3 §6 §d";
        }

        if (journeyMap.isEmpty()) {
            return message;
        } else {
            return message + journeyMap;
        }
    }

}
