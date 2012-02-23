package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

/**
 * The DirectionCheck will find out if a player tried to interact with something
 * that's not in his field of view.
 * 
 */
public class DirectionCheck extends BlockBreakCheck {

    public DirectionCheck(NoCheat plugin) {
        super(plugin, "blockbreak.direction");
    }

    public boolean check(final NoCheatPlayer player, final BlockBreakData data, final BlockBreakConfig ccblockbreak) {

        final SimpleLocation brokenBlock = data.brokenBlockLocation;
        final boolean isInstaBreak = data.instaBrokenBlockLocation.equals(brokenBlock);
        boolean cancel = false;

        double off = CheckUtil.directionCheck(player, brokenBlock.x + 0.5D, brokenBlock.y + 0.5D, brokenBlock.z + 0.5D, 1D, 1D, ccblockbreak.directionPrecision);

        final long time = System.currentTimeMillis();

        if(off < 0.1D) {
            // Player did nothing wrong
            // reduce violation counter
            data.directionVL *= 0.9D;
        } else {
            // Player failed the check
            // Increment violation counter
            if(isInstaBreak) {
                // Instabreak block failures are very common, so don't be as
                // hard on people failing them
                off /= 5;
            }
            data.directionVL += off;
            incrementStatistics(player, Id.BB_DIRECTION, off);

            cancel = executeActions(player, ccblockbreak.directionActions, data.directionVL);

            if(cancel) {
                // Needed to calculate penalty times
                data.directionLastViolationTime = time;
            }
        }

        // If the player is still in penalty time, cancel the event anyway
        if(data.directionLastViolationTime + ccblockbreak.directionPenaltyTime > time) {
            if(data.directionLastViolationTime > time) {
                System.out.println("Nocheat noted that your time ran backwards for " + (data.directionLastViolationTime - time) + " ms");
                data.directionLastViolationTime = 0;
            }
            return true;
        }

        return cancel;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).directionVL);
        else
            return super.getParameter(wildcard, player);
    }
}
