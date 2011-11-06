package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.Locale;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.entity.CraftPlayer;

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

    public PreciseLocation check(NoCheatPlayer player, MovingData moving, CCMoving ccmoving) {

        final PreciseLocation setBack = moving.runflySetBackPoint;
        final PreciseLocation from = moving.from;
        final PreciseLocation to = moving.to;

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
        double speedLimitHorizontal = player.getPlayer().getGameMode() == GameMode.CREATIVE ? Math.max(creativeSpeed, ccmoving.flyingSpeedLimitHorizontal) : ccmoving.flyingSpeedLimitHorizontal;

        EntityPlayer p = ((CraftPlayer) player).getHandle();

        if(p.hasEffect(MobEffectList.FASTER_MOVEMENT)) {
            // Taken directly from Minecraft code, should work
            speedLimitHorizontal *= 1.0F + 0.2F * (float) (p.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier() + 1);
        }

        result += Math.max(0.0D, horizontalDistance - moving.horizFreedom - speedLimitHorizontal);

        boolean sprinting = player.getPlayer().isSprinting();

        moving.bunnyhopdelay--;

        // Did he go too far?
        if(result > 0 && sprinting) {

            // Try to treat it as a the "bunnyhop" problem
            if(moving.bunnyhopdelay <= 0 && result < 0.4D) {
                moving.bunnyhopdelay = 3;
                result = 0;
            }
        }

        // super simple, just check distance compared to max distance
        result += Math.max(0.0D, yDistance - moving.vertFreedom - ccmoving.flyingSpeedLimitVertical);
        result = result * 100;

        if(result > 0) {

            // Increment violation counter
            moving.runflyViolationLevel += result;

            boolean cancel = executeActions(player, ccmoving.flyingActions.getActions(moving.runflyViolationLevel));

            // Was one of the actions a cancel? Then really do it
            if(cancel) {
                newToLocation = setBack;
            }
        }

        // Slowly reduce the level with each event
        moving.runflyViolationLevel *= 0.97;

        // Some other cleanup 'n' stuff
        if(newToLocation == null) {
            setBack.set(to);
        }

        return newToLocation;
    }

    @Override
    public boolean isEnabled(CCMoving moving) {
        // TODO Auto-generated method stub
        return moving.allowFlying && moving.runflyCheck;
    }
    
    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case VIOLATIONS:
            return String.format(Locale.US, "%d", player.getData().moving.runflyViolationLevel);
        default:
            return super.getParameter(wildcard, player);
        }
    }
}
