package fr.neatmonster.nocheatplus.checks;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.types.ActionList;

/**
 * Violation specific dataFactory, for executing actions.<br>
 * This is meant to capture a violation incident in a potentially thread safe way (!). 
 * @author mc_dev
 *
 */
public class ViolationData {
	
	public final Check check;
	public final Player player;
	public final double VL;
	public final ActionList actions;
	public final String bypassPermission;
	
	public ViolationData(final Check check, final Player player, final double VL, final ActionList actions){
		this(check, player, VL, actions, null);
	}
	
	/**
	 * 
	 * @param check
	 * @param player
	 * @param VL
	 * @param actions
	 * @param bypassPermission Permission to bypass the execution, if not null.
	 */
	public ViolationData(final Check check, final Player player, final double VL, final ActionList actions, final String bypassPermission){
		this.check = check;
		this.player = player;
		this.VL = VL;
		this.actions = actions;
		this.bypassPermission = bypassPermission;
	}
	
}
