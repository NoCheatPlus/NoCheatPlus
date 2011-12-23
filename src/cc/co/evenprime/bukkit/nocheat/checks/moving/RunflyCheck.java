package cc.co.evenprime.bukkit.nocheat.checks.moving;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
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

        final boolean flyAllowed = cc.allowFlying || player.hasPermission(Permissions.MOVING_FLYING) || (player.isCreative() && cc.identifyCreativeMode);

        /********************* EXECUTE THE FLY/JUMP/RUNNING CHECK ********************/
        // If the player is not allowed to fly and not allowed to run

        if(flyAllowed) {
            return flyingCheck.check(player, data, cc);
        } else {
            return runningCheck.check(player, data, cc);
        }
    }

    @Override
    public boolean isEnabled(CCMoving moving) {
        return moving.runflyCheck;
    }
}
