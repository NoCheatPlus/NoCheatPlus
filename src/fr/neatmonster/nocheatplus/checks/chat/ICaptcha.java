package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

/**
 * Captcha related operations.<br>
 * Auxiliary interface, most methods should need sync over data, unless stated otherwise.
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

	/**
     * Just resets tries, generate new captcha if necessary.
     * @param cc
     * @param data
     */
	public void resetCaptcha(ChatConfig cc, ChatData data);
	
	/**
	 * Convenience method. Should synchronize over data of player (!).
	 * @param player
	 */
	public void resetCaptcha(Player player);

	/**
	 * Generate a captcha.
	 * @param cc
	 * @param data
	 * @param reset If to reset tries.
	 */
	public void generateCaptcha(ChatConfig cc, ChatData data, boolean reset);
}
