package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;

import net.minecraft.server.EntityPlayer;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutorWithHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * A check designed for people that are allowed to fly. The complement to
 * the "RunningCheck", which is for people that aren't allowed to fly, and
 * therefore have tighter rules to obey.
 * 
 * @author Evenprime
 * 
 */
public class FlyingCheck {

    private final ActionExecutor action;

    private static Method isRunningMethod;

    private static final double  creativeSpeed = 0.60D;

    public FlyingCheck(NoCheat plugin) {
        this.action = new ActionExecutorWithHistory(plugin);
    }

    public Location check(Player player, Location from, Location to, ConfigurationCache cc, MovingData data) {

        if(data.runflySetBackPoint == null) {
            data.runflySetBackPoint = player.getLocation().clone();
        }

        final double yDistance = to.getY() - from.getY();

        // Calculate some distances
        final double xDistance = to.getX() - from.getX();
        final double zDistance = to.getZ() - from.getZ();
        final double horizontalDistance = Math.sqrt((xDistance * xDistance + zDistance * zDistance));

        double result = 0;
        Location newToLocation = null;

        // In case of creative gamemode, give at least 0.60 speed limit
        // horizontal
        final double speedLimitHorizontal = player.getGameMode() == GameMode.CREATIVE ? Math.max(creativeSpeed, cc.moving.flyingSpeedLimitHorizontal) : cc.moving.flyingSpeedLimitHorizontal;

        result += Math.max(0.0D, horizontalDistance - data.horizFreedom - speedLimitHorizontal);

        if(isRunningMethod == null) {
            isRunningMethod = getIsRunningMethod();
        }
        
        
        boolean sprinting = true;
        
        try {
            sprinting = !(player instanceof CraftPlayer) || isRunningMethod.invoke(((CraftPlayer) player).getHandle()).equals(true);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
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
        result += Math.max(0.0D, yDistance - data.vertFreedom - cc.moving.flyingSpeedLimitVertical);
        result = result * 100;

        if(result > 0) {

            // Increment violation counter
            data.runflyViolationLevel += result;

            // Prepare some event-specific values for logging and custom actions
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(LogAction.DISTANCE, String.format(Locale.US, "%.2f,%.2f,%.2f", xDistance, yDistance, zDistance));
            params.put(LogAction.LOCATION_TO, String.format(Locale.US, "%.2f,%.2f,%.2f", to.getX(), to.getY(), to.getZ()));
            params.put(LogAction.CHECK, "flying/toofast");

            boolean cancel = action.executeActions(player, cc.moving.flyingActions, (int) data.runflyViolationLevel, params, cc);

            // Was one of the actions a cancel? Then really do it
            if(cancel) {
                newToLocation = data.runflySetBackPoint;
            }
        }

        // Slowly reduce the level with each event
        data.runflyViolationLevel *= 0.97;

        // Some other cleanup 'n' stuff
        if(newToLocation == null) {
            data.runflySetBackPoint = to.clone();
        }

        return newToLocation;
    }
    
    private Method getIsRunningMethod() {
        try {
            return EntityPlayer.class.getMethod("isSprinting");
        } catch(NoSuchMethodException e) {
            try {
                return EntityPlayer.class.getMethod("at");
            } catch(Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return null;
            }
        }
    }
}
