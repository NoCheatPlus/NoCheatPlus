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

import java.util.HashSet;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.players.IPlayerData;

public class Items extends Check{

    private static Items instance = null;

    public Items() {
        super(CheckType.INVENTORY_ITEMS);
        instance = this;
    }

    /**
     * Checks for illegal enchantments (legacy). Removes enchantments from
     * WRITTEN_BOOK. Check both main and off hand.
     * 
     * @param player
     * @return True if the check is failed.
     */
    public static final boolean checkIllegalEnchantmentsAllHands(final Player player,
            final IPlayerData pData) {
        boolean result = false;
        if (checkIllegalEnchantments(player, Bridge1_9.getItemInMainHand(player), pData)) {
            result = true;
        }
        if (Bridge1_9.hasGetItemInOffHand() 
                && checkIllegalEnchantments(player, Bridge1_9.getItemInOffHand(player), pData)) {
            result = true;
        }
        return result;
    }

    /**
     * Checks for illegal enchantments (legacy). Removes enchantments from
     * WRITTEN_BOOK.
     * 
     * @param player
     * @param stack
     * @return True if the check is failed.
     */
    public static final boolean checkIllegalEnchantments(final Player player, 
            final ItemStack stack, final IPlayerData pData){
        if (stack == null) {
            return false;
        }
        final Material type = stack.getType();
        // Fastest checks first.
        // TODO: Make stuff configurable.
        if (type == Material.WRITTEN_BOOK){
            final Map<Enchantment, Integer> enchantments = stack.getEnchantments();
            if (enchantments != null && !enchantments.isEmpty() && pData.isCheckActive(instance.type, player)){
                // TODO: differentiate sub checks maybe or add extra permissions, later.
                for (final Enchantment ench : new HashSet<Enchantment>(enchantments.keySet())){
                    stack.removeEnchantment(ench);
                }
                // TODO: actions and similar.
                return true;
            }
        }
        return false;
    }

}
