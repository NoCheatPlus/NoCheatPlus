package cc.co.evenprime.bukkit.nocheat.log;

import org.bukkit.ChatColor;

/**
 * Somehow manage color codes in NoCheat
 *
 */
public class Colors {

    public static String replaceColors(String text) {
        
        for(ChatColor c : ChatColor.values()) {
            text = text.replace("&" + Integer.toHexString(c.getCode()), c.toString());
        }
        
        return text;
    }
}
