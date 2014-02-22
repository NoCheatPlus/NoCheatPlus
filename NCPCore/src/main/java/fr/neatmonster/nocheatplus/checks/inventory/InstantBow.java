package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * The InstantBow check will find out if a player pulled the string of their bow too fast.
 */
public class InstantBow extends Check {
    
    private static final float maxTime = 800f;

    /**
     * Instantiates a new instant bow check.
     */
    public InstantBow() {
        super(CheckType.INVENTORY_INSTANTBOW);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param force
     *            the force
     * @return true, if successful
     */
    public boolean check(final Player player, final float force, final long now) {
    	
        final InventoryData data = InventoryData.getData(player);
        final InventoryConfig cc = InventoryConfig.getConfig(player);

        boolean cancel = false;

        // Rough estimation of how long pulling the string should've taken.
        final long expectedPullDuration = (long) (maxTime - maxTime * (1f - force) * (1f - force)) - cc.instantBowDelay;
        
        // Time taken to pull the string.
        final long pullDuration = now - (cc.instantBowStrict ? data.instantBowInteract : data.instantBowShoot);

        if ((!cc.instantBowStrict || data.instantBowInteract > 0) && pullDuration >= expectedPullDuration){
            // The player was slow enough, reward them by lowering their violation level.
            data.instantBowVL *= 0.9D;
        }
        else if (data.instantBowInteract > now){
            // Security check if time ran backwards.
            // TODO: Maybe this can be removed, though TickTask does not reset at the exact moment.
        }
        else {
        	// Account for server side lag.
        	final long correctedPullduration = cc.lag ? (long) (TickTask.getLag(expectedPullDuration, true) * pullDuration) : pullDuration;
        	if (correctedPullduration < expectedPullDuration){
                // TODO: Consider: Allow one time but set yawrate penalty time ?
                final double difference = (expectedPullDuration - pullDuration) / 100D;

                // Player was too fast, increase their violation level.
                data.instantBowVL += difference;

                // Execute whatever actions are associated with this check and the
                // violation level and find out if we should cancel the event
    			cancel = executeActions(player, data.instantBowVL, difference, cc.instantBowActions);
        	}
        }
        
        if (cc.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
            player.sendMessage(ChatColor.YELLOW + "NCP: " + ChatColor.GRAY + "Bow shot - force: " + force +", " + (cc.instantBowStrict || pullDuration < 2 * expectedPullDuration ? ("pull time: " + pullDuration) : "") + "(" + expectedPullDuration +")");
        }
        
        // Reset data here.
        data.instantBowInteract = 0;
        data.instantBowShoot = now;
        return cancel;
    }
}
