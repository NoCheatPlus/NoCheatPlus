package fr.neatmonster.nocheatplus.net.protocollib;

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
		try {
			PacketAdapter adapter = new MoveFrequency(plugin);
			protocolManager.addPacketListener(adapter); 
			registeredPacketAdapters.add(adapter);
			LogUtil.logWarning("[NoCheatPlus] Registered some packet-level hook.");
		} catch (Throwable t) {
			LogUtil.logWarning("[NoCheatPlus] Could not register some packet-level hook.");
			LogUtil.logWarning(t); // TODO: Maybe temporary.
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
