package fr.neatmonster.nocheatplus.players;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.utilities.OnDemandTickListener;

/**
 * Unregisters during delegateTick to achieve thread-safety.
 * @author mc_dev
 *
 */
public class PlayerMessageSender extends OnDemandTickListener {
	
	private final class MessageEntry{
		public final String playerName;
		public final String message;
		public MessageEntry(final String playerName, final String message){
			this.playerName = playerName;
			this.message = message;
		}
	}
	
	/** Queued entries, also used as lock. */
	private final List<MessageEntry> messageEntries = new LinkedList<MessageEntry>();

	@Override
	public boolean delegateTick(int tick, long timeLast) {
		// Copy entries.
		final MessageEntry[] entries;
		synchronized (messageEntries) {
			entries = new MessageEntry[messageEntries.size()];
			messageEntries.toArray(entries);
			messageEntries.clear();
		}
		// Do messaging.
		for (int i = 0; i < entries.length; i++){
			final MessageEntry entry = entries[i];
			final Player player = DataManager.getPlayerExact(entry.playerName);
			if (player != null && player.isOnline()){
				player.sendMessage(entry.message);
			}
		}
		// Unregister if no further entries are there.
		synchronized (messageEntries) {
			if (messageEntries.isEmpty()){
				// Force unregister.
				unRegister(true);
			}
		}
		// Always continue here to never use external setRegistered.
		return true;
	}
	
	public void sendMessageThreadSafe(final String playerName, final String message){
		final MessageEntry entry = new MessageEntry(playerName.toLowerCase(), message);
		synchronized (messageEntries) {
			messageEntries.add(entry);
			// Called register asynchronously, potentially.
			register();
		}
	}

}
