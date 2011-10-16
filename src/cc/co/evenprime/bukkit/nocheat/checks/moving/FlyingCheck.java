package cc.co.evenprime.bukkit.nocheat.checks.moving;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * A check designed for people that are allowed to fly. The complement to
 * the "RunningCheck", which is for people that aren't allowed to fly, and
 * therefore have tighter rules to obey.
 * 
 * @author Evenprime
 * 
 */
public class FlyingCheck {

    private final NoCheat       plugin;

    private static final double creativeSpeed = 0.60D;

    public FlyingCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public Location check(Player player, Location from, Location to, ConfigurationCache cc) {

        BaseData data = plugin.getPlayerData(player);

        if(data.moving.runflySetBackPoint == null) {
            data.moving.runflySetBackPoint = player.getLocation().clone();
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

        result += Math.max(0.0D, horizontalDistance - data.moving.horizFreedom - speedLimitHorizontal);

        boolean sprinting = true;

        try {
            sprinting = !(player instanceof CraftPlayer) || player.isSprinting();
        } catch(Exception e) {
            e.printStackTrace();
        }

        data.moving.bunnyhopdelay--;

        // Did he go too far?
        if(result > 0 && sprinting) {

            // Try to treat it as a the "bunnyhop" problem
            if(data.moving.bunnyhopdelay <= 0 && result < 0.4D) {
                data.moving.bunnyhopdelay = 3;
                result = 0;
            }
        }

        // super simple, just check distance compared to max distance
        result += Math.max(0.0D, yDistance - data.moving.vertFreedom - cc.moving.flyingSpeedLimitVertical);
        result = result * 100;

        if(result > 0) {

            // Increment violation counter
            data.moving.runflyViolationLevel += result;

            data.log.toLocation = to;
            data.log.check = "flying/toofast";

            boolean cancel = plugin.getActionManager().executeActions(player, cc.moving.flyingActions, (int) data.moving.runflyViolationLevel, data.moving.history, cc);

            // Was one of the actions a cancel? Then really do it
            if(cancel) {
                newToLocation = data.moving.runflySetBackPoint;
            }
        }

        // Slowly reduce the level with each event
        data.moving.runflyViolationLevel *= 0.97;

        // Some other cleanup 'n' stuff
        if(newToLocation == null) {
            data.moving.runflySetBackPoint = to.clone();
        }

        return newToLocation;
    }
}
