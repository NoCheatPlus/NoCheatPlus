package cc.co.evenprime.bukkit.nocheat;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class DupePrevention {

	/**
	 * Explicitly remove all items that are going to be dropped from the players inventory
	 * @param event
	 */
	public static void playerDeath(EntityDeathEvent event) {
		
		if(event.getEntity() instanceof Player) {

			Player p = (Player)event.getEntity();

			PlayerInventory playerInventory = p.getInventory(); 
			List<ItemStack> drops = event.getDrops();

			for(ItemStack drop : drops) {
				for(int i = 0; i < playerInventory.getSize(); i++) {
					if(playerInventory.getItem(i).equals(drop)) {
						p.getInventory().clear(i);
						i = playerInventory.getSize();
					}
				}
			}
		}
	}
}