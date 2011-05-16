package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.ConfigurationException;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.listeners.BogusitemsPlayerListener;

public class BogusitemsCheck extends Check {

	public BogusitemsCheck(NoCheat plugin, NoCheatConfiguration config){
		super(plugin, "bogusitems", PermissionData.PERMISSION_BOGUSITEMS, config);
	}

	public void check(PlayerPickupItemEvent event) {

		// Should we check at all?
		if(skipCheck(event.getPlayer())) return;

		Item i = event.getItem();
		if(i != null) {
			ItemStack s = i.getItemStack();
			if(s != null) {
				if(s.getAmount() <= 0) {// buggy item
					event.getItem().remove();
					event.setCancelled(true);
					plugin.log(Level.WARNING, event.getPlayer().getName() + " tried to pick up an invalid item. Item was removed.");

					cleanPlayerInventory(event.getPlayer());
				}
			}
		}
	}

	public void check(PlayerInteractEvent event) {

		if(skipCheck(event.getPlayer())) return;

		if(event.hasItem() && event.getItem().getAmount() <= 0) {// buggy item
			event.setCancelled(true);
			plugin.log(Level.WARNING, event.getPlayer().getName() + " tried to use an invalid item. Item was removed.");
			event.getPlayer().getInventory().remove(event.getItem());

			cleanPlayerInventory(event.getPlayer());
		}
	}

	public void check(PlayerDropItemEvent event) {

		if(skipCheck(event.getPlayer())) return;

		Item item = event.getItemDrop();

		if(item.getItemStack() != null) {
			ItemStack stack = item.getItemStack();

			if(stack.getAmount() <= 0) {
				plugin.log(Level.WARNING, event.getPlayer().getName() + " tried to drop an invalid item. Dropped item was changed to dirt.");
				stack.setTypeId(3); // dirt
				stack.setAmount(1);

				cleanPlayerInventory(event.getPlayer());
			}
		}
	}

	private void cleanPlayerInventory(Player player) {

		Inventory inv = player.getInventory();

		ItemStack stacks[] = inv.getContents();

		for(int i = 0; i < stacks.length; i++) {
			if(stacks[i] != null && stacks[i].getAmount() <= 0) {
				inv.clear(i);
				plugin.log(Level.WARNING, "Removed invalid item from inventory of " + player.getName());
			}
		}
	}

	@Override
	public void configure(NoCheatConfiguration config) {

		try {
			setActive(config.getBooleanValue("active.bogusitems"));
		} catch (ConfigurationException e) {
			setActive(false);
			e.printStackTrace();
		}
	}

	@Override
	protected void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		// Register listeners for itemdupe check
		Listener bogusitemsPlayerListener = new BogusitemsPlayerListener(this);

		// Register listeners for itemdupe check
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, bogusitemsPlayerListener, Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, bogusitemsPlayerListener, Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, bogusitemsPlayerListener, Priority.Lowest, plugin);

	}
}
