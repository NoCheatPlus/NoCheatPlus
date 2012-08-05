package fr.neatmonster.nocheatplus.checks.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * MMP"""""""MM                   oo                   dP          
 * M' .mmmm  MM                                        88          
 * M         `M 88d888b. 88d888b. dP dP   .dP .d8888b. 88 .d8888b. 
 * M  MMMMM  MM 88'  `88 88'  `88 88 88   d8' 88'  `88 88 Y8ooooo. 
 * M  MMMMM  MM 88       88       88 88 .88'  88.  .88 88       88 
 * M  MMMMM  MM dP       dP       dP 8888P'   `88888P8 dP `88888P' 
 * MMMMMMMMMMMM                                                    
 */
/**
 * The Arrivals check is used to limit the number of new players allowed to join in a specified time frame.
 */
public class Arrivals extends Check {

    /**
     * The event triggered by this check.
     */
    public class ArrivalsEvent extends CheckEvent {

        /**
         * Instantiates a new arrivals event.
         * 
         * @param player
         *            the player
         */
        public ArrivalsEvent(final Player player) {
            super(player);
        }
    }

    /** The map containing the time and the name of the player, every time that one of them joins. */
    private final Map<Long, String> joins = new HashMap<Long, String>();

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
        final ChatConfig cc = ChatConfig.getConfig(player);

        // If the server has just restarted or if the player is a regular one, do not check it.
        if (System.currentTimeMillis() - NoCheatPlus.time < 120000L
                || System.currentTimeMillis() - player.getFirstPlayed() > cc.arrivalsJoinsLimit)
            return false;

        boolean cancel = false;

        // Remove the old data from the map holding the joins.
        final List<Long> toRemove = new ArrayList<Long>();
        for (final long time : joins.keySet())
            // If the data is too old or belong to the checked player.
            if (System.currentTimeMillis() - time > cc.arrivalsTimeLimit && joins.get(time).equals(player.getName()))
                toRemove.add(time);
        for (final long time : toRemove)
            joins.remove(time);

        // Add the new data.
        joins.put(System.currentTimeMillis(), player.getName());

        if (joins.size() > cc.arrivalsJoinsLimit) {
            // Dispatch an arrivals event (API).
            final ArrivalsEvent e = new ArrivalsEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Find out if we should cancel the event or not.
            cancel = !e.isCancelled() && executeActions(player, cc.arrivalsActions, 0);
        }

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return "0";
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.CHAT_ARRIVALS) && ChatConfig.getConfig(player).arrivalsCheck;
    }
}
