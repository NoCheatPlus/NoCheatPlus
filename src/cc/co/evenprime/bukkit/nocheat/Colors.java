package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.ChatColor;


/**
 * Somehow manage color codes in NoCheat
 * 
 */
public class Colors {

    
    /** 
     * Replace instances of &X with a color
     * 
     * @param text
     * @return
     */
    public static String replaceColors(String text) {

        for(ChatColor c : ChatColor.values()) {
            text = text.replace("&" + c.getChar(), c.toString());
        }

        return text;
    }
    
    /** 
     * Remove instances of &X
     * 
     * @param text
     * @return
     */
    public static String removeColors(String text) {

        for(ChatColor c : ChatColor.values()) {
            text = text.replace("&" + c.getChar(), "");
        }

        return text;
    }
}
