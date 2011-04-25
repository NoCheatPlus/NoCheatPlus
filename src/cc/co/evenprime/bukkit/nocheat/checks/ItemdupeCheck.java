package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.logging.Level;

import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import cc.co.evenprime.bukkit.nocheat.NoCheat;

public class ItemdupeCheck extends Check {
	
	public ItemdupeCheck(NoCheat plugin){
		super(plugin);
		this.setActive(false);
	}
	
	public void check(PlayerPickupItemEvent event) {
		
		Item i = event.getItem();
		if(i != null) {
			ItemStack s = i.getItemStack();
			if(s != null) {
				if(s.getAmount() <= 0) {// buggy item
					event.getItem().remove();
					event.setCancelled(true);
					plugin.log(Level.WARNING, event.getPlayer().getName() + " tried to pick up an invalid item. Item will be removed now.");
				}
			}
		}
	}
	
	public void check(PlayerInteractEvent event) {
		if(event.hasItem() && event.getItem().getAmount() <= 0) {// buggy item
			event.setCancelled(true);
			plugin.log(Level.WARNING, event.getPlayer().getName() + " tried to use an invalid item. Item will be removed now.");
			event.getPlayer().getInventory().remove(event.getItem());
		}
	}

	@Override
	public String getName() {
		
		return "itemdupe";
	}

}
