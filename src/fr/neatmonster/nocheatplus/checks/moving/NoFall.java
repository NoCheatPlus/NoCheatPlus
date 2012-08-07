package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;
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
     * The event triggered by this check.
     */
    public class NoFallEvent extends CheckEvent {

        /**
         * Instantiates a new no fall event.
         * 
         * @param player
         *            the player
         */
        public NoFallEvent(final Player player) {
            super(player);
        }
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

        // If the player is server-side in creative mode, we have to stop here to avoid hurting him when he switches
        // back to "normal" mode.
        if (player.getGameMode() == GameMode.CREATIVE || player.getAllowFlight()) {
            data.noFallDistance = 0F;
            data.noFallLastAddedDistance = 0F;
            return;
        }

        // This check is pretty much always a step behind for technical reasons.
        if (from.isInLiquid() || from.isOnGround() || from.isOnLadder())
            // Start with zero fall distance.
            data.noFallDistance = 0F;

        if (cc.noFallAggressive && (from.isInLiquid() || from.isOnGround() || from.isOnLadder())
                && (to.isInLiquid() || to.isOnGround() || to.isOnLadder()) && from.getY() <= to.getY()
                && player.getFallDistance() > 3F) {
            data.noFallDistance = player.getFallDistance();

            // Increment violation level.
            data.noFallVL += player.getFallDistance();

            // Dispatch a no fall event (API).
            final NoFallEvent e = new NoFallEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            if (!e.isCancelled() && executeActions(player, cc.noFallActions, data.noFallVL))
                // Deal fall damages to the player.
                ((CraftPlayer) player).getHandle().b(0D, true);
            data.noFallDistance = 0F;
        }

        // If we increased fall height before for no good reason, reduce now by the same amount.
        if (player.getFallDistance() > data.noFallLastAddedDistance)
            player.setFallDistance(player.getFallDistance() - data.noFallLastAddedDistance);

        data.noFallLastAddedDistance = 0F;

        final float difference = data.noFallDistance - player.getFallDistance();

        // We want to know if the fallDistance recorded by the game is smaller than the fall distance recorded by the
        // plugin.
        if (difference > 1F && (to.isInWater() || to.isOnGround() || to.isOnLadder()) && data.noFallDistance > 2F) {
            // Increment violation level.
            data.noFallVL += difference;

            // Dispatch a no fall event (API).
            final NoFallEvent e = new NoFallEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event. If "cancelled", the fall damage gets dealt in a way that's visible to other plugins.
            if (!e.isCancelled() && executeActions(player, cc.noFallActions, data.noFallVL))
                // Increase the fall distance a bit. :)
                player.setFallDistance(data.noFallDistance + difference);
            data.noFallDistance = 0F;
        }

        // Increase the fall distance that is recorded by the plugin, AND set the fall distance of the player to
        // whatever he would get with this move event. This modifies Minecrafts fall damage calculation slightly, but
        // that's still better than ignoring players that try to use "teleports" or "stepdown" to avoid falldamage. It
        // is only added for big height differences anyway, as to avoid to much deviation from the original Minecraft
        // feeling.
        if (from.getY() > to.getY()) {
            final float deltaY = (float) (from.getY() - to.getY());
            data.noFallDistance += deltaY;

            if (deltaY > 1F) {
                data.noFallLastAddedDistance = deltaY;
                player.setFallDistance(player.getFallDistance() + deltaY);
            } else
                data.noFallLastAddedDistance = 0F;
        } else
            data.noFallLastAddedDistance = 0F;

        // Reduce violation level.
        data.noFallVL *= 0.95D;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName,
     * org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(MovingData.getData(player).noFallVL));
        else if (wildcard == ParameterName.FALL_DISTANCE)
            return String.format(Locale.US, "%.2f", MovingData.getData(player).noFallDistance);
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.MOVING_NOFALL) && MovingConfig.getConfig(player).noFallCheck;
    }
}
