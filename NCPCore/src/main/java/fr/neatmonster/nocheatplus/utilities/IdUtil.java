package fr.neatmonster.nocheatplus.utilities;

import java.util.UUID;

/**
 * Utility for UUIDs, player names.
 * @author mc_dev
 *
 */
public class IdUtil {
	
	/**
	 * Valid user name check (Minecraft).<br>
	 * (Taken from TrustCore.)
	 * @param name Allows null input.
	 * @return
	 */
	public static boolean isValidMinecraftUserName(final String name) {
		return name != null && name.matches("[\\w]{2,16}");
	}
	
	/**
	 * Safe method to parse a UUID, using UUIDFromString internally.
	 * @param input
	 * @return
	 */
	public static UUID UUIDFromStringSafe(final String input) {
		if (input == null) {
			return null;
		}
		try {
			return UUIDFromString(input);
		}
		catch (IllegalArgumentException e1) {}
		return null;
	}
	
	/**
	 * More flexible UUID parsing.<br>
	 * (Taken from TrustCore.)
	 * @param input
	 * @return
	 */
	public static UUID UUIDFromString(final String input) {
		// TODO: Add unit tests.
		final int len = input.length();
		if (len == 36) {
			return UUID.fromString(input);
		} else if (len == 32) {
			// TODO: Might better translate to longs right away !?
			// Fill in '-'
			char[] chars = input.toCharArray();
			char[] newChars = new char[36];
			int index = 0;
			int targetIndex = 0;
			while (targetIndex < 36) {
				newChars[targetIndex] = chars[index];
				index ++;
				targetIndex ++;
				switch (index) {
    				case 8:
    				case 12:
    				case 16:
    				case 20:
    					newChars[targetIndex] = '-';
    					targetIndex ++;
    					break;
					default:
						break;
				}
			}
			return UUID.fromString(new String(newChars));
		} else {
			throw new IllegalArgumentException("Unexpected length (" + len + ") for uuid: " + input);
		}
	}
	
}
