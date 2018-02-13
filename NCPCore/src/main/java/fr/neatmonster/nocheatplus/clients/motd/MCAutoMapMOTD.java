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

public class MCAutoMapMOTD extends ClientMOTD {

    @Override
    public String onPlayerJoin(final String message, final Player player, final IPlayerData data, 
            final boolean allowAll) {

        if (allowAll){
            return message;
        }

        String mcAutoMap = "";

        // TODO: Is there a compact version (just one prefix)?

        // Disable Minecraft AutoMap's ores.
        if (!data.hasPermission(Permissions.MINECRAFTAUTOMAP_ORES, player)){
            mcAutoMap += "§0§0§1§f§e";
        }

        // Disable Minecraft AutoMap's cave mode.
        if (!data.hasPermission(Permissions.MINECRAFTAUTOMAP_CAVE, player)){
            mcAutoMap += "§0§0§2§f§e";
        }

        // Disable Minecraft AutoMap's radar.
        if (!data.hasPermission(Permissions.MINECRAFTAUTOMAP_RADAR, player)){
            mcAutoMap += "§0§0§3§4§5§6§7§8§f§e";
        }

        if (mcAutoMap.isEmpty()){
            return message;
        }
        else{
            return message + mcAutoMap;
        }
    }

}
