package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.CustomNetServerHandler;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/*
 * M"""""""`YM          MM""""""""`M          dP dP 
 * M  mmmm.  M          MM  mmmmmmmM          88 88 
 * M  MMMMM  M .d8888b. M'      MMMM .d8888b. 88 88 
 * M  MMMMM  M 88'  `88 MM  MMMMMMMM 88'  `88 88 88 
 * M  MMMMM  M 88.  .88 MM  MMMMMMMM 88.  .88 88 88 
 * M  MMMMM  M `88888P' MM  MMMMMMMM `88888P8 dP dP 
 * MMMMMMMMMMM          MMMMMMMMMMMM                
 */
/**
 * A check to see if people cheat by tricking the server to not deal them fall damage.
 */
public class NoFall extends Check {

    /**
     * Instantiates a new no fall check.
     */
    public NoFall() {
        super(CheckType.MOVING_NOFALL);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param from
     *            the from
     * @param to
     *            the to
     */
    public void check(final Player player, final PlayerLocation from, final PlayerLocation to) {
        final MovingConfig cc = MovingConfig.getConfig(player);
        final MovingData data = MovingData.getData(player);

        // Get the CustomNetServerHandler of the player.
        final CustomNetServerHandler customNSH = (CustomNetServerHandler) ((CraftPlayer) player).getHandle().netServerHandler;

        // If the player has just started falling, is falling into a liquid, in web or is on a ladder.
        if (to.isInLiquid() || to.isInWeb() || to.isOnLadder())
            // Reset his fall distance.
            customNSH.fallDistance = 0D;

        data.noFallFallDistance = customNSH.fallDistance;

        // If the player just touched the ground for the server, but no for the client.
        if (!customNSH.wasOnGroundServer && customNSH.onGroundServer
                && (customNSH.wasOnGroundClient || !customNSH.onGroundClient)) {
            // Calculate the fall damages to be dealt.
            final int fallDamage = (int) customNSH.fallDistance - 2;
            if (fallDamage > 0) {
                // Add the fall distance to the violation level.
                data.noFallVL += customNSH.fallDistance;

                // Execute the actions to find out if we need to cancel the event or not.
                if (executeActions(player, data.noFallVL, cc.noFallActions))
                    // Deal the fall damages to the player.
                    player.damage(fallDamage);
            }
        }

        // If the player just touched the ground for the server.
        else if (!customNSH.wasOnGroundServer && customNSH.onGroundServer) {
            // Calculate the difference between the fall distance calculated by the server and by the plugin.
            final double difference = (customNSH.fallDistance - player.getFallDistance()) / customNSH.fallDistance;

            // If the difference is too big and the fall distance calculated by the plugin should hurt the player.
            if (difference > 0.15D && (int) customNSH.fallDistance > 2) {
                // Add the difference to the violation level.
                data.noFallVL += customNSH.fallDistance - player.getFallDistance();

                // Execute the actions to find out if we need to cancel the event or not.
                if (executeActions(player, data.noFallVL, cc.noFallActions))
                    // Set the fall distance to its right value.
                    player.setFallDistance((float) customNSH.fallDistance);
            } else
                // Reward the player by lowering his violation level.
                data.noFallVL *= 0.95D;
        } else
            // Reward the player by lowering his violation level.
            data.noFallVL *= 0.95D;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName,
     * org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final ViolationData violationData) {
        if (wildcard == ParameterName.FALL_DISTANCE)
            return String.format(Locale.US, "%.2f", MovingData.getData(violationData.player).noFallFallDistance);
        else
            return super.getParameter(wildcard, violationData);
    }
}
