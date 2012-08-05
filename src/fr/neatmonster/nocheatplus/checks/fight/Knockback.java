package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;

/*
 * M""MMMMM""M                            dP       dP                         dP       
 * M  MMMM' .M                            88       88                         88       
 * M       .MM 88d888b. .d8888b. .d8888b. 88  .dP  88d888b. .d8888b. .d8888b. 88  .dP  
 * M  MMMb. YM 88'  `88 88'  `88 88'  `"" 88888"   88'  `88 88'  `88 88'  `"" 88888"   
 * M  MMMMb  M 88    88 88.  .88 88.  ... 88  `8b. 88.  .88 88.  .88 88.  ... 88  `8b. 
 * M  MMMMM  M dP    dP `88888P' `88888P' dP   `YP 88Y8888' `88888P8 `88888P' dP   `YP 
 * MMMMMMMMMMM                                                                         
 */
/**
 * A check used to verify if players aren't "knockbacking" other players when it's not technically possible.
 */
public class Knockback extends Check {

    /**
     * The event triggered by this check.
     */
    public class KnockbackEvent extends CheckEvent {

        /**
         * Instantiates a new knockback event.
         * 
         * @param player
         *            the player
         */
        public KnockbackEvent(final Player player) {
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
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        boolean cancel = false;

        // How long ago has the player started sprinting?
        if (data.knockbackSprintTime > 0L
                && System.currentTimeMillis() - data.knockbackSprintTime < cc.knockbackInterval) {

            // Player failed the check, but this is influenced by lag, so don't do it if there was lag.
            if (!LagMeasureTask.skipCheck())
                // Increment the violation level
                data.knockbackVL += cc.knockbackInterval - System.currentTimeMillis() + data.knockbackSprintTime;

            // Dispatch a knockback event (API).
            final KnockbackEvent e = new KnockbackEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = !e.isCancelled() && executeActions(player, cc.knockbackActions, data.knockbackVL);
        }

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(FightData.getData(player).knockbackVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.FIGHT_KNOCKBACK) && FightConfig.getConfig(player).knockbackCheck;
    }
}
