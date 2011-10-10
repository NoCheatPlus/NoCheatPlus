package cc.co.evenprime.bukkit.nocheat.checks.moving;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.LogData;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * A check to see if people cheat by tricking the server to not deal them
 * fall damage.
 * 
 * @author Evenprime
 * 
 */
public class NoFallCheck {

    private final ActionExecutor action;
    private final NoCheat        plugin;

    public NoFallCheck(NoCheat plugin) {
        this.plugin = plugin;
        this.action = new ActionExecutor(plugin);
    }

    /**
     * Calculate if and how much the player "failed" this check.
     * 
     */
    public void check(final Player player, final Location from, final boolean fromOnOrInGround, final Location to, final boolean toOnOrInGround, final ConfigurationCache cc, final MovingData data) {

        double oldY = from.getY();
        double newY = to.getY();

        // This check is pretty much always a step behind for technical reasons.
        if(fromOnOrInGround) {
            // Start with zero fall distance
            data.fallDistance = 0F;
        }

        // If we increased fall height before for no good reason, reduce now by the same amount
        if(player.getFallDistance() > data.lastAddedFallDistance) {
            player.setFallDistance(player.getFallDistance() - data.lastAddedFallDistance);
        }
        
        data.lastAddedFallDistance = 0;

        // We want to know if the fallDistance recorded by the game is smaller
        // than the fall distance recorded by the plugin
        float difference = data.fallDistance - player.getFallDistance();
        
        if(difference > 1.0F && toOnOrInGround && data.fallDistance > 2.0F) {
            data.nofallViolationLevel += difference;

            // Prepare some event-specific values for logging and custom actions
            LogData ldata = plugin.getDataManager().getLogData(player);
            ldata.falldistance = data.fallDistance;
            ldata.check = "moving/nofall";

            boolean cancel = action.executeActions(player, cc.moving.nofallActions, (int) data.nofallViolationLevel, ldata, cc);

            // If "cancelled", the fall damage gets dealt in a way that's visible to other plugins
            if(cancel) {
                // Increase the fall distance a bit :)
                float totalDistance = data.fallDistance + difference * (cc.moving.nofallMultiplier - 1.0F);

                player.setFallDistance(totalDistance);
            }

            data.fallDistance = 0F;
        }

        // Increase the fall distance that is recorded by the plugin, AND set the fall distance of the player
        // to whatever he would get with this move event. This modifies Minecrafts fall damage calculation
        // slightly, but that's still better than ignoring players that try to use "teleports" or "stepdown"
        // to avoid falldamage. It is only added for big height differences anyway, as to avoid to much deviation
        // from the original Minecraft feeling.
        if(oldY > newY) {
            float dist = (float) (oldY - newY);
            data.fallDistance += dist;
            
            if(dist > 1.0F) {
                data.lastAddedFallDistance = dist;
                player.setFallDistance(player.getFallDistance() + dist);
            }
            else {
                data.lastAddedFallDistance = 0.0F;
            }
        }
        else {
            data.lastAddedFallDistance = 0.0F;
        }
        
        // Reduce falldamage violation level
        data.nofallViolationLevel *= 0.99D;
    }
}
