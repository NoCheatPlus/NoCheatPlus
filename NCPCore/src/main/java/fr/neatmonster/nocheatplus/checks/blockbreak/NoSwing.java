package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * We require that the player moves their arm between block breaks, this is what gets checked here.
 */
public class NoSwing extends Check {

    /**
     * Instantiates a new no swing check.
     */
    public NoSwing() {
        super(CheckType.BLOCKBREAK_NOSWING);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player, final BlockBreakData data) {

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
            cancel = executeActions(player, data.noSwingVL, 1D, BlockBreakConfig.getConfig(player).noSwingActions).willCancel();
        }

        return cancel;
    }
}
