package cc.co.evenprime.bukkit.nocheat.checks.moving;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * The main Check class for Move event checking. It will decide which checks
 * need to be executed and in which order. It will also precalculate some values
 * that are needed by multiple checks.
 * 
 * @author Evenprime
 * 
 */
public class RunFlyCheck {

    private final FlyingCheck      flyingCheck;
    private final RunningCheck     runningCheck;
    private final NoFallCheck      noFallCheck;
    private final MorePacketsCheck morePacketsCheck;

    private final NoCheat          plugin;

    public RunFlyCheck(NoCheat plugin) {
        this.plugin = plugin;

        this.flyingCheck = new FlyingCheck(plugin);
        this.noFallCheck = new NoFallCheck(plugin);
        this.runningCheck = new RunningCheck(plugin, noFallCheck);
        this.morePacketsCheck = new MorePacketsCheck(plugin);
    }

    /**
     * Return the a new destination location or null
     * 
     * @param event
     * @return
     */
    public Location check(final Player player, final Location from, final Location to, final ConfigurationCache cc) {

        // Players in vehicles are of no interest
        if(player.isInsideVehicle())
            return null;

        /**
         * If not null, this will be used as the new target location
         */
        Location newToLocation = null;

        BaseData data = plugin.getPlayerData(player);

        /******** DO GENERAL DATA MODIFICATIONS ONCE FOR EACH EVENT *****/
        if(data.moving.horizVelocityCounter > 0) {
            data.moving.horizVelocityCounter--;
        } else {
            data.moving.horizFreedom *= 0.90;
        }

        if(data.moving.vertVelocityCounter > 0) {
            data.moving.vertVelocityCounter--;
            data.moving.vertFreedom += data.moving.vertVelocity;
            data.moving.vertVelocity *= 0.90;
        } else {
            data.moving.vertFreedom = 0;
        }

        /************* DECIDE WHICH CHECKS NEED TO BE RUN *************/
        final boolean runflyCheck = cc.moving.runflyCheck && !player.hasPermission(Permissions.MOVE_RUNFLY);
        final boolean flyAllowed = cc.moving.allowFlying || player.hasPermission(Permissions.MOVE_FLY) || (player.getGameMode() == GameMode.CREATIVE && cc.moving.identifyCreativeMode);
        final boolean morepacketsCheck = cc.moving.morePacketsCheck && !player.hasPermission(Permissions.MOVE_MOREPACKETS);

        /********************* EXECUTE THE FLY/JUMP/RUNNING CHECK ********************/
        // If the player is not allowed to fly and not allowed to run
        if(runflyCheck) {
            if(flyAllowed) {
                newToLocation = flyingCheck.check(player, from, to, cc);
            } else {
                newToLocation = runningCheck.check(player, from, to, cc);
            }
        }

        /********* EXECUTE THE MOREPACKETS CHECK ********************/

        if(newToLocation == null && morepacketsCheck) {
            newToLocation = morePacketsCheck.check(player, cc);
        }

        return newToLocation;
    }

    /**
     * This is a workaround for people placing blocks below them causing false
     * positives
     * with the move check(s).
     */
    public void blockPlaced(Player player, Block blockPlaced) {

        BaseData data = plugin.getPlayerData(player);

        if(blockPlaced == null || data.moving.runflySetBackPoint == null) {
            return;
        }

        Location lblock = blockPlaced.getLocation();
        Location lplayer = player.getLocation();

        if(Math.abs(lplayer.getBlockX() - lblock.getBlockX()) <= 1 && Math.abs(lplayer.getBlockZ() - lblock.getBlockZ()) <= 1 && lplayer.getBlockY() - lblock.getBlockY() >= 0 && lplayer.getBlockY() - lblock.getBlockY() <= 2) {

            int type = CheckUtil.getType(blockPlaced.getTypeId());
            if(CheckUtil.isSolid(type) || CheckUtil.isLiquid(type)) {
                if(lblock.getBlockY() + 1 >= data.moving.runflySetBackPoint.getY()) {
                    data.moving.runflySetBackPoint.setY(lblock.getBlockY() + 1);
                    data.moving.jumpPhase = 0;
                }
            }
        }
    }
}
