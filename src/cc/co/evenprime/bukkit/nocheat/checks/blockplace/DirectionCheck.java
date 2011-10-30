package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCBlockPlace;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

public class DirectionCheck {

    private final NoCheat plugin;

    public DirectionCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean check(final Player player, final BaseData data, final ConfigurationCache cc) {

        boolean cancel = false;

        final CCBlockPlace ccblockplace = cc.blockplace;
        final BlockPlaceData blockplace = data.blockplace;

        final SimpleLocation blockPlaced = blockplace.blockPlaced;
        final SimpleLocation blockPlacedAgainst = blockplace.blockPlacedAgainst;

        // First check if the crosshair matches (roughly) the clicked block
        double off = CheckUtil.directionCheck(player, blockPlacedAgainst.x + 0.5D, blockPlacedAgainst.y + 0.5D, blockPlacedAgainst.z + 0.5D, 1D, 1D, ccblockplace.directionPrecision);

        // now check if the player is looking at the block from the correct side

        double off2 = 0.0D;

        // Find out against which face the player tried to build, and if he
        // stood on the correct side
        if(blockPlaced.x > blockPlacedAgainst.x) {
            off2 = blockPlacedAgainst.x + 0.5D - player.getEyeLocation().getX();
        } else if(blockPlaced.x < blockPlacedAgainst.x) {
            off2 = -(blockPlacedAgainst.x + 0.5D - player.getEyeLocation().getX());
        } else if(blockPlaced.y > blockPlacedAgainst.y) {
            off2 = blockPlacedAgainst.y + 0.5D - player.getEyeLocation().getY();
        } else if(blockPlaced.y < blockPlacedAgainst.y) {
            off2 = -(blockPlacedAgainst.y + 0.5D - player.getEyeLocation().getY());
        } else if(blockPlaced.z > blockPlacedAgainst.z) {
            off2 = blockPlacedAgainst.z + 0.5D - player.getEyeLocation().getZ();
        } else if(blockPlaced.z < blockPlacedAgainst.z) {
            off2 = -(blockPlacedAgainst.z + 0.5D - player.getEyeLocation().getZ());
        }

        if(off2 > 0.0D) {
            off += off2;
        }

        final long time = System.currentTimeMillis();

        if(off < 0.1D) {
            // Player did nothing wrong
            // reduce violation counter
            blockplace.directionViolationLevel *= 0.9D;
        } else {
            // Player failed the check
            // Increment violation counter
            blockplace.directionViolationLevel += off;

            // Prepare some event-specific values for logging and custom actions
            data.log.check = "blockplace.direction";

            cancel = plugin.execute(player, ccblockplace.directionActions, (int) blockplace.directionViolationLevel, blockplace.history, cc);

            if(cancel) {
                // Needed to calculate penalty times
                blockplace.directionLastViolationTime = time;
            }
        }

        // If the player is still in penalty time, cancel the event anyway
        if(blockplace.directionLastViolationTime + ccblockplace.directionPenaltyTime >= time) {
            return true;
        }

        return cancel;
    }
}
