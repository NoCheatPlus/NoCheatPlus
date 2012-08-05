package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * M""""""'YMM                            
 * M  mmmm. `M                            
 * M  MMMMM  M 88d888b. .d8888b. 88d888b. 
 * M  MMMMM  M 88'  `88 88'  `88 88'  `88 
 * M  MMMM' .M 88       88.  .88 88.  .88 
 * M       .MM dP       `88888P' 88Y888P' 
 * MMMMMMMMMMM                   88       
 *                               dP       
 */
/**
 * The Drop check will find out if a player drops too many items within a short amount of time.
 */
public class Drop extends Check {

    /**
     * The event triggered by this check.
     */
    public class DropEvent extends CheckEvent {

        /**
         * Instantiates a new drop event.
         * 
         * @param player
         *            the player
         */
        public DropEvent(final Player player) {
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
        final InventoryConfig cc = InventoryConfig.getConfig(player);
        final InventoryData data = InventoryData.getData(player);

        boolean cancel = false;

        // Has the configured time passed? If so, reset the counter.
        if (data.dropLastTime + cc.dropTimeFrame <= System.currentTimeMillis()) {
            data.dropLastTime = System.currentTimeMillis();
            data.dropCount = 0;
            data.dropVL = 0D;
        }

        // Security check, if the system time changes.
        else if (data.dropLastTime > System.currentTimeMillis())
            data.dropLastTime = Integer.MIN_VALUE;

        data.dropCount++;

        // The player dropped more than he should.
        if (data.dropCount > cc.dropLimit) {
            // Set his violation level.
            data.dropVL = data.dropCount - cc.dropLimit;

            // Dispatch a drop event (API).
            final DropEvent e = new DropEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = !e.isCancelled() && executeActions(player, cc.dropActions, data.dropVL);
        }

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(InventoryData.getData(player).dropVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.INVENTORY_DROP) && InventoryConfig.getConfig(player).dropCheck;
    }
}
