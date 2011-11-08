package cc.co.evenprime.bukkit.nocheat.checks.moving;

import org.bukkit.GameMode;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCMoving;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;

public class RunflyCheck extends MovingCheck {

    private final FlyingCheck  flyingCheck;
    private final RunningCheck runningCheck;

    public RunflyCheck(NoCheat plugin) {
        super(plugin, "moving.runfly", Permissions.MOVING_RUNFLY);

        flyingCheck = new FlyingCheck(plugin);
        runningCheck = new RunningCheck(plugin);
    }

    @Override
    public PreciseLocation check(NoCheatPlayer player, MovingData data, CCMoving cc) {

        final boolean runflyCheck = cc.runflyCheck && !player.hasPermission(Permissions.MOVING_RUNFLY);
        final boolean flyAllowed = cc.allowFlying || player.hasPermission(Permissions.MOVING_FLYING) || (player.getPlayer().getGameMode() == GameMode.CREATIVE && cc.identifyCreativeMode);

        /********************* EXECUTE THE FLY/JUMP/RUNNING CHECK ********************/
        // If the player is not allowed to fly and not allowed to run
        if(runflyCheck) {
            if(flyAllowed) {
                return flyingCheck.check(player, data, cc);
            } else {
                return runningCheck.check(player, data, cc);
            }
        }

        return null;
    }

    @Override
    public boolean isEnabled(CCMoving moving) {
        return runningCheck.isEnabled(moving) || flyingCheck.isEnabled(moving);
    }
}
