package fr.neatmonster.nocheatplus.net.protocollib;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

/**
 * Prevent extremely fast ticking by just sending packets that don't do anything
 * new and also don't trigger moving events in CraftBukkit.
 * 
 * @author dev1mc
 *
 */
public class MoveFrequency extends PacketAdapter implements Listener, JoinLeaveListener {
	
	// TODO: Configuration.
	// TODO: Optimized options (receive only, other?).
	// TODO: Async version ?
	
	private Map<String, ActionFrequency> freqMap = new LinkedHashMap<String, ActionFrequency>();  
	
	public MoveFrequency(Plugin plugin) {
		// PacketPlayInFlying[3, legacy: 10]
		super(plugin, PacketType.Play.Client.FLYING); // TODO: How does POS and POS_LOOK relate/translate?
	}
	
	private ActionFrequency addName(String name) {
		Map<String, ActionFrequency> freqMap = new HashMap<String, ActionFrequency>(this.freqMap);
		ActionFrequency freq = new ActionFrequency(5, 1000);
		freqMap.put(name, freq);
		this.freqMap = freqMap;
		return freq;
	}
	
	private void removeName(String name) {
		Map<String, ActionFrequency> freq = new HashMap<String, ActionFrequency>(this.freqMap);
		freq.remove(name);
		this.freqMap = freq;
	}

	@Override
	public void playerJoins(Player player) {
		addName(player.getName()); // Could spare that one.
	}

	@Override
	public void playerLeaves(Player player) {
		removeName(player.getName());
	}
	
	private ActionFrequency getFreq(final String name) {
		final ActionFrequency freq = this.freqMap.get(name);
		if (freq == null) {
			return addName(name);
		} else {
			return freq;
		}
	}

	@Override
	public void onPacketReceiving(final PacketEvent event) {
		// TODO: Add several (at least has look + has pos individually, maybe none/onground)
		final ActionFrequency freq = getFreq(event.getPlayer().getName());
		freq.add(System.currentTimeMillis(), 1f);
		if (freq.score(1f) > 300) {
			event.setCancelled(true);
		}
	}
	
}
