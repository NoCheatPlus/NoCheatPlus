package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * The reach check will find out if a player interacts with something that's too
 * far away
 * 
 */
public class ReachCheck extends BlockPlaceCheck {

    public ReachCheck(NoCheat plugin) {
        super(plugin, "blockplace.reach", Permissions.BLOCKPLACE_REACH);
    }

    public boolean check(NoCheatPlayer player, BlockPlaceData data, CCBlockPlace cc) {

        boolean cancel = false;

        final SimpleLocation placedAgainstBlock = data.blockPlacedAgainst;

        final double distance = CheckUtil.reachCheck(player, placedAgainstBlock.x + 0.5D, placedAgainstBlock.y + 0.5D, placedAgainstBlock.z + 0.5D, player.isCreative() ? cc.reachDistance + 2 : cc.reachDistance);

        if(distance > 0D) {
            // Player failed the check

            // Increment violation counter
            data.reachVL += distance;
            data.reachTotalVL += distance;
            data.reachFailed++;
            data.reachdistance = distance;

            cancel = executeActions(player, cc.reachActions.getActions(data.reachVL));
        } else {
            data.reachVL *= 0.9D;
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(CCBlockPlace cc) {
        return cc.reachCheck;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).reachVL);
        else if(wildcard == ParameterName.REACHDISTANCE)
            return String.format(Locale.US, "%.2f", getData(player.getDataStore()).reachdistance);
        else
            return super.getParameter(wildcard, player);
    }
}
