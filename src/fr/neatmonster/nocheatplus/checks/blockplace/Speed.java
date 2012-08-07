package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

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
     * Instantiates a new speed check.
     */
    public Speed() {
        super(CheckType.BLOCKPLACE_SPEED);
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

                // Execute whatever actions are associated with this check and the violation level and find out if we
                // should cancel the event.
                cancel = executeActions(player);
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
}
