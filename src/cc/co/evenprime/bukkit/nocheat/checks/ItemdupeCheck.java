package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.listeners.ItemdupePlayerListener;

public class ItemdupeCheck extends Check {
	
	public ItemdupeCheck(NoCheat plugin){
		super(plugin, "itemdupe", NoCheatData.PERMISSION_ITEMDUPE);
	}
	
	public void check(PlayerPickupItemEvent event) {
		
		// Should we check at all?
		if(hasPermission(event.getPlayer())) return;
		
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
		
		if(plugin.hasPermission(event.getPlayer(), NoCheatData.PERMISSION_ITEMDUPE)) 
			return;
		
		if(event.hasItem() && event.getItem().getAmount() <= 0) {// buggy item
			event.setCancelled(true);
			plugin.log(Level.WARNING, event.getPlayer().getName() + " tried to use an invalid item. Item will be removed now.");
			event.getPlayer().getInventory().remove(event.getItem());
		}
	}

	@Override
	protected void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		
		// Register listeners for itemdupe check
		Listener itemdupePlayerListener = new ItemdupePlayerListener(this);
		
		// Register listeners for itemdupe check
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, itemdupePlayerListener, Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, itemdupePlayerListener, Priority.Lowest, plugin);
		
		
	}
}
