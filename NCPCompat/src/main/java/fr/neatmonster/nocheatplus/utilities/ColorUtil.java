package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.ChatColor;

/**
 * Desperate :).
 * @author mc_dev
 *
 */
public class ColorUtil {

	/**
	 * Removes the colors of a message.
	 * 
	 * @param text
	 *            the text
	 * @return the string
	 */
	public static String removeColors(String text) {
	    for (final ChatColor c : ChatColor.values())
	        text = text.replace("&" + c.getChar(), "");
	    return text;
	}

	/**
	 * Replace colors of a message.
	 * 
	 * @param text
	 *            the text
	 * @return the string
	 */
	public static String replaceColors(String text) {
	    for (final ChatColor c : ChatColor.values())
	        text = text.replace("&" + c.getChar(), c.toString());
	    return text;
	}

}
