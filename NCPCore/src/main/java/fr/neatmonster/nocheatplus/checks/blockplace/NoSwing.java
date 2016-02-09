package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * We require that the player moves their arm between block places, this is what gets checked here.
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
     * @param cc 
     * @return true, if successful
     */
    public boolean check(final Player player, final BlockPlaceData data, final BlockPlaceConfig cc) {

        boolean cancel = false;

        // Did they swing their arm before?
        if (data.noSwingArmSwung) {
            // "Consume" the flag.
            data.noSwingArmSwung = false;
            // Reward with lowering of the violation level.
            data.noSwingVL *= 0.9D;
        } else {
            // They failed, increase violation level.
            data.noSwingVL += 1D;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.noSwingVL, 1D, cc.noSwingActions).willCancel();
        }

        return cancel;
    }
}
