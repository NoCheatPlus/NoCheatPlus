package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/*
 * MM"""""""`MM                            dP       
 * MM  mmmm,  M                            88       
 * M'        .M .d8888b. .d8888b. .d8888b. 88d888b. 
 * MM  MMMb. "M 88ooood8 88'  `88 88'  `"" 88'  `88 
 * MM  MMMMM  M 88.  ... 88.  .88 88.  ... 88    88 
 * MM  MMMMM  M `88888P' `88888P8 `88888P' dP    dP 
 * MMMMMMMMMMMM                                     
 */
/**
 * The Reach check will find out if a player interacts with something that's too far away.
 */
public class Reach extends Check {

    /**
     * The event triggered by this check.
     */
    public class ReachEvent extends CheckEvent {

        /**
         * Instantiates a new reach event.
         * 
         * @param player
         *            the player
         */
        public ReachEvent(final Player player) {
            super(player);
        }
    }

    /** The maximum distance allowed to interact with a block. */
    public final double DISTANCE = 5D; // TODO: Test with creative mode.

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param location
     *            the location
     * @return true, if successful
     */
    public boolean check(final Player player, final Location location) {
        final BlockBreakConfig cc = BlockBreakConfig.getConfig(player);
        final BlockBreakData data = BlockBreakData.getData(player);

        boolean cancel = false;

        // Distance is calculated from eye location to center of targeted block. If the player is further away from his
        // target than allowed, the difference will be assigned to "distance".
        final double distance = Math.max(CheckUtils.distance(player, location) - DISTANCE, 0D);

        if (distance > 0) {
            // He failed, increment violation level.
            data.reachVL += distance;

            // Dispatch a reach event (API).
            final ReachEvent e = new ReachEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Remember how much further than allowed he tried to reach for logging, if necessary.
            data.reachDistance = distance + DISTANCE;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = !e.isCancelled() && executeActions(player, cc.reachActions, data.reachVL);
        } else
            // Player passed the check, reward him.
            data.reachVL *= 0.9D;

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(BlockBreakData.getData(player).reachVL));
        else if (wildcard == ParameterName.REACHDISTANCE)
            return String.valueOf(Math.round(BlockBreakData.getData(player).reachDistance));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.BLOCKBREAK_REACH) && BlockBreakConfig.getConfig(player).reachCheck;
    }
}
