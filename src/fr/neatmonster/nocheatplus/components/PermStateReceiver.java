package fr.neatmonster.nocheatplus.components;



/**
 * Receive permission changes for certain permissions.
 * @author mc_dev
 *
 */
public interface PermStateReceiver extends PermStateHolder{
	
	public void setPermission(String player, String permission, boolean state);
	
	public void removePlayer(String player);
	
}
