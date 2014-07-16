package fr.neatmonster.nocheatplus.net.protocollib;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;

import fr.neatmonster.nocheatplus.components.DisableListener;
import fr.neatmonster.nocheatplus.logging.LogUtil;

/**
 * Quick and dirty ProtocolLib setup.
 * @author dev1mc
 *
 */
public class ProtocolLibComponent implements DisableListener{
	
	private final List<PacketAdapter> registeredPacketAdapters = new LinkedList<PacketAdapter>();
	
	public ProtocolLibComponent(Plugin plugin) {
		// Register with ProtocolLib
		final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		LogUtil.logInfo("[NoCheatPlus] ProtocolLib seems to be available.");
		// Classes having a constructor with Plugin as argument.
		List<Class<? extends PacketAdapter>> adapterClasses = Arrays.asList(
			MoveFrequency.class,
			WeatherDistance.class
			);
		// TODO: Configurability.
		for (Class<? extends PacketAdapter> clazz : adapterClasses) {
			try {
				// Construct a new instance using reflection.
				PacketAdapter adapter = clazz.getDeclaredConstructor(Plugin.class).newInstance(plugin);
				protocolManager.addPacketListener(adapter);
				registeredPacketAdapters.add(adapter);
			} catch (Throwable t) {
				LogUtil.logWarning("[NoCheatPlus] Could not register packet level hook: " + clazz.getSimpleName());
				LogUtil.logWarning(t); // TODO: Maybe temporary.
			}
		}
	}

	@Override
	public void onDisable() {
		final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		for (PacketAdapter adapter : registeredPacketAdapters) {
			try {
				protocolManager.removePacketListener(adapter);
			} catch (Throwable t) {
				LogUtil.logWarning("[NoCheatPlus] Failed to unregister protocol listener: " + adapter.getClass().getName());
			}
		}
	}
	
	
	
	

}
