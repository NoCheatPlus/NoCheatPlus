package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;

/**
 * A check to see if people cheat by tricking the server to not deal them
 * fall damage.
 * 
 */
public class NoFallCheck extends MovingCheck {

    public NoFallCheck(NoCheat plugin) {
        super(plugin, "moving.nofall", Permissions.MOVING_NOFALL);
    }

    /**
     * Calculate if and how much the player "failed" this check.
     * 
     */
    public PreciseLocation check(NoCheatPlayer player, MovingData data, MovingConfig cc) {

        // If the player is serverside in creative mode, we have to stop here to
        // avoid hurting him when he switches back to "normal" mode
        if(player.isCreative()) {
            data.fallDistance = 0F;
            data.lastAddedFallDistance = 0F;
            return null;
        }

        // This check is pretty much always a step behind for technical reasons.
        if(data.fromOnOrInGround) {
            // Start with zero fall distance
            data.fallDistance = 0F;
        }

        // If we increased fall height before for no good reason, reduce now by
        // the same amount
        if(player.getPlayer().getFallDistance() > data.lastAddedFallDistance) {
            player.getPlayer().setFallDistance(player.getPlayer().getFallDistance() - data.lastAddedFallDistance);
        }

        data.lastAddedFallDistance = 0;

        // We want to know if the fallDistance recorded by the game is smaller
        // than the fall distance recorded by the plugin
        final float difference = data.fallDistance - player.getPlayer().getFallDistance();

        if(difference > 1.0F && data.toOnOrInGround && data.fallDistance > 2.0F) {
            data.nofallVL += difference;
            data.nofallTotalVL += difference;
            data.nofallFailed++;

            final boolean cancel = executeActions(player, cc.nofallActions.getActions(data.nofallVL));

            // If "cancelled", the fall damage gets dealt in a way that's
            // visible to other plugins
            if(cancel) {
                // Increase the fall distance a bit :)
                final float totalDistance = data.fallDistance + difference * (cc.nofallMultiplier - 1.0F);

                player.getPlayer().setFallDistance(totalDistance);
            }

            data.fallDistance = 0F;
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

        final double oldY = data.from.y;
        final double newY = data.to.y;

        if(oldY > newY) {
            final float dist = (float) (oldY - newY);
            data.fallDistance += dist;

            if(dist > 1.0F) {
                data.lastAddedFallDistance = dist;
                player.getPlayer().setFallDistance(player.getPlayer().getFallDistance() + dist);
            } else {
                data.lastAddedFallDistance = 0.0F;
            }
        } else {
            data.lastAddedFallDistance = 0.0F;
        }

        // Reduce falldamage violation level
        data.nofallVL *= 0.99D;

        return null;
    }

    @Override
    public boolean isEnabled(MovingConfig moving) {
        return moving.nofallCheck;
    }

    @Override
    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).nofallVL);
        else if(wildcard == ParameterName.FALLDISTANCE)
            return String.format(Locale.US, "%.2f", getData(player.getDataStore()).fallDistance);
        else
            return super.getParameter(wildcard, player);
    }
}
