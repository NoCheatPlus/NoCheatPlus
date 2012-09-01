package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

/**
 * Captcha related operations.<br>
 * Auxiliary interface to avoid creating a new check type.
 * @author mc_dev
 *
 */
public interface ICaptcha {
	
	/**
	 * Check if the captcha has been entered correctly. 
	 * Reset if correct, otherwise increase tries and execute actions if necessary.
	 * @param player
	 * @param message
	 * @param cc
	 * @param data
	 * @param isMainThread
	 */
	public void checkCaptcha(Player player, String message, ChatConfig cc, ChatData data, boolean isMainThread);
	
	/**
	 * Just send the current captcha to the player.
	 * @param player
	 * @param cc
	 * @param data
	 */
	public void sendCaptcha(Player player, ChatConfig cc, ChatData data);
	
	/**
	 * Generate a new captcha and send to the player.
	 * @param player
	 * @param cc
	 * @param data
	 */
	public void sendNewCaptcha(Player player, ChatConfig cc, ChatData data);
	
	/**
	 * Check if checkCaptcha should be called.
	 * @param cc
	 * @param data
	 * @return
	 */
	public boolean shouldCheckCaptcha(ChatConfig cc, ChatData data);
	
	/**
	 * Check if captcha should be generated and send to the player.
	 * @param cc
	 * @param data
	 * @return
	 */
	public boolean shouldStartCaptcha(ChatConfig cc, ChatData data);

}
