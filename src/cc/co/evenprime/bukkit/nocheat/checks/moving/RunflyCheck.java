package cc.co.evenprime.bukkit.nocheat.checks.moving;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;

public class RunflyCheck extends MovingCheck {

    private final FlyingCheck  flyingCheck;
    private final RunningCheck runningCheck;

    public RunflyCheck(NoCheat plugin) {
        // Permission field intentionally left blank here
        // We check in the actual "check" method, because
        // we have to do something beside skipping the test
        super(plugin, "moving.runfly", null);

        flyingCheck = new FlyingCheck(plugin);
        runningCheck = new RunningCheck(plugin);
    }

    @Override
    public PreciseLocation check(NoCheatPlayer player, MovingData data, MovingConfig cc) {

        if(player.hasPermission(Permissions.MOVING_RUNFLY)) {
            // If the player doesn't get checked for movement
            // reset his critical data
            data.clearCriticalData();
            return null;
        }
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
    public boolean isEnabled(MovingConfig moving) {
        return moving.runflyCheck;
    }
}
