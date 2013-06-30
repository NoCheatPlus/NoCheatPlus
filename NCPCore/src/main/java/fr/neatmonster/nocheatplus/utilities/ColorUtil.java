package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.ChatColor;

/**
 * More and less color.
 * @author mc_dev
 *
 */
public class ColorUtil {
	
	private static final String allColorChars = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";

	/**
	 * Removes the colors from a message (definitions with '&').
	 * 
	 * @param text
	 *            the text
	 * @return Either the object reference itself, or a copy with color definitions removed.
	 */
	public static String removeColors(final String text) {
		if (text.length() <= 1) return text;
		final char[] chars = text.toCharArray();
		// First find a begin index at all.
		// TODO: Consider removing the normal color char as well (!).
		int srcIndex = 0; // SourceIndex
		do{
			if (chars[srcIndex] == '&' && allColorChars.indexOf(chars[srcIndex + 1]) > -1){
				break;
			}
			srcIndex ++;
		} while (srcIndex < chars.length - 1);
		if (srcIndex >= chars.length - 1){
			// Nothing found
			return text;
		}
		// At least one color inside.
		char[] newChars = new char[chars.length - 2];
		int tgtIndex = 0; // TargetIndex.
		for(tgtIndex = 0; tgtIndex < srcIndex; tgtIndex++){
			newChars[tgtIndex] = chars[tgtIndex];
		};
		for (srcIndex = srcIndex + 2; srcIndex < chars.length; srcIndex++){
			if (chars[srcIndex] == '&' && srcIndex < chars.length -1 && allColorChars.indexOf(chars[srcIndex + 1]) > -1){
				// Skip this one;
				srcIndex ++;
			}
			else{
				newChars[tgtIndex] = chars[srcIndex];
				tgtIndex++;
			}
		}
		return new String(newChars, 0, tgtIndex);
	}

	/**
	 * Replace color definitions with '&' by ChatColor for the given text.
	 * 
	 * @param text
	 *            the text
	 * @return the string
	 */
	public static String replaceColors(final String text) {
	    return ChatColor.translateAlternateColorCodes('&', text);
	}

}
