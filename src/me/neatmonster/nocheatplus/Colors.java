package me.neatmonster.nocheatplus;

import org.bukkit.ChatColor;

/**
 * Somehow manage color codes in NoCheatPlus
 * 
 */
public class Colors {

    /**
     * Remove instances of &X
     * 
     * @param text
     * @return
     */
    public static String removeColors(String text) {

        for (final ChatColor c : ChatColor.values())
            text = text.replace("&" + c.getChar(), "");

        return text;
    }

    /**
     * Replace instances of &X with a color
     * 
     * @param text
     * @return
     */
    public static String replaceColors(String text) {

        for (final ChatColor c : ChatColor.values())
            text = text.replace("&" + c.getChar(), c.toString());

        return text;
    }
}
