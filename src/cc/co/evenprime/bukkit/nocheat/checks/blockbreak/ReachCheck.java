package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

/**
 * The reach check will find out if a player interacts with something that's
 * too far away
 * 
 */
public class ReachCheck extends BlockBreakCheck {

    public ReachCheck(NoCheat plugin) {
        super(plugin, "blockbreak.reach");
    }

    public boolean check(NoCheatPlayer player, BlockBreakData data, BlockBreakConfig cc) {

        boolean cancel = false;

        final SimpleLocation brokenBlock = data.brokenBlockLocation;

        // Distance is calculated from eye location to center of targeted block
        // If the player is further away from his target than allowed, the
        // difference will be assigned to "distance"
        final double distance = CheckUtil.reachCheck(player, brokenBlock.x + 0.5D, brokenBlock.y + 0.5D, brokenBlock.z + 0.5D, player.isCreative() ? cc.reachDistance + 2 : cc.reachDistance);

        if(distance <= 0D) {
            // Player passed the check, reward him
            data.reachVL *= 0.9D;
        } else {
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
    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).reachVL);
        else if(wildcard == ParameterName.REACHDISTANCE)
            return String.format(Locale.US, "%.2f", getData(player).reachDistance);
        else
            return super.getParameter(wildcard, player);
    }
}
