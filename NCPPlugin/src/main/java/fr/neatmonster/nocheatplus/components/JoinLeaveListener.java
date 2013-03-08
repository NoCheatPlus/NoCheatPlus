package fr.neatmonster.nocheatplus.components;

import org.bukkit.entity.Player;

/**
 * Receive calls for players joining and leaving (quit/kick).
 * @author mc_dev
 *
 */
public interface JoinLeaveListener {
	
	/**
	 * Called on join (monitor level).
	 * @param player
	 */
	public void playerJoins(final Player player);
	
	/**
	 * Called both on quit/kick (monitor level).
	 * @param player
	 */
	public void playerLeaves(final Player player);
}
