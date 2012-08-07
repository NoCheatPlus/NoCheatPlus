package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/*
 * M""M                     dP                       dP   M#"""""""'M                      
 * M  M                     88                       88   ##  mmmm. `M                     
 * M  M 88d888b. .d8888b. d8888P .d8888b. 88d888b. d8888P #'        .M .d8888b. dP  dP  dP 
 * M  M 88'  `88 Y8ooooo.   88   88'  `88 88'  `88   88   M#  MMMb.'YM 88'  `88 88  88  88 
 * M  M 88    88       88   88   88.  .88 88    88   88   M#  MMMM'  M 88.  .88 88.88b.88' 
 * M  M dP    dP `88888P'   dP   `88888P8 dP    dP   dP   M#       .;M `88888P' 8888P Y8P  
 * MMMM                                                   M#########M                      
 */
/**
 * The InstantBow check will find out if a player pulled the string of his bow too fast.
 */
public class InstantBow extends Check {

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
    public boolean check(final Player player, final float force) {
        InventoryConfig.getConfig(player);
        final InventoryData data = InventoryData.getData(player);

        boolean cancel = false;

        // Rough estimation of how long pulling the string should've taken.
        final long expectedTimeWhenStringDrawn = data.instantBowLastTime + (int) (force * force * 700F);

        if (expectedTimeWhenStringDrawn < System.currentTimeMillis())
            // The player was slow enough, reward him by lowering his violation level.
            data.instantBowVL *= 0.9D;
        else if (data.instantBowLastTime > System.currentTimeMillis())
            // Security check if time ran backwards, reset
            data.instantBowLastTime = 0L;
        else {
            // Player was too fast, increase his violation level.
            data.instantBowVL += (expectedTimeWhenStringDrawn - System.currentTimeMillis()) / 100D;

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player);
        }

        return cancel;
    }
}
