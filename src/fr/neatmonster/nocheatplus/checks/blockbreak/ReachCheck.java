package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckUtils;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.SimpleLocation;

/**
 * The reach check will find out if a player interacts with something that's
 * too far away
 * 
 */
public class ReachCheck extends BlockBreakCheck {

    public class ReachCheckEvent extends BlockBreakEvent {

        public ReachCheckEvent(final ReachCheck check, final NCPPlayer player, final ActionList actions, final double vL) {
            super(check, player, actions, vL);
        }
    }

    public ReachCheck() {
        super("reach");
    }

    @Override
    public boolean check(final fr.neatmonster.nocheatplus.players.NCPPlayer player, final Object... args) {
        final BlockBreakConfig cc = getConfig(player);
        final BlockBreakData data = getData(player);

        boolean cancel = false;

        final SimpleLocation brokenBlock = data.brokenBlockLocation;

        // Distance is calculated from eye location to center of targeted block
        // If the player is further away from his target than allowed, the
        // difference will be assigned to "distance"
        final double distance = CheckUtils.reachCheck(player, brokenBlock.x + 0.5D, brokenBlock.y + 0.5D,
                brokenBlock.z + 0.5D,
                player.getBukkitPlayer().getGameMode() == GameMode.CREATIVE ? cc.reachDistance + 2 : cc.reachDistance);

        if (distance <= 0D)
            // Player passed the check, reward him
            data.reachVL *= 0.9D;
        else {
            // He failed, increment violation level and statistics
            data.reachVL += distance;
            incrementStatistics(player, Id.BB_REACH, distance);

            // Remember how much further than allowed he tried to reach for
            // logging, if necessary
            data.reachDistance = distance;

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.reachActions, data.reachVL);
        }

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final ReachCheckEvent event = new ReachCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).reachVL));
        else if (wildcard == ParameterName.REACHDISTANCE)
            return String.valueOf(Math.round(getData(player).reachDistance));
        else
            return super.getParameter(wildcard, player);
    }
}
