package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * MP""""""`MM                                  dP 
 * M  mmmmm..M                                  88 
 * M.      `YM 88d888b. .d8888b. .d8888b. .d888b88 
 * MMMMMMM.  M 88'  `88 88ooood8 88ooood8 88'  `88 
 * M. .MMM'  M 88.  .88 88.  ... 88.  ... 88.  .88 
 * Mb.     .dM 88Y888P' `88888P' `88888P' `88888P8 
 * MMMMMMMMMMM 88                                  
 *             dP                                  
 */
/**
 * This check verifies if the player isn't throwing items too quickly, like eggs or arrows.
 */
public class Speed extends Check {

    /**
     * The event triggered by this check.
     */
    public class SpeedEvent extends CheckEvent {

        /**
         * Instantiates a new speed event.
         * 
         * @param player
         *            the player
         */
        public SpeedEvent(final Player player) {
            super(player);
        }
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
        final BlockPlaceConfig cc = BlockPlaceConfig.getConfig(player);
        final BlockPlaceData data = BlockPlaceData.getData(player);

        boolean cancel = false;

        // Has the player thrown items too quickly?
        if (data.speedLastTime != 0 && System.currentTimeMillis() - data.speedLastTime < cc.speedInterval) {
            if (data.speedLastRefused) {
                // He failed, increase this violation level.
                data.speedVL += cc.speedInterval - System.currentTimeMillis() + data.speedLastTime;

                // Dispatch a speed event (API).
                final SpeedEvent e = new SpeedEvent(player);
                Bukkit.getPluginManager().callEvent(e);

                // Execute whatever actions are associated with this check and the violation level and find out if we
                // should cancel the event.
                cancel = !e.isCancelled() && executeActions(player, cc.speedActions, data.speedVL);
            }

            data.speedLastRefused = true;
        } else {
            // Reward him by lowering his violation level.
            data.speedVL *= 0.9D;

            data.speedLastRefused = false;
        }

        data.speedLastTime = System.currentTimeMillis();

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(BlockPlaceData.getData(player).speedVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.BLOCKPLACE_SPEED) && BlockPlaceConfig.getConfig(player).speedCheck;
    }
}
