package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * We require that the player moves their arm between attacks, this is basically what gets checked here.
 */
public class NoSwing extends Check {

    /**
     * Instantiates a new no swing check.
     */
    public NoSwing() {
        super(CheckType.FIGHT_NOSWING);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player, final FightData data, final FightConfig cc) {
        boolean cancel = false;

        // Did they swing his arm before?
        if (data.noSwingArmSwung) {
            // Yes, reward them with reduction of their violation level.
            data.noSwingArmSwung = false;
            data.noSwingVL *= 0.9D;
        } else {
            // No, increase their violation level.
            data.noSwingVL += 1D;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player, data.noSwingVL, 1D, cc.noSwingActions).willCancel();
        }

        return cancel;
    }

}
