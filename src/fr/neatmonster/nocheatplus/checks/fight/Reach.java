package fr.neatmonster.nocheatplus.checks.fight;

import net.minecraft.server.Entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;

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

    /** The maximum distance allowed to interact with an entity. */
    public final double DISTANCE = 4D; // TODO: Needs testing.

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param damaged
     *            the damaged
     * @return true, if successful
     */
    public boolean check(final Player player, final Entity damaged) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        boolean cancel = false;

        final Location minimum = new Location(player.getWorld(), damaged.boundingBox.a, damaged.boundingBox.b,
                damaged.boundingBox.c);
        final Location maximum = new Location(player.getWorld(), damaged.boundingBox.d, damaged.boundingBox.e,
                damaged.boundingBox.f);
        final Location location = minimum.add(maximum).multiply(0.5D);

        // Distance is calculated from eye location to center of targeted. If the player is further away from his target
        // than allowed, the difference will be assigned to "distance".
        final double distance = Math.max(CheckUtils.distance(player, location) - DISTANCE, 0D);

        if (distance > 0) {
            // He failed, increment violation level. This is influenced by lag, so don't do it if there was lag.
            if (!LagMeasureTask.skipCheck())
                data.reachVL += distance;

            // Dispatch a reach event (API).
            final ReachEvent e = new ReachEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = !e.isCancelled() && executeActions(player, cc.reachActions, data.reachVL);

            if (cancel)
                // If we should cancel, remember the current time too.
                data.reachLastViolationTime = System.currentTimeMillis();
        } else
            // Player passed the check, reward him.
            data.reachVL *= 0.8D;

        // If the player is still in penalty time, cancel the event anyway.
        if (data.reachLastViolationTime + cc.reachPenalty > System.currentTimeMillis()) {
            // A safeguard to avoid people getting stuck in penalty time indefinitely in case the system time of the
            // server gets changed.
            if (data.reachLastViolationTime > System.currentTimeMillis())
                data.reachLastViolationTime = 0;

            // He is in penalty time, therefore request cancelling of the event.
            return true;
        }

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(FightData.getData(player).reachVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.FIGHT_REACH) && FightConfig.getConfig(player).reachCheck;
    }
}
