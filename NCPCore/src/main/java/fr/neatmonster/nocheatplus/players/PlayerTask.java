package fr.neatmonster.nocheatplus.players;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.utilities.OnDemandTickListener;

/**
 * Player specific task.
 * @author mc_dev
 *
 */
public class PlayerTask extends OnDemandTickListener {
	
	public final String lcName;
	
	protected boolean updateInventory = false;
	
	/** Messages scheduled for sending. */
	protected final List<String> messages = new LinkedList<String>();

	/**
	 * 
	 * @param name Not demanded to be case sensitive.
	 */
	public PlayerTask(final String name){
		this.lcName = name.toLowerCase();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean delegateTick(final int tick, final long timeLast) {
		final Player player = DataManager.getPlayer(lcName);
		if (player != null){
			if (player.isOnline()){
				if (updateInventory){
					player.updateInventory();
					updateInventory = false;
				}
				if (!messages.isEmpty()){
					final String[] message = new String[messages.size()];
					messages.toArray(message);
					player.sendMessage(message);
				}
			}
		}
		// Cleanup.
		if (!messages.isEmpty()){
			messages.clear();
		}
		// TODO: Consider setting updateInventory to false here.
		// No re-scheduling (run once each time).
		return false;
	}
	
	/**
	 * Add a message to be sent once the task is running. This method is NOT thread-safe.
	 * @param message
	 */
	public void sendMessage(String message){
		messages.add(message);
		register();
	}
	
	public void updateInventory(){
		// TODO: Might not allow registering every tick.
		updateInventory = true;
		register();
	}
	
	// TODO: updateHunger

}
