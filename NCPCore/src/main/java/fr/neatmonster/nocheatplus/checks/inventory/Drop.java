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
package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * The Drop check will find out if a player drops too many items within a short amount of time.
 */
public class Drop extends Check {

    /**
     * Instantiates a new drop check.
     */
    public Drop() {
        super(CheckType.INVENTORY_DROP);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
        // Take time once.
        final long time = System.currentTimeMillis();

        final InventoryConfig cc = InventoryConfig.getConfig(player);
        final InventoryData data = InventoryData.getData(player);

        boolean cancel = false;

        // Has the configured time passed? If so, reset the counter.
        if (data.dropLastTime + cc.dropTimeFrame <= time) {
            data.dropLastTime = time;
            data.dropCount = 0;
            data.dropVL = 0D;
        }

        // Security check, if the system time changes.
        else if (data.dropLastTime > time)
            data.dropLastTime = Integer.MIN_VALUE;

        data.dropCount++;

        // The player dropped more than they should.
        if (data.dropCount > cc.dropLimit) {
            // Set their violation level.
            data.dropVL = data.dropCount - cc.dropLimit;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.dropVL, data.dropCount - cc.dropLimit, cc.dropActions).willCancel();
        }

        return cancel;
    }
}
