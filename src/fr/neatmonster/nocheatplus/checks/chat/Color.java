package fr.neatmonster.nocheatplus.checks.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;

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
public class Color extends Check {

    /**
     * The event triggered by this check.
     */
    public class ColorEvent extends CheckEvent {

        /**
         * Instantiates a new color event.
         * 
         * @param player
         *            the player
         */
        public ColorEvent(final Player player) {
            super(player);
        }
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param message
     *            the message
     * @return the string
     */
    public String check(final Player player, final String message) {
        final ChatConfig cc = ChatConfig.getConfig(player);
        final ChatData data = ChatData.getData(player);

        // If the message contains colors...
        if (message.contains("\247")) {
            // Increment the violation level of the player.
            data.colorVL++;

            // Dispatch a color event (API).
            final ColorEvent e = new ColorEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Find out if we need to remove the colors or not.
            if (!e.isCancelled() && executeActions(player, cc.colorActions, data.colorVL))
                // Remove color codes.
                message.replaceAll("\302\247.", "").replaceAll("\247.", "");
        }

        return message;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(ChatData.getData(player).colorVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.CHAT_COLOR) && ChatConfig.getConfig(player).colorCheck;
    }
}
