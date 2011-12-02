package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionWithParameters.WildCard;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCMoving;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;

/**
 * A check designed for people that are allowed to fly. The complement to
 * the "RunningCheck", which is for people that aren't allowed to fly, and
 * therefore have tighter rules to obey.
 * 
 */
public class FlyingCheck extends MovingCheck {

    public FlyingCheck(NoCheat plugin) {
        super(plugin, "moving.flying", null);
    }

    private static final double creativeSpeed = 0.60D;

    public PreciseLocation check(NoCheatPlayer player, MovingData data, CCMoving ccmoving) {

        final PreciseLocation setBack = data.runflySetBackPoint;
        final PreciseLocation from = data.from;
        final PreciseLocation to = data.to;

        if(!setBack.isSet()) {
            setBack.set(from);
        }

        final double yDistance = to.y - from.y;

        // Calculate some distances
        final double xDistance = to.x - from.x;
        final double zDistance = to.z - from.z;
        final double horizontalDistance = Math.sqrt((xDistance * xDistance + zDistance * zDistance));

        double result = 0;
        PreciseLocation newToLocation = null;

        // In case of creative gamemode, give at least 0.60 speed limit
        // horizontal
        double speedLimitHorizontal = player.isCreative() ? Math.max(creativeSpeed, ccmoving.flyingSpeedLimitHorizontal) : ccmoving.flyingSpeedLimitHorizontal;

        speedLimitHorizontal *= player.getSpeedAmplifier();

        result += Math.max(0.0D, horizontalDistance - data.horizFreedom - speedLimitHorizontal);

        boolean sprinting = player.isSprinting();

        data.bunnyhopdelay--;

        // Did he go too far?
        if(result > 0 && sprinting) {

            // Try to treat it as a the "bunnyhop" problem
            if(data.bunnyhopdelay <= 0 && result < 0.4D) {
                data.bunnyhopdelay = 3;
                result = 0;
            }
        }

        // super simple, just check distance compared to max distance
        result += Math.max(0.0D, yDistance - data.vertFreedom - ccmoving.flyingSpeedLimitVertical);
        result = result * 100;

        if(result > 0) {

            // Increment violation counter
            data.runflyVL += result;
            data.runflyTotalVL += result;
            data.runflyFailed++;

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

    @Override
    public boolean isEnabled(CCMoving moving) {
        return moving.allowFlying && moving.runflyCheck;
    }

    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case VIOLATIONS:
            return String.format(Locale.US, "%d", (int) player.getData().moving.runflyVL);
        default:
            return super.getParameter(wildcard, player);
        }
    }
}
