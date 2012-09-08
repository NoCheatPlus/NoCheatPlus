package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.AsyncCheck;
import fr.neatmonster.nocheatplus.checks.CheckType;

/*
 * MM'""""'YMM          dP                   
 * M' .mmm. `M          88                   
 * M  MMMMMooM .d8888b. 88 .d8888b. 88d888b. 
 * M  MMMMMMMM 88'  `88 88 88'  `88 88'  `88 
 * M. `MMM' .M 88.  .88 88 88.  .88 88       
 * MM.     .dM `88888P' dP `88888P' dP       
 * MMMMMMMMMMM                               
 */
/**
 * The Color check verifies that no color codes are sent in players' messages.
 */
public class Color extends AsyncCheck {

    /**
     * Instantiates a new color check.
     */
    public Color() {
        super(CheckType.CHAT_COLOR);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param message
     *            the message
     * @param isMainThread
     *            is the thread the main thread
     * @return the string
     */
    public String check(final Player player, final String message, final boolean isMainThread) {

        final ChatConfig cc = ChatConfig.getConfig(player);

        final ChatData data = ChatData.getData(player);
        // Keep related to ChatData/NoPwnage/Color used lock.
        synchronized (data) {
            // If the message contains colors...
            if (message.contains("\247")) {
                // Increment the violation level of the player.
                data.colorVL++;

                // Find out if we need to remove the colors or not.
                if (executeActionsThreadSafe(player, data.colorVL, 1D, cc.colorActions, isMainThread))
                    // Remove color codes.
                    return message.replaceAll("\302\247.", "").replaceAll("\247.", "");
            }
        }

        return message;
    }

}
