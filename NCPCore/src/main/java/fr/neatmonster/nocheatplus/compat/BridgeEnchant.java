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
package fr.neatmonster.nocheatplus.compat;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

public final class BridgeEnchant {

    private static final Enchantment parseEnchantment(final String name) {
        try {
            return Enchantment.getByName(name);
        } catch (Exception e) {
            return null;
        }
    }

    private final static Enchantment DEPTH_STRIDER = parseEnchantment("DEPTH_STRIDER");

    private final static Enchantment THORNS = parseEnchantment("THORNS");

    /**
     * Retrieve the maximum level for an enchantment, present in armor slots.
     * 
     * @param player
     * @param enchantment
     *            If null, 0 will be returned.
     * @return 0 if none found, or the maximum found.
     */
    private static int getMaxLevelArmor(final Player player, final Enchantment enchantment) {
        if (enchantment == null) {
            return 0;
        }
        int level = 0;
        // Find the maximum level for the given enchantment.
        final ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            final ItemStack item = armor[i];
            if (!BlockProperties.isAir(item)) {
                level = Math.max(item.getEnchantmentLevel(enchantment), level);
            }
        }
        return level;
    }

    /**
     * Test, if there is any armor with the given enchantment on.
     * 
     * @param player
     * @param enchantment
     *            If null, false will be returned.
     * @return
     */
    private static boolean hasArmor(final Player player, final Enchantment enchantment) {
        if (enchantment == null) {
            return false;
        }
        final PlayerInventory inv = player.getInventory();
        final ItemStack[] contents = inv.getArmorContents();
        for (int i = 0; i < contents.length; i++){
            final ItemStack stack = contents[i];
            if (stack != null && stack.getEnchantmentLevel(enchantment) > 0){
                return true;
            }
        }
        return false;
    }

    public static boolean hasThorns() {
        return THORNS != null;
    }

    public static boolean hasDepthStrider() {
        return DEPTH_STRIDER != null;
    }

    /**
     * Check if the player might return some damage due to the "thorns"
     * enchantment.
     * 
     * @param player
     * @return
     */
    public static boolean hasThorns(final Player player) {
        return hasArmor(player, THORNS);
    }

    /**
     * 
     * @param player
     * @return Maximum level of DEPTH_STRIDER found on armor items, capped at 3.
     *         Will return 0 if not available.
     */
    public static int getDepthStriderLevel(final Player player) {
        // Cap at three.
        return Math.min(3, getMaxLevelArmor(player, DEPTH_STRIDER));
    }

}
