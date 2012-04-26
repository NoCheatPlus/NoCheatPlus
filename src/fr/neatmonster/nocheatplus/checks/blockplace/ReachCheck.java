package fr.neatmonster.nocheatplus.checks.blockplace;

import java.util.Locale;

import org.bukkit.GameMode;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.CheckUtils;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.SimpleLocation;

/**
 * The reach check will find out if a player interacts with something that's
 * too far away
 * 
 */
public class ReachCheck extends BlockPlaceCheck {

    public ReachCheck() {
        super("reach");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final BlockPlaceConfig cc = getConfig(player);
        final BlockPlaceData data = getData(player);

        boolean cancel = false;

        final SimpleLocation placedAgainstBlock = data.blockPlacedAgainst;

        // Distance is calculated from eye location to center of targeted block
        // If the player is further away from his target than allowed, the
        // difference will be assigned to "distance"
        final double distance = CheckUtils.reachCheck(player, placedAgainstBlock.x + 0.5D, placedAgainstBlock.y + 0.5D,
                placedAgainstBlock.z + 0.5D,
                player.getBukkitPlayer().getGameMode() == GameMode.CREATIVE ? cc.reachDistance + 2 : cc.reachDistance);

        if (distance <= 0D)
            // Player passed the check, reward him
            data.reachVL *= 0.9D;
        else {
            // He failed, increment violation level and statistics
            data.reachVL += distance;
            incrementStatistics(player, Id.BP_REACH, distance);

            // Remember how much further than allowed he tried to reach for
            // logging, if necessary
            data.reachdistance = distance;

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.reachActions, data.reachVL);
        }

        return cancel;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).reachVL);
        else if (wildcard == ParameterName.REACHDISTANCE)
            return String.format(Locale.US, "%.2f", getData(player).reachdistance);
        else
            return super.getParameter(wildcard, player);
    }
}
