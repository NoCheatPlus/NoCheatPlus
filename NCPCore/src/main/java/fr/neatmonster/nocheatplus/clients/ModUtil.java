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
package fr.neatmonster.nocheatplus.clients;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.clients.motd.CJBMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ClientMOTD;
import fr.neatmonster.nocheatplus.clients.motd.JourneyMapMOTD;
import fr.neatmonster.nocheatplus.clients.motd.MCAutoMapMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ReiMOTD;
import fr.neatmonster.nocheatplus.clients.motd.SmartMovingMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ZombeMOTD;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * Utilities for dealing with client mods. This is likely to by just a refactoring stage.
 * @author mc_dev
 *
 */
public class ModUtil {

    // TODO: Consider to register individual codes in a generic way, detect conflicts (+tests?).
    private static final ClientMOTD[] motdS = new ClientMOTD[]{
            new ReiMOTD(),
            new ZombeMOTD(),
            new SmartMovingMOTD(),
            new CJBMOTD(),
            new MCAutoMapMOTD(),
            new JourneyMapMOTD()
    };

    /**
     * Send block codes to the player according to allowed or disallowed client-mods or client-mod features.
     * @param player
     */
    public static void motdOnJoin(final Player player) {
        final ConfigFile config = ConfigManager.getConfigFile(); 
        if (!config.getBoolean(ConfPaths.PROTECT_CLIENTS_MOTD_ACTIVE)){
            // No message is to be sent.
            return;
        }
        // TODO: Somebody test this all !
        // TODO: add feature to check world specific (!).

        // Check if we allow all the client mods.
        final boolean allowAll = config.getBoolean(ConfPaths.PROTECT_CLIENTS_MOTD_ALLOWALL);

        String message = "";
        final IPlayerData data = DataManager.getPlayerData(player);
        for (int i = 0; i < motdS.length; i++){
            message = motdS[i].onPlayerJoin(message, player, data, allowAll);
        }

        if (!message.isEmpty()){
            player.sendMessage(message);
        }
    }

}
