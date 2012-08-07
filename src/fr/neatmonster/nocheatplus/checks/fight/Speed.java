package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;

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
 * The Speed check is used to detect players who are attacking entities too quickly.
 */
public class Speed extends Check {

    /**
     * Instantiates a new speed check.
     */
    public Speed() {
        super(CheckType.FIGHT_SPEED);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        boolean cancel = false;

        // Has one second passed? Reset counters and violation level in that case.
        if (data.speedTime + 1000L <= System.currentTimeMillis()) {
            data.speedTime = System.currentTimeMillis();
            data.speedAttacks = 0;
            data.speedVL = 0D;
        }

        // Count the attack.
        data.speedAttacks++;

        // Too many attacks?
        if (data.speedAttacks > cc.speedLimit) {
            // If there was lag, don't count it towards violation level.
            if (!LagMeasureTask.skipCheck())
                data.speedVL += 1;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = executeActions(player);
        }

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.LIMIT)
            return String.valueOf(Math.round(FightConfig.getConfig(player).speedLimit));
        else
            return super.getParameter(wildcard, player);
    }
}
