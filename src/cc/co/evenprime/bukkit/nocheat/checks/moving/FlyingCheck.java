package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

/**
 * A check designed for people that are allowed to fly. The complement to
 * the "RunningCheck", which is for people that aren't allowed to fly, and
 * therefore have tighter rules to obey.
 * 
 */
public class FlyingCheck extends MovingCheck {

    public FlyingCheck(NoCheat plugin) {
        super(plugin, "moving.flying");
    }

    private static final double creativeSpeed = 0.60D;

    public PreciseLocation check(NoCheatPlayer player, MovingData data, MovingConfig ccmoving) {

        final PreciseLocation setBack = data.runflySetBackPoint;
        final PreciseLocation from = data.from;
        final PreciseLocation to = data.to;

        if(!setBack.isSet()) {
            setBack.set(from);
        }

        PreciseLocation newToLocation = null;

        // Before doing anything, do a basic height check
        // This is silent for now, will log messages later
        // probably
        if(to.y - data.vertFreedom > ccmoving.flyingHeightLimit) {
            newToLocation = new PreciseLocation();
            newToLocation.set(setBack);
            newToLocation.y = ccmoving.flyingHeightLimit - 10;
            return newToLocation;
        }

        final double yDistance = to.y - from.y;

        // Calculate some distances
        final double xDistance = to.x - from.x;
        final double zDistance = to.z - from.z;
        final double horizontalDistance = Math.sqrt((xDistance * xDistance + zDistance * zDistance));

        double resultHoriz = 0;
        double resultVert = 0;
        double result = 0;

        // In case of creative gamemode, give at least 0.60 speed limit
        // horizontal
        double speedLimitHorizontal = player.isCreative() ? Math.max(creativeSpeed, ccmoving.flyingSpeedLimitHorizontal) : ccmoving.flyingSpeedLimitHorizontal;

        speedLimitHorizontal *= player.getSpeedAmplifier();

        resultHoriz = Math.max(0.0D, horizontalDistance - data.horizFreedom - speedLimitHorizontal);

        boolean sprinting = player.isSprinting();

        data.bunnyhopdelay--;

        // Did he go too far?
        if(resultHoriz > 0 && sprinting) {

            // Try to treat it as a the "bunnyhop" problem
            if(data.bunnyhopdelay <= 0 && resultHoriz < 0.4D) {
                data.bunnyhopdelay = 9;
                resultHoriz = 0;
            }
        }

        resultHoriz *= 100;

        double jumpAmplifier = player.getJumpAmplifier();
        if(jumpAmplifier > data.lastJumpAmplifier) {
            data.lastJumpAmplifier = jumpAmplifier;
        }

        double speedLimitVertical = ccmoving.flyingSpeedLimitVertical * data.lastJumpAmplifier;

        if(data.from.y >= data.to.y && data.lastJumpAmplifier > 0) {
            data.lastJumpAmplifier--;
        }

        // super simple, just check distance compared to max distance
        resultVert = Math.max(0.0D, yDistance - data.vertFreedom - speedLimitVertical) * 100;

        result = resultHoriz + resultVert;

        if(result > 0) {

            // Increment violation counter
            data.runflyVL += result;
            if(resultHoriz > 0) {
                incrementStatistics(player, Id.MOV_RUNNING, resultHoriz);
            }

            if(resultVert > 0) {
                incrementStatistics(player, Id.MOV_FLYING, resultVert);
            }

            boolean cancel = executeActions(player, ccmoving.flyingActions.getActions(data.runflyVL));

            // Was one of the actions a cancel? Then really do it
            if(cancel) {
                newToLocation = setBack;
            }
        }

        // Slowly reduce the level with each event
        data.runflyVL *= 0.97;

        // Some other cleanup 'n' stuff
        if(newToLocation == null) {
            setBack.set(to);
        }

        return newToLocation;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).runflyVL);
        else
            return super.getParameter(wildcard, player);
    }
}
