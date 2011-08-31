package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;

/**
 * The main Check class for blockbreak event checking. It will decide which checks
 * need to be executed and in which order. It will also precalculate some values
 * that are needed by multiple checks.
 * 
 * @author Evenprime
 * 
 */
public class BlockBreakCheck {

    private final ReachCheck     reachCheck;
    private final DirectionCheck directionCheck;

    public BlockBreakCheck(NoCheat plugin) {

        this.reachCheck = new ReachCheck(plugin);
        this.directionCheck = new DirectionCheck(plugin);
    }

    public boolean check(final Player player, final Block brokenBlock, final BlockBreakData data, final ConfigurationCache cc) {

        boolean cancel = false;

        boolean reach = cc.blockbreak.reachCheck && !player.hasPermission(Permissions.BLOCKBREAK_REACH);
        boolean direction = cc.blockbreak.directionCheck && !player.hasPermission(Permissions.BLOCKBREAK_DIRECTION);

        if((reach || direction) && brokenBlock != null) {
            Location eyes = player.getEyeLocation();

            final double x1 = ((double) brokenBlock.getX()) - eyes.getX() - 0.5;
            final double y1 = ((double) brokenBlock.getY()) - eyes.getY() - 0.5;
            final double z1 = ((double) brokenBlock.getZ()) - eyes.getZ() - 0.5;

            double factor = new Vector(x1 + 1, y1 + 1, z1 + 1).length();

            if(reach) {
                cancel = reachCheck.check(player, factor, data, cc);
            }

            if(!cancel && direction && !brokenBlock.getLocation().equals(data.instaBrokeBlockLocation)) {
                cancel = directionCheck.check(player, factor, x1, y1, z1, data, cc);
            }
        }
        return cancel;
    }
}
