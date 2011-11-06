package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionWithParameters.WildCard;
import cc.co.evenprime.bukkit.nocheat.checks.BlockPlaceCheck;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCBlockPlace;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

public class DirectionCheck extends BlockPlaceCheck {

    public DirectionCheck(NoCheat plugin) {
        super(plugin, "blockplace.direction", Permissions.BLOCKPLACE_DIRECTION);
    }

    public boolean check(NoCheatPlayer player, BlockPlaceData data, CCBlockPlace cc) {

        boolean cancel = false;

        final SimpleLocation blockPlaced = data.blockPlaced;
        final SimpleLocation blockPlacedAgainst = data.blockPlacedAgainst;

        // First check if the crosshair matches (roughly) the clicked block
        double off = CheckUtil.directionCheck(player, blockPlacedAgainst.x + 0.5D, blockPlacedAgainst.y + 0.5D, blockPlacedAgainst.z + 0.5D, 1D, 1D, cc.directionPrecision);

        // now check if the player is looking at the block from the correct side

        double off2 = 0.0D;

        // Find out against which face the player tried to build, and if he
        // stood on the correct side
        if(blockPlaced.x > blockPlacedAgainst.x) {
            off2 = blockPlacedAgainst.x + 0.5D - player.getPlayer().getEyeLocation().getX();
        } else if(blockPlaced.x < blockPlacedAgainst.x) {
            off2 = -(blockPlacedAgainst.x + 0.5D - player.getPlayer().getEyeLocation().getX());
        } else if(blockPlaced.y > blockPlacedAgainst.y) {
            off2 = blockPlacedAgainst.y + 0.5D - player.getPlayer().getEyeLocation().getY();
        } else if(blockPlaced.y < blockPlacedAgainst.y) {
            off2 = -(blockPlacedAgainst.y + 0.5D - player.getPlayer().getEyeLocation().getY());
        } else if(blockPlaced.z > blockPlacedAgainst.z) {
            off2 = blockPlacedAgainst.z + 0.5D - player.getPlayer().getEyeLocation().getZ();
        } else if(blockPlaced.z < blockPlacedAgainst.z) {
            off2 = -(blockPlacedAgainst.z + 0.5D - player.getPlayer().getEyeLocation().getZ());
        }

        if(off2 > 0.0D) {
            off += off2;
        }

        final long time = System.currentTimeMillis();

        if(off < 0.1D) {
            // Player did nothing wrong
            // reduce violation counter
            data.directionViolationLevel *= 0.9D;
        } else {
            // Player failed the check
            // Increment violation counter
            data.directionViolationLevel += off;

            // Prepare some event-specific values for logging and custom actions

            cancel = executeActions(player, cc.directionActions.getActions(data.directionViolationLevel));

            if(cancel) {
                // Needed to calculate penalty times
                data.directionLastViolationTime = time;
            }
        }

        // If the player is still in penalty time, cancel the event anyway
        if(data.directionLastViolationTime + cc.directionPenaltyTime > time) {
            return true;
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(CCBlockPlace cc) {
        return cc.directionCheck;
    }

    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case VIOLATIONS:
            return String.format(Locale.US, "%d", player.getData().blockplace.directionViolationLevel);

        default:
            return super.getParameter(wildcard, player);
        }
    }
}
