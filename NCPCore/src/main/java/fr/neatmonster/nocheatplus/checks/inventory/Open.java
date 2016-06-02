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

import java.util.UUID;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.DisableListener;
import fr.neatmonster.nocheatplus.utilities.InventoryUtil;

/**
 * Watch over open inventories - check with "combined" static access, put here because it has too much to do with inventories.
 * @author mc_dev
 *
 */
public class Open extends Check implements DisableListener{

    private static Open instance = null;

    private UUID nestedPlayer = null;

    // TODO: Add specific contexts (allow different settings for fight / blockbreak etc.).

    /**
     * Static access check, if there is a cancel-action flag the caller should have stored that locally already and use the result to know if to cancel or not.
     * @param player
     * @return If cancelling some event is opportune (open inventory and cancel flag set).
     */
    public static boolean checkClose(Player player) {
        return instance.check(player);
    }

    public Open() {
        super(CheckType.INVENTORY_OPEN);
        instance = this;
    }

    @Override
    public void onDisable() {
        instance = null;
        nestedPlayer = null;
    }

    /**
     * This check contains the isEnabled checking (!). Inventory is closed if set in the config.
     * @param player
     * @return If cancelling some event is opportune (open inventory and cancel flag set).
     */
    public boolean check(final Player player) {
        if (!isEnabled(player) || !InventoryUtil.hasInventoryOpen(player)) {
            return false;
        }
        final InventoryConfig cc = InventoryConfig.getConfig(player);
        if (cc.openClose) {
            final UUID id = player.getUniqueId();
            if (this.nestedPlayer == null || !id.equals(this.nestedPlayer)) {
                // (The second condition represents an error, but we don't handle alternating things just yet.)
                this.nestedPlayer = id;
                player.closeInventory();
                this.nestedPlayer = null;
            }
        }
        return cc.openCancelOther;
    }

}
