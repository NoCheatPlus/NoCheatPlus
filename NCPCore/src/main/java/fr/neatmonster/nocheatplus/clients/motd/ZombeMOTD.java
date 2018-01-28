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

public class ZombeMOTD extends ClientMOTD {

    @Override
    public String onPlayerJoin(final String message, final Player player, final PlayerData data, 
            final boolean allowAll) {

        String zombe = "";

        // TODO: Is there a compact version (just one prefix)?

        // Disable Zombe's noclip.
        if (allowAll || data.hasPermission(Permissions.ZOMBE_NOCLIP, player)){
            zombe += "§f §f §4 §0 §9 §6";
        }

        if (!allowAll){
            // Disable Zombe's fly mod.
            if (!data.hasPermission(Permissions.ZOMBE_FLY, player)){
                zombe += "§f §f §1 §0 §2 §4";
            }

            // Disable Zombe's cheat.
            if (!data.hasPermission(Permissions.ZOMBE_CHEAT, player)){
                zombe += "§f §f §2 §0 §4 §8";
            }
        }

        if (zombe.isEmpty()){
            return message;
        }
        else{
            return message + zombe;
        }
    }

}
