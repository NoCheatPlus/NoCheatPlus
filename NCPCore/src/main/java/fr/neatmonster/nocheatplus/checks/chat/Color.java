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
package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * The Color check verifies that no color codes are sent in players' messages.
 */
public class Color extends Check {

    /**
     * Instantiates a new color check.
     */
    public Color() {
        super(CheckType.CHAT_COLOR);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param message
     *            the message
     * @param isMainThread
     *            is the thread the main thread
     * @return the string
     */
    public String check(final Player player, final String message, final boolean isMainThread) {

        final IPlayerData pData = DataManager.getPlayerData(player);
        final ChatConfig cc = pData.getGenericInstance(ChatConfig.class);
        final ChatData data = pData.getGenericInstance(ChatData.class);

        // Keep related to ChatData used lock.
        synchronized (data) {
            // If the message contains colors...
            if (message.contains("\247")) {
                // Increment the violation level of the player.
                data.colorVL++;

                // Find out if we need to remove the colors or not.
                if (executeActions(player, data.colorVL, 1D, cc.colorActions).willCancel())
                    // Remove color codes.
                    return message.replaceAll("\302\247.", "").replaceAll("\247.", "");
            }
        }

        return message;
    }

}
