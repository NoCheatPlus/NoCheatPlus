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
 * Rei's Minimap v3.3_04
 * 
 * http://www.minecraftforum.net/topic/482147-151-mar21-reis-minimap-v33-04/
 * 
 * &0&0: prefix
 * &e&f: suffix
 * &1: cave mapping
 * &2: entities radar (player)
 * &3: entities radar (animal)
 * &4: entities radar (mob)
 * &5: entities radar (slime)
 * &6: entities radar (squid)
 * &7: entities radar (other living)
 * 
 * @author mc_dev
 *
 */
public class ReiMOTD extends ClientMOTD {

    @Override
    public String onPlayerJoin(final String message, final Player player, final IPlayerData data, 
            final boolean allowAll) {
        String rei = "";

        // Allow Rei's Minimap's cave mode.
        if (allowAll || data.hasPermission(Permissions.REI_CAVE, player)){
            rei += "§1";
        }

        // Allow Rei's Minimap's radar.
        if (allowAll || data.hasPermission(Permissions.REI_RADAR, player)){
            // TODO: Does this allow all radar features?
            rei += "§2§3§4§5§6§7";
        }
        else{
            // Allow Rei's Minimap's player radar
            if (allowAll || data.hasPermission(Permissions.REI_RADAR_PLAYER, player)){
                rei += "§2";
            }

            // Allow Rei's Minimap's animal radar
            if (allowAll || data.hasPermission(Permissions.REI_RADAR_ANIMAL, player)){
                rei += "§3";
            }

            // Allow Rei's Minimap's mob radar
            if (allowAll || data.hasPermission(Permissions.REI_RADAR_MOB, player)){
                rei += "§4";
            }

            // Allow Rei's Minimap's slime radar
            if (allowAll || data.hasPermission(Permissions.REI_RADAR_SLIME, player)){
                rei += "§5";
            }

            // Allow Rei's Minimap's squid radar
            if (allowAll || data.hasPermission(Permissions.REI_RADAR_SQUID, player)){
                rei += "§6";
            }

            // Allow Rei's Minimap's other radar
            if (allowAll || data.hasPermission(Permissions.REI_RADAR_OTHER, player)){
                rei += "§7";
            }
        }

        if (rei.isEmpty()){
            return message;
        }
        else{
            rei = "§0§0" + rei + "§e§f";
            return message + rei;
        }
    }

}
