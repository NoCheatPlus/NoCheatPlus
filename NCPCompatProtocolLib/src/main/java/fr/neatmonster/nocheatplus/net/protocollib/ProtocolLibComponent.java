package fr.neatmonster.nocheatplus.net.protocollib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.DisableListener;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Quick and dirty ProtocolLib setup.
 * @author dev1mc
 *
 */
public class ProtocolLibComponent implements DisableListener, INotifyReload{
	
	private final List<PacketAdapter> registeredPacketAdapters = new LinkedList<PacketAdapter>();
	
	public ProtocolLibComponent(Plugin plugin) {
		LogUtil.logInfo("Adding packet level hooks for ProtocolLib (MC " + ProtocolLibrary.getProtocolManager().getMinecraftVersion().getVersion() + ")...");
		register(plugin);
	}

	private void register(Plugin plugin) {	
		// REgister Classes having a constructor with Plugin as argument.
		// TODO: Config paths for activation flags ...
		// TODO: @GlobalConfig simple setup at first.
		if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_FLYINGFREQUENCY_ACTIVE)) {
			register(FlyingFrequency.class, plugin);
		}
		if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_SOUNDDISTANCE_ACTIVE)) {
			register(SoundDistance.class, plugin);
		}
		if (!registeredPacketAdapters.isEmpty()) {
			List<String> names = new ArrayList<String>(registeredPacketAdapters.size());
			for (PacketAdapter adapter : registeredPacketAdapters) {
				names.add(adapter.getClass().getSimpleName());
			}
			LogUtil.logInfo("[NoCheatPlus] Available (and activated) packet level hooks: " + StringUtil.join(names, " | "));
		}
	}
	
	private void register(Class<? extends PacketAdapter> clazz, Plugin plugin) {
		try {
			// Construct a new instance using reflection.
			PacketAdapter adapter = clazz.getDeclaredConstructor(Plugin.class).newInstance(plugin);
			ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
			registeredPacketAdapters.add(adapter);
		} catch (Throwable t) {
			LogUtil.logWarning("[NoCheatPlus] Could not register packet level hook: " + clazz.getSimpleName());
			LogUtil.logWarning(t);
		}
	}

	@Override
	public void onDisable() {
		unregister();
	}

	@Override
	public void onReload() {
		unregister();
		register(Bukkit.getPluginManager().getPlugin("NoCheatPlus")); // Store instead ?
	}
	
	private void unregister() {
		final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		final NoCheatPlusAPI api = NCPAPIProvider.getNoCheatPlusAPI();
		for (PacketAdapter adapter : registeredPacketAdapters) {
			try {
				protocolManager.removePacketListener(adapter);
				api.removeComponent(adapter); // Bit heavy, but consistent.
			} catch (Throwable t) {
				LogUtil.logWarning("[NoCheatPlus] Failed to unregister packet level hook: " + adapter.getClass().getName());
			}
		}
		registeredPacketAdapters.clear();
	}

}
