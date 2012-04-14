package me.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.checks.CheckUtil;
import me.neatmonster.nocheatplus.config.Permissions;
import me.neatmonster.nocheatplus.data.PreciseLocation;
import me.neatmonster.nocheatplus.data.Statistics.Id;

import org.bukkit.GameMode;
import org.bukkit.Material;

public class TrackerCheck extends MovingCheck {

    public TrackerCheck(final NoCheatPlus plugin) {
        super(plugin, "moving.tracker");
    }

    public void check(final NoCheatPlusPlayer player, final MovingData data, final MovingConfig cc) {

        final PreciseLocation location = new PreciseLocation();
        location.x = player.getPlayer().getLocation().getX();
        location.y = player.getPlayer().getLocation().getY();
        location.z = player.getPlayer().getLocation().getZ();
        final int type = CheckUtil.evaluateLocation(player.getPlayer().getWorld(), location);
        final boolean isLiquid = CheckUtil.isLiquid(type);

        // Check if the player is on the ground
        if (player.getPlayer().getLocation().add(0, -1, 0).getBlock().getType() != Material.AIR)
            data.hasAlreadyBeenOnTheGround = true;

        // Do not do the check if it's disabled, if flying is allowed, if the player is
        // allowed to fly because of its game mode, if he has the required permission,
        // if he is in water, on a ladder or in vines.
        if (!cc.tracker || cc.allowFlying || player.getPlayer().getGameMode() == GameMode.CREATIVE
                || player.getPlayer().getAllowFlight() || player.getPlayer().hasPermission(Permissions.MOVING_RUNFLY)
                || player.getPlayer().hasPermission(Permissions.MOVING_FLYING) || isLiquid
                || player.getPlayer().getLocation().getBlock().getType() == Material.LADDER
                || player.getPlayer().getLocation().getBlock().getType() == Material.VINE
                || player.getPlayer().getLocation().getY() < 0D) {
            data.fallingSince = 0L;
            return;
        }

        // If the player isn't static or jumping
        if (Math.abs(player.getPlayer().getVelocity().getY()) > 0.1D) {

            // Only do something if the player has already been on the ground
            if (!data.hasAlreadyBeenOnTheGround)
                return;

            // The player is static/jumping, check if he was previously on the ground
            if (data.fallingSince == 0L)
                data.fallingSince = System.currentTimeMillis();

            // Check if he has stayed too much time in the air (more than 6 seconds)
            else if (System.currentTimeMillis() - data.fallingSince > 6000L) {
                // He has, so increment the violation level and the statistics
                data.trackerVL += System.currentTimeMillis() - data.fallDistance - 6000L;
                incrementStatistics(player, Id.MOV_TRACKER, data.trackerVL);

                // Execute whatever actions are associated with this check and the
                // violation level and find out if we should cancel the event
                executeActions(player, cc.trackerActions, data.trackerVL);
            }
        } else {
            // Reset the timer
            data.fallingSince = 0L;

            // Reduce the violation level
            data.trackerVL *= 0.95;
        }
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).trackerVL);
        else
            return super.getParameter(wildcard, player);
    }
}
