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

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

// TODO: Auto-generated Javadoc
/**
 * Auxiliary/convenience methods for inventories.
 * @author mc_dev
 *
 */
public class InventoryUtil {
	
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
	
	/**
     * Search for players / passengers (broken by name: closes the inventory of
     * first player found including entity and passengers recursively).
     *
     * @param entity
     *            the entity
     * @return true, if successful
     */
	public static boolean closePlayerInventoryRecursively(Entity entity) {
		// Find a player.
		final Player player = getPlayerPassengerRecursively(entity);
		if (player != null && closeOpenInventory((Player) entity)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
     * Get a player from an entity. This will return the first player found
     * amongst the entity itself and passengers checked recursively.
     *
     * @param entity
     *            the entity
     * @return the player passenger recursively
     */
	public static Player getPlayerPassengerRecursively(Entity entity) {
		while (entity != null) {
			if (entity instanceof Player) {
				// Scrap the case of players riding players for the moment.
				return (Player) entity;
			}
			final Entity passenger = entity.getPassenger();
			if (entity.equals(passenger)) {
				// Just in case :9.
				break;
			}
			else {
				entity = passenger;
			}
		}
		return null;
	}

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

}
