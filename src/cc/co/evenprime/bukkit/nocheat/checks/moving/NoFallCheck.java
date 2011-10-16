package cc.co.evenprime.bukkit.nocheat.checks.moving;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * A check to see if people cheat by tricking the server to not deal them
 * fall damage.
 * 
 * @author Evenprime
 * 
 */
public class NoFallCheck {

    private final NoCheat plugin;

    public NoFallCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    /**
     * Calculate if and how much the player "failed" this check.
     * 
     */
    public void check(final Player player, final Location from, final boolean fromOnOrInGround, final Location to, final boolean toOnOrInGround, final ConfigurationCache cc) {

        double oldY = from.getY();
        double newY = to.getY();

        BaseData data = plugin.getPlayerData(player);

        // This check is pretty much always a step behind for technical reasons.
        if(fromOnOrInGround) {
            // Start with zero fall distance
            data.moving.fallDistance = 0F;
        }

        // If we increased fall height before for no good reason, reduce now by
        // the same amount
        if(player.getFallDistance() > data.moving.lastAddedFallDistance) {
            player.setFallDistance(player.getFallDistance() - data.moving.lastAddedFallDistance);
        }

        data.moving.lastAddedFallDistance = 0;

        // We want to know if the fallDistance recorded by the game is smaller
        // than the fall distance recorded by the plugin
        float difference = data.moving.fallDistance - player.getFallDistance();

        if(difference > 1.0F && toOnOrInGround && data.moving.fallDistance > 2.0F) {
            data.moving.nofallViolationLevel += difference;

            // Prepare some event-specific values for logging and custom actions
            data.log.falldistance = data.moving.fallDistance;
            data.log.check = "moving/nofall";

            boolean cancel = plugin.getActionManager().executeActions(player, cc.moving.nofallActions, (int) data.moving.nofallViolationLevel, data.moving.history, cc);

            // If "cancelled", the fall damage gets dealt in a way that's
            // visible to other plugins
            if(cancel) {
                // Increase the fall distance a bit :)
                float totalDistance = data.moving.fallDistance + difference * (cc.moving.nofallMultiplier - 1.0F);

                player.setFallDistance(totalDistance);
            }

            data.moving.fallDistance = 0F;
        }

        // Increase the fall distance that is recorded by the plugin, AND set
        // the fall distance of the player
        // to whatever he would get with this move event. This modifies
        // Minecrafts fall damage calculation
        // slightly, but that's still better than ignoring players that try to
        // use "teleports" or "stepdown"
        // to avoid falldamage. It is only added for big height differences
        // anyway, as to avoid to much deviation
        // from the original Minecraft feeling.
        if(oldY > newY) {
            float dist = (float) (oldY - newY);
            data.moving.fallDistance += dist;

            if(dist > 1.0F) {
                data.moving.lastAddedFallDistance = dist;
                player.setFallDistance(player.getFallDistance() + dist);
            } else {
                data.moving.lastAddedFallDistance = 0.0F;
            }
        } else {
            data.moving.lastAddedFallDistance = 0.0F;
        }

        // Reduce falldamage violation level
        data.moving.nofallViolationLevel *= 0.99D;
    }
}
