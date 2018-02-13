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
package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * JourneyMap + VoxelMap mod.
 * @author asofold
 *
 */
public class JourneyMapMOTD extends ClientMOTD {

    @Override
    public String onPlayerJoin(final String message, final Player player, final IPlayerData data, final boolean allowAll) {

        if (allowAll) {
            return message;
        }

        String journeyMap = "";

        // Disable JourneyMap's Radar.
        if (!data.hasPermission(Permissions.JOURNEY_RADAR, player)) {
            journeyMap += "§3 §6 §3 §6 §3 §6 §e";
        }

        // Disable JourneyMap's CaveMap.
        if (!data.hasPermission(Permissions.JOURNEY_CAVE, player)) {
            journeyMap += "§3 §6 §3 §6 §3 §6 §d";
        }

        if (journeyMap.isEmpty()) {
            return message;
        } else {
            return message + journeyMap;
        }
    }

}
