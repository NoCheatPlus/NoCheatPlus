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
import fr.neatmonster.nocheatplus.players.PlayerData;

public class CJBMOTD extends ClientMOTD {

    @Override
    public String onPlayerJoin(final String message, final Player player, final PlayerData data, 
            final boolean allowAll) {

        if (allowAll){
            return message;
        }

        String cjb = "";

        // TODO: Is there a compact version (just one prefix)?

        // TODO: Fly and xray removed ?

        // Disable CJB's fly mod.
        if (!data.hasPermission(Permissions.CJB_FLY, player)){
            cjb += "§3 §9 §2 §0 §0 §1";
        }

        // Disable CJB's xray.
        if (!data.hasPermission(Permissions.CJB_XRAY, player)){
            cjb += "§3 §9 §2 §0 §0 §2";
        }

        // Disable CJB's radar.
        if (!data.hasPermission(Permissions.CJB_RADAR, player)){
            cjb += "§3 §9 §2 §0 §0 §3";
        }

        if (cjb.isEmpty()){
            return message;
        }
        else{
            return message + cjb;
        }
    }

}
