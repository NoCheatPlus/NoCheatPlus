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
package fr.neatmonster.nocheatplus.utilities;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

// TODO: Auto-generated Javadoc
/**
 * Auxiliary/convenience methods for inventories.
 * @author mc_dev
 *
 */
public class InventoryUtil {

    /**
     * Collect non-block items by suffix of their Material name (case insensitive).
     * @param suffix
     * @return
     */
    public static List<Material> collectItemsBySuffix(String suffix) {
        suffix = suffix.toLowerCase();
        final List<Material> res = new LinkedList<Material>();
        for (final Material mat : Material.values()) {
            if (!mat.isBlock() && mat.name().toLowerCase().endsWith(suffix)) {
                res.add(mat);
            }
        }
        return res;
    }

    /**
     * Collect non-block items by suffix of their Material name (case insensitive).
     * @param prefix
     * @return
     */
    public static List<Material> collectItemsByPrefix(String prefix) {
        prefix = prefix.toLowerCase();
        final List<Material> res = new LinkedList<Material>();
        for (final Material mat : Material.values()) {
            if (!mat.isBlock() && mat.name().toLowerCase().startsWith(prefix)) {
                res.add(mat);
            }
        }
        return res;
    }

    /**
     * Does not account for special slots like armor.
     *
     * @param inventory
     *            the inventory
     * @return the free slots
     */
    public static int getFreeSlots(final Inventory inventory) {
        final ItemStack[] contents = inventory.getContents();
        int count = 0;
        for (int i = 0; i < contents.length; i++) {
            if (BlockProperties.isAir(contents[i])) {
                count ++;
            }
        }
        return count;
    }

    /**
     * Count slots with type-id and data (enchantments and other meta data are
     * ignored at present).
     *
     * @param inventory
     *            the inventory
     * @param reference
     *            the reference
     * @return the stack count
     */
    public static int getStackCount(final Inventory inventory, final ItemStack reference) {
        if (inventory == null) return 0;
        if (reference == null) return getFreeSlots(inventory);
        final Material mat = reference.getType();
        final int durability = reference.getDurability();
        final ItemStack[] contents = inventory.getContents();
        int count = 0;
        for (int i = 0; i < contents.length; i++) {
            final ItemStack stack = contents[i];
            if (stack == null) {
                continue;
            }
            else if (stack.getType() == mat && stack.getDurability() == durability) {
                count ++;
            }
        }
        return count;
    }

    /**
     * Sum of bottom + top inventory slots with item type / data, see:
     * getStackCount(Inventory, reference).
     *
     * @param view
     *            the view
     * @param reference
     *            the reference
     * @return the stack count
     */
    public static int getStackCount(final InventoryView view, final ItemStack reference) {
        return getStackCount(view.getBottomInventory(), reference) + getStackCount(view.getTopInventory(), reference);
    }

    //    /**
    //     * Search for players / passengers (broken by name: closes the inventory of
    //     * first player found including entity and passengers recursively).
    //     *
    //     * @param entity
    //     *            the entity
    //     * @return true, if successful
    //     */
    //    public static boolean closePlayerInventoryRecursively(Entity entity) {
    //        // Find a player.
    //        final Player player = PassengerUtil.getFirstPlayerIncludingPassengersRecursively(entity);
    //        if (player != null && closeOpenInventory((Player) entity)) {
    //            return true;
    //        } else {
    //            return false;
    //        }
    //    }

    /**
     * Close one players inventory, if open. This might ignore
     * InventoryType.CRAFTING (see: hasInventoryOpen).
     *
     * @param player
     *            the player
     * @return If closed.
     */
    public static boolean closeOpenInventory(final Player player) {
        if (hasInventoryOpen(player)) {
            player.closeInventory();
            return true;
        } else {
            return true;
        }
    }

    /**
     * Check if the players inventory is open. This might ignore
     * InventoryType.CRAFTING.
     *
     * @param player
     *            the player
     * @return true, if successful
     */
    public static boolean hasInventoryOpen(final Player player) {
        final InventoryView view = player.getOpenInventory();
        return view != null && view.getType() != InventoryType.CRAFTING;
    }

    /**
     * Return the first consumable item found, checking main hand first and then
     * off hand, if available. Concerns food/edible, potions, milk bucket.
     *
     * @param player
     *            the player
     * @return null in case no item is consumable.
     */
    public static ItemStack getFirstConsumableItemInHand(final Player player) {
        ItemStack actualStack = Bridge1_9.getItemInMainHand(player);
        if (
                Bridge1_9.hasGetItemInOffHand()
                && (actualStack == null || !InventoryUtil.isConsumable(actualStack.getType()))
                ) {
            // Assume this to make sense.
            actualStack = Bridge1_9.getItemInOffHand(player);
            if (actualStack == null || !InventoryUtil.isConsumable(actualStack.getType())) {
                actualStack = null;
            }
        }
        return actualStack;
    }

    /**
     * Test if the item is consumable, like food, potions, milk bucket.
     *
     * @param stack
     *            May be null.
     * @return true, if is consumable
     */
    public static boolean isConsumable(final ItemStack stack) {
        return stack == null ? false : isConsumable(stack.getType());
    }

    /**
     * Test if the item is consumable, like food, potions, milk bucket.
     *
     * @param type
     *            May be null.
     * @return true, if is consumable
     */
    public static boolean isConsumable(final Material type) {
        return type != null &&
                (type.isEdible() || type == Material.POTION || type == Material.MILK_BUCKET);
    }

    /**
     * Test for max durability, only makes sense with items that can be in
     * inventory once broken, such as elytra. This method does not (yet) provide
     * legacy support. This tests for ItemStack.getDurability() >=
     * Material.getMaxDurability, so it only is suited for a context where this
     * is what you want to check for.
     * 
     * @param stack
     *            May be null, would yield true.
     * @return
     */
    public static boolean isItemBroken(final ItemStack stack) {
        if (stack == null) {
            return true;
        }
        final Material mat = stack.getType();
        return stack.getDurability() >= mat.getMaxDurability();
    }

}
