package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {
	
	/**
	 * Does not account for special slots like armor.
	 * @param inventory
	 * @return
	 */
	public static int getFreeSlots(final Inventory inventory){
		final ItemStack[] contents = inventory.getContents();
		int count = 0;
		for (int i = 0; i < contents.length; i++){
			final ItemStack stack = contents[i];
			if (stack == null || stack.getTypeId() == 0){
				count ++;
			}
		}
		return count;
	}

	/**
	 * Count slots with type-id and data (enchantments are ignored at present).
	 * @param inventory
	 * @param reference
	 * @return
	 */
	public static int getStackCount(final Inventory inventory, final ItemStack reference) {
		if (inventory == null) return 0;
		if (reference == null) return getFreeSlots(inventory);
		final int id = reference.getTypeId();
		final int durability = reference.getDurability();
		final ItemStack[] contents = inventory.getContents();
		int count = 0;
		for (int i = 0; i < contents.length; i++){
			final ItemStack stack = contents[i];
			if (stack == null){
				continue;
			}
			else if (stack.getTypeId() == id && stack.getDurability() == durability){
				count ++;
			}
		}
		return count;
	}

	/**
	 * Sum of bottom + top inventory slots with item type / data, see: getStackCount(Inventory, reference).
	 * @param view
	 * @param reference
	 * @return
	 */
	public static int getStackCount(final InventoryView view, final ItemStack reference) {
		return getStackCount(view.getBottomInventory(), reference) + getStackCount(view.getTopInventory(), reference);
	}

}
