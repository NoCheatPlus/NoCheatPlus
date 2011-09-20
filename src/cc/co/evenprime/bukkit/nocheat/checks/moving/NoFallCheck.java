package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.HashMap;
import java.util.Locale;

import net.minecraft.server.DamageSource;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutorWithHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
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
    private final NoCheat plugin;

    public NoFallCheck(NoCheat plugin) {
        this.action = new ActionExecutorWithHistory(plugin);
        this.plugin = plugin;
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
       
        // We want to know if the fallDistance recorded by the game is smaller
        // than the fall distance recorded by the plugin
        float distance = data.fallDistance - player.getFallDistance();
        
        if(distance > 0.01 && toOnOrInGround && data.fallDistance > 2.0F) {
            data.nofallViolationLevel += distance;
            // Prepare some event-specific values for logging and custom actions
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(LogAction.DISTANCE, String.format(Locale.US, "%.2f", data.fallDistance));
            params.put(LogAction.CHECK, "nofall");

            boolean cancel = action.executeActions(player, cc.moving.nofallActions, (int) data.nofallViolationLevel, params, cc);

            // If "cancelled", the fall damage gets dealt in a way that's visible to other players
            if(cancel) {
                // Increase the damage a bit :)
                float totalDistance = (data.fallDistance - 2.0F)* cc.moving.nofallMultiplier + 2.0F;
                player.setFallDistance(totalDistance);
            }
            
            data.fallDistance = 0F;
        }

        // Reduce falldamage violation level
        data.nofallViolationLevel *= 0.99D;

        // Increase the fall distance that is recorded by the plugin
        if(!toOnOrInGround && oldY > newY) {
            data.fallDistance += (float) (oldY - newY);
        }
    }
}
