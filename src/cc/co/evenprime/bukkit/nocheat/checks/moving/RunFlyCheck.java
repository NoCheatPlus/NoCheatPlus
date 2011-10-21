package cc.co.evenprime.bukkit.nocheat.checks.moving;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCMoving;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

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
    public PreciseLocation check(final Player player, final BaseData data, final ConfigurationCache cc) {

        // Players in vehicles are of no interest
        if(player.isInsideVehicle())
            return null;

        /**
         * If not null, this will be used as the new target location
         */
        PreciseLocation newTo = null;

        final MovingData moving = data.moving;

        /******** DO GENERAL DATA MODIFICATIONS ONCE FOR EACH EVENT *****/
        if(moving.horizVelocityCounter > 0) {
            moving.horizVelocityCounter--;
        } else {
            moving.horizFreedom *= 0.90;
        }

        if(moving.vertVelocityCounter > 0) {
            moving.vertVelocityCounter--;
            moving.vertFreedom += moving.vertVelocity;
            moving.vertVelocity *= 0.90;
        } else {
            moving.vertFreedom = 0;
        }

        final CCMoving ccmoving = cc.moving;

        /************* DECIDE WHICH CHECKS NEED TO BE RUN *************/
        final boolean runflyCheck = ccmoving.runflyCheck && !player.hasPermission(Permissions.MOVE_RUNFLY);
        final boolean flyAllowed = ccmoving.allowFlying || player.hasPermission(Permissions.MOVE_FLY) || (player.getGameMode() == GameMode.CREATIVE && ccmoving.identifyCreativeMode);
        final boolean morepacketsCheck = ccmoving.morePacketsCheck && !player.hasPermission(Permissions.MOVE_MOREPACKETS);

        /********************* EXECUTE THE FLY/JUMP/RUNNING CHECK ********************/
        // If the player is not allowed to fly and not allowed to run
        if(runflyCheck) {
            if(flyAllowed) {
                newTo = flyingCheck.check(player, data, cc);
            } else {
                newTo = runningCheck.check(player, data, cc);
            }
        }

        /********* EXECUTE THE MOREPACKETS CHECK ********************/

        if(newTo == null && morepacketsCheck) {
            newTo = morePacketsCheck.check(player, data, cc);
        }

        return newTo;
    }

    /**
     * This is a workaround for people placing blocks below them causing false
     * positives
     * with the move check(s).
     */
    public void blockPlaced(Player player, Block blockPlaced) {

        BaseData data = plugin.getData(player.getName());

        if(blockPlaced == null || !data.moving.runflySetBackPoint.isSet()) {
            return;
        }

        SimpleLocation lblock = new SimpleLocation();
        lblock.setLocation(blockPlaced);
        SimpleLocation lplayer = new SimpleLocation();
        lplayer.setLocation(player.getLocation());

        if(Math.abs(lplayer.x - lblock.x) <= 1 && Math.abs(lplayer.z - lblock.z) <= 1 && lplayer.y - lblock.y >= 0 && lplayer.y - lblock.y <= 2) {

            int type = CheckUtil.getType(blockPlaced.getTypeId());
            if(CheckUtil.isSolid(type) || CheckUtil.isLiquid(type)) {
                if(lblock.y + 1 >= data.moving.runflySetBackPoint.y) {
                    data.moving.runflySetBackPoint.y = (lblock.y + 1);
                    data.moving.jumpPhase = 0;
                }
            }
        }
    }
}
