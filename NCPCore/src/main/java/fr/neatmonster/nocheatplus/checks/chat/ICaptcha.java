/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
