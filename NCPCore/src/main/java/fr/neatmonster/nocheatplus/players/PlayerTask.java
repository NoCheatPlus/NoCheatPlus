package fr.neatmonster.nocheatplus.players;

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
			}
		}
		return false;
	}
	
	public void updateInventory(){
		// TODO: Might not allow registering every tick.
		updateInventory = true;
		register();
	}
	
	// TODO: updateHunger

}
