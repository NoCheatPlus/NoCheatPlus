package fr.neatmonster.nocheatplus.players;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.utilities.OnDemandTickListener;

/**
 * Player specific task.
 * @author mc_dev
 *
 */
public class PlayerTask extends OnDemandTickListener {
	
	// TODO: Consider overriding some logic, because it is used in the main thread only (context: isRegisterd + register).
	
	public final String lcName;
	
	protected boolean updateInventory = false;
	
//	protected boolean correctDirection = false;

	/**
	 * 
	 * @param name Not demanded to be case sensitive.
	 */
	public PlayerTask(final String name) {
		this.lcName = name.toLowerCase();
	}

	
	@SuppressWarnings("deprecation")
	@Override
	public boolean delegateTick(final int tick, final long timeLast) {
		final Player player = DataManager.getPlayer(lcName);
		if (player != null) {
			if (player.isOnline()) {
//				if (correctDirection) {
//					final MCAccess access = NCPAPIProvider.getNoCheatPlusAPI().getMCAccess();
//					access.correctDirection(player);
//				}
				if (updateInventory) {
					player.updateInventory();
				}
			}
		}
		// Reset values (players logging back in should be fine or handled differently).
		updateInventory = false;
//		correctDirection = false;
		return false;
	}
	
	public void updateInventory() {
		// TODO: Might not allow registering every tick.
		updateInventory = true;
		register();
	}
	
//	public void correctDirection() {
//		correctDirection = true;
//		register();
//	}
	
	// TODO: updateHunger

}
