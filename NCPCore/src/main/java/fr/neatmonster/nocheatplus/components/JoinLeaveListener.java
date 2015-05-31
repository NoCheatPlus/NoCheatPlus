package fr.neatmonster.nocheatplus.components;

import org.bukkit.entity.Player;

/**
 * Receive calls for players joining and leaving (quit/kick).
 * @author mc_dev
 *
 */
public interface JoinLeaveListener {
	
	/**
	 * Called on join (priority level: low).
	 * @param player
	 */
	public void playerJoins(final Player player);
	
	/**
	 * Called both on quit/kick (priority level: monitor). Might get called twice on some server implementations.
	 * @param player
	 */
	public void playerLeaves(final Player player);
}
