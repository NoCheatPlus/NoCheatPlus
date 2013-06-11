package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/*
 * M"""""""`YM          MP""""""`MM            oo                   
 * M  mmmm.  M          M  mmmmm..M                                 
 * M  MMMMM  M .d8888b. M.      `YM dP  dP  dP dP 88d888b. .d8888b. 
 * M  MMMMM  M 88'  `88 MMMMMMM.  M 88  88  88 88 88'  `88 88'  `88 
 * M  MMMMM  M 88.  .88 M. .MMM'  M 88.88b.88' 88 88    88 88.  .88 
 * M  MMMMM  M `88888P' Mb.     .dM 8888P Y8P  dP dP    dP `8888P88 
 * MMMMMMMMMMM          MMMMMMMMMMM                             .88 
 *                                                          d8888P  
 */
/**
 * We require that the player moves his arm between block places, this is what gets checked here.
 */
public class NoSwing extends Check {

    /**
     * Instantiates a new no swing check.
     */
    public NoSwing() {
        super(CheckType.BLOCKPLACE_NOSWING);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param data 
     * @return true, if successful
     */
    public boolean check(final Player player, final BlockPlaceData data) {

        boolean cancel = false;

        // Did he swing his arm before?
        if (data.noSwingArmSwung) {
            // "Consume" the flag.
            data.noSwingArmSwung = false;
            // Reward with lowering of the violation level.
            data.noSwingVL *= 0.9D;
        } else {
            // He failed, increase violation level.
            data.noSwingVL += 1D;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.noSwingVL, 1D, BlockPlaceConfig.getConfig(player).noSwingActions);
        }

        return cancel;
    }
}
