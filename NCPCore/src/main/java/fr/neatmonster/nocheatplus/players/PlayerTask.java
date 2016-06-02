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
package fr.neatmonster.nocheatplus.players;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.utilities.OnDemandTickListener;

/**
 * Player specific task.
 * @author mc_dev
 *
 */
public class PlayerTask extends OnDemandTickListener {

    // TODO: Consider overriding some logic, because it is used in the main thread only (context: isRegisterd + register).

    public final String lcName;

    protected boolean updateInventory = false;

    //	protected boolean correctDirection = false;

    /**
     * 
     * @param name Not demanded to be case sensitive.
     */
    public PlayerTask(final String name) {
        this.lcName = name.toLowerCase();
    }


    @SuppressWarnings("deprecation")
    @Override
    public boolean delegateTick(final int tick, final long timeLast) {
        final Player player = DataManager.getPlayer(lcName);
        if (player != null) {
            if (player.isOnline()) {
                //				if (correctDirection) {
                //					final MCAccess access = NCPAPIProvider.getNoCheatPlusAPI().getMCAccess();
                //					access.correctDirection(player);
                //				}
                if (updateInventory) {
                    player.updateInventory();
                }
            }
        }
        // Reset values (players logging back in should be fine or handled differently).
        updateInventory = false;
        //		correctDirection = false;
        return false;
    }

    public void updateInventory() {
        // TODO: Might not allow registering every tick.
        updateInventory = true;
        register();
    }

    //	public void correctDirection() {
    //		correctDirection = true;
    //		register();
    //	}

    // TODO: updateHunger

}
