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

public class SmartMovingMOTD extends ClientMOTD {

    @Override
    public String onPlayerJoin(final String message, final Player player, final IPlayerData data, 
            final boolean allowAll) {

        if (allowAll){
            return message;
        }

        String smartMoving = "";

        // TODO: Is there a compact version (just one prefix)?

        // Disable Smart Moving's climbing.
        if (!data.hasPermission(Permissions.SMARTMOVING_CLIMBING, player)){
            smartMoving += "§0§1§0§1§2§f§f";
        }

        // Disable Smart Moving's climbing.
        if (!data.hasPermission(Permissions.SMARTMOVING_SWIMMING, player)){
            smartMoving += "§0§1§3§4§f§f";
        }

        // Disable Smart Moving's climbing.
        if (!data.hasPermission(Permissions.SMARTMOVING_CRAWLING, player)){
            smartMoving += "§0§1§5§f§f";
        }

        // Disable Smart Moving's climbing.
        if (!data.hasPermission(Permissions.SMARTMOVING_SLIDING, player)){
            smartMoving += "§0§1§6§f§f";
        }

        // Disable Smart Moving's climbing.
        if (!data.hasPermission(Permissions.SMARTMOVING_JUMPING, player)){
            smartMoving += "§0§1§8§9§a§b§f§f";
        }

        // Disable Smart Moving's climbing.
        if (!data.hasPermission(Permissions.SMARTMOVING_FLYING, player)){
            smartMoving += "§0§1§7§f§f";
        }

        if (smartMoving.isEmpty()){
            return message;
        }
        else{
            return message + smartMoving;
        }
    }

}
