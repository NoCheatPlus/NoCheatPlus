package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.ConfigurationException;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.listeners.ItemdupeEntityListener;

public class ItemdupeCheck extends Check {

	public ItemdupeCheck(NoCheat plugin, NoCheatConfiguration config){
		super(plugin, "itemdupe", PermissionData.PERMISSION_ITEMDUPE, config);
	}


	public void check(EntityDeathEvent event) {

		if(event.getEntity() instanceof CraftPlayer) {
			if(skipCheck((CraftPlayer)event.getEntity())) return;

			((CraftPlayer)event.getEntity()).getHandle().x(); // close all inventory screens
		}
	}

	@Override
	public void configure(NoCheatConfiguration config) {

		try {
			setActive(config.getBooleanValue("active.itemdupe"));
		} catch (ConfigurationException e) {
			setActive(false);
			e.printStackTrace();
		}
	}

	@Override
	protected void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		// Register listeners for itemdupe check
		Listener itemdupePlayerListener = new ItemdupeEntityListener(this);

		// Register listeners for itemdupe check
		pm.registerEvent(Event.Type.ENTITY_DEATH, itemdupePlayerListener, Priority.Lowest, plugin);

	}
}
