package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;

/**
 * Auxiliary/convenience methods for inventories.
 * @author mc_dev
 *
 */
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
	 * Count slots with type-id and data (enchantments and other meta data are ignored at present).
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

	/**
	 * Test if global config has the flag set.
	 * @return
	 */
	public static boolean shouldEnsureCloseInventories(){
		return ConfigManager.getConfigFile().getBoolean(ConfPaths.INVENTORY_ENSURECLOSE, true);
	}

	/**
	 * Search for players / passengers.
	 * @param entity
	 */
	public static void closePlayerInventoryRecursively(Entity entity){
		// Find a player.
		while (entity != null){
			if (entity instanceof Player){
				closeOpenInventory((Player) entity);
			}
			final Entity passenger = entity.getPassenger();
			if (entity.equals(passenger)){
				// Just in case :9.
				break;
			}
			else{
				entity = passenger;
			}
		}
	}

	/**
	 * Close one players inventory, if open. This ignores InventoryType.CRAFTING.
	 * @param player
	 */
	public static void closeOpenInventory(final Player player){
		final InventoryView view = player.getOpenInventory();
		if (view != null && view.getType() != InventoryType.CRAFTING) {
				player.closeInventory();
		}
	}

}
