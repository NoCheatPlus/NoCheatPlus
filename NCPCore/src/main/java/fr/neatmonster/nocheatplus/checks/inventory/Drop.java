package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * The Drop check will find out if a player drops too many items within a short amount of time.
 */
public class Drop extends Check {

    /**
     * Instantiates a new drop check.
     */
    public Drop() {
        super(CheckType.INVENTORY_DROP);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
    	// Take time once.
    	final long time = System.currentTimeMillis();
    	
        final InventoryConfig cc = InventoryConfig.getConfig(player);
        final InventoryData data = InventoryData.getData(player);

        boolean cancel = false;

        // Has the configured time passed? If so, reset the counter.
        if (data.dropLastTime + cc.dropTimeFrame <= time) {
            data.dropLastTime = time;
            data.dropCount = 0;
            data.dropVL = 0D;
        }

        // Security check, if the system time changes.
        else if (data.dropLastTime > time)
            data.dropLastTime = Integer.MIN_VALUE;

        data.dropCount++;

        // The player dropped more than they should.
        if (data.dropCount > cc.dropLimit) {
            // Set their violation level.
            data.dropVL = data.dropCount - cc.dropLimit;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.dropVL, data.dropCount - cc.dropLimit, cc.dropActions);
        }

        return cancel;
    }
}
