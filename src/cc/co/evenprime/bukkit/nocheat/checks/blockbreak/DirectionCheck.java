package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionWithParameters.WildCard;
import cc.co.evenprime.bukkit.nocheat.checks.BlockBreakCheck;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCBlockBreak;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * The DirectionCheck will find out if a player tried to interact with something
 * that's not in his field of view.
 * 
 */
public class DirectionCheck extends BlockBreakCheck {

    public DirectionCheck(NoCheat plugin) {
        super(plugin, "blockbreak.direction", Permissions.BLOCKBREAK_DIRECTION);
    }

    public boolean check(final NoCheatPlayer player, final BlockBreakData blockbreak, final CCBlockBreak ccblockbreak) {

        final SimpleLocation brokenBlock = blockbreak.brokenBlockLocation;
        final boolean isInstaBreak = blockbreak.instaBrokenBlockLocation.equals(brokenBlock);

        // If the block is instabreak and we don't check instabreak, return
        if(isInstaBreak && !ccblockbreak.checkinstabreakblocks) {
            return false;
        }

        boolean cancel = false;

        double off = CheckUtil.directionCheck(player, brokenBlock.x + 0.5D, brokenBlock.y + 0.5D, brokenBlock.z + 0.5D, 1D, 1D, ccblockbreak.directionPrecision);

        final long time = System.currentTimeMillis();

        if(off < 0.1D) {
            // Player did nothing wrong
            // reduce violation counter
            blockbreak.directionVL *= 0.9D;
        } else {
            // Player failed the check
            // Increment violation counter
            if(isInstaBreak) {
                // Instabreak block failures are very common, so don't be as
                // hard on people failing them
                off /= 10;
            }
            blockbreak.directionVL += off;

            cancel = executeActions(player, ccblockbreak.directionActions.getActions(blockbreak.directionVL));

            if(cancel) {
                // Needed to calculate penalty times
                blockbreak.directionLastViolationTime = time;
            }
        }

        // If the player is still in penalty time, cancel the event anyway
        if(blockbreak.directionLastViolationTime + ccblockbreak.directionPenaltyTime > time) {
            if(blockbreak.directionLastViolationTime > time) {
                System.out.println("Nocheat noted that your time ran backwards for " + (blockbreak.directionLastViolationTime - time) + " ms");
                blockbreak.directionLastViolationTime = 0;
            }
            return true;
        }

        return cancel;
    }

    public boolean isEnabled(CCBlockBreak cc) {
        return cc.directionCheck;
    }

    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case VIOLATIONS:
            return String.format(Locale.US, "%d", (int) player.getData().blockbreak.directionVL);

        default:
            return super.getParameter(wildcard, player);

        }
    }
}
