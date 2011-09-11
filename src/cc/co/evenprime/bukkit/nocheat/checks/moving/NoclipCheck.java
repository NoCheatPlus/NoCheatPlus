package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.HashMap;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutorWithHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * A simple NoClip check. It tries to identify players that walk into/through
 * walls by remembering their last location and whenever they move into or
 * through a wall (with their upper body), this check should identify it.
 * EXPERIMENTAL!!
 * 
 * @author Evenprime
 * 
 */
public class NoclipCheck {

    private final double         bodyHeight = 1.1;
    private final ActionExecutor action;

    public NoclipCheck(NoCheat plugin) {
        this.action = new ActionExecutorWithHistory(plugin);
    }

    /**
     * Calculate if and how much the player "failed" this check. The check
     * should not modify any data
     * 
     */
    public Location check(final Player player, final Location from, final Location to, final MovingEventHelper helper, final ConfigurationCache cc, final MovingData data) {

        /*** THE CHECK ***/
        Location current = from.clone();

        final double distanceX = to.getX() - current.getX();
        final double distanceY = to.getY() - current.getY();
        final double distanceZ = to.getZ() - current.getZ();

        current.setY(current.getY() + bodyHeight);

        double distance = Math.abs(distanceX) > Math.abs(distanceY) ? (Math.abs(distanceX) > Math.abs(distanceZ) ? Math.abs(distanceX) : Math.abs(distanceZ)) : (Math.abs(distanceY) > Math.abs(distanceZ) ? Math.abs(distanceY) : Math.abs(distanceZ));
        int steps = (int) (distance / 0.1) + 1;

        double divideBy = distance * 10D;

        final double stepX = distanceX / divideBy;
        final double stepY = distanceY / divideBy;
        final double stepZ = distanceZ / divideBy;

        final World world = from.getWorld();

        int oldX, oldY, oldZ;
        int newX, newY, newZ;
        if(Math.abs(data.noclipX - current.getBlockX()) > 1 || Math.abs(data.noclipY - current.getBlockY()) > 1 || Math.abs(data.noclipZ - current.getBlockZ()) > 1) {
            oldX = newX = current.getBlockX();
            oldY = newY = current.getBlockY();
            oldZ = newZ = current.getBlockZ();
        } else {
            oldX = newX = data.noclipX;
            oldY = newY = data.noclipY;
            oldZ = newZ = data.noclipZ;
        }

        int violationLevel = 0;

        for(int i = 0; i < steps; i++) {

            newX = current.getBlockX();
            newY = current.getBlockY();
            newZ = current.getBlockZ();

            final boolean xChanged = newX != oldX;
            final boolean yChanged = newY != oldY;
            final boolean zChanged = newZ != oldZ;

            boolean failed = false;
            // Looks scarier than it is
            if(!failed && (xChanged || yChanged || zChanged)) {
                failed = check(helper, world, newX, newY, newZ);

                if(!failed && xChanged && (yChanged || zChanged)) {
                    failed = check(helper, world, oldX, newY, newZ);
                }

                if(!failed && yChanged && (xChanged || zChanged)) {
                    failed = check(helper, world, newX, oldY, newZ);
                }

                if(!failed && zChanged && (xChanged || yChanged)) {
                    failed = check(helper, world, newX, newY, oldZ);
                }

                oldX = newX;
                oldY = newY;
                oldZ = newZ;
            }

            // Determine if the block can be passed by the player
            if(failed) {
                violationLevel++;
            }

            current.add(stepX, stepY, stepZ);
        }

        if(violationLevel > 0) {
            // Prepare some event-specific values for logging and custom
            // actions
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(LogAction.DISTANCE, String.format(Locale.US, "%.2f,%.2f,%.2f", to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ()));
            params.put(LogAction.LOCATION_TO, String.format(Locale.US, "%.2f,%.2f,%.2f", to.getX(), to.getY(), to.getZ()));
            params.put(LogAction.CHECK, "noclip");

            /*boolean cancelled =*/ action.executeActions(player, cc.moving.noclipActions, violationLevel, params, cc);
            
            // TODO: UNCOMMENT, WHEN THE CHECK WORKS RELIABLY
            
            //if(cancelled) {
            //    return new Location(from.getWorld(), data.noclipX + 0.5, data.noclipY - ((int) bodyHeight), data.noclipZ + 0.5, to.getPitch(), to.getYaw());
            //}
        }

        // We didn't cancel the noclipping, so store the new location
        data.noclipX = newX;
        data.noclipY = newY;
        data.noclipZ = newZ;

        return null;

    }

    private final boolean check(MovingEventHelper helper, World world, int x, int y, int z) {
        if(y < 0 || y > 127) {
            return false;
        }
        return !helper.isNonSolid(helper.types[world.getBlockAt(x, y, z).getTypeId()]);
    }
}
