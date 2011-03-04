package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;

/**
 * 
 * @author Evenprime
 *
 */
public class DupebydeathCheck {

	/**
	 * Explicitly remove all items that are going to be dropped from the players inventory
	 * @param event
	 */
	public static void playerDeath(EntityDeathEvent event) {

		if(event.getEntity() instanceof Player) {

			Player p = (Player)event.getEntity();

			// Should we prevent at all?
			if(NoCheatPlugin.Permissions != null && NoCheatPlugin.Permissions.has(p, "nocheat.dupebydeath")) {
				return;
			}
			else if(NoCheatPlugin.Permissions == null && p.isOp() ) {
				return;
			}

			PlayerInventory playerInventory = p.getInventory(); 
			List<ItemStack> drops = event.getDrops();

			// Go through the "to-be-dropped" items and delete the corresponding items from the players inventory
			for(ItemStack drop : drops) {
				for(int i = 0; i < playerInventory.getSize(); i++) {
					if(playerInventory.getItem(i).equals(drop)) {
						playerInventory.clear(i);
						break;
					}
				}
			}
		}
	}
}