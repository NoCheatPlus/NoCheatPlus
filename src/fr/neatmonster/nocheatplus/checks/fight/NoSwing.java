package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * M"""""""`YM          MP""""""`MM            oo                   
 * M  mmmm.  M          M  mmmmm..M                                 
 * M  MMMMM  M .d8888b. M.      `YM dP  dP  dP dP 88d888b. .d8888b. 
 * M  MMMMM  M 88'  `88 MMMMMMM.  M 88  88  88 88 88'  `88 88'  `88 
 * M  MMMMM  M 88.  .88 M. .MMM'  M 88.88b.88' 88 88    88 88.  .88 
 *  * M  MMMMM  M `88888P' Mb.     .dM 8888P Y8P  dP dP    dP `8888P88 
 * MMMMMMMMMMM          MMMMMMMMMMM                             .88 
 *                                                          d8888P  
 */
/**
 * We require that the player moves his arm between attacks, this is basically what gets checked here.
 */
public class NoSwing extends Check {

    /**
     * The event triggered by this check.
     */
    public class NoSwingEvent extends CheckEvent {

        /**
         * Instantiates a new no swing event.
         * 
         * @param player
         *            the player
         */
        public NoSwingEvent(final Player player) {
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

        // Did he swing his arm before?
        if (data.noSwingArmSwung) {
            // Yes, reward him with reduction of his violation level.
            data.noSwingArmSwung = false;
            data.noSwingVL *= 0.9D;
        } else {
            // No, increase his violation level.
            data.noSwingVL += 1D;

            // Dispatch a no swing event (API).
            final NoSwingEvent e = new NoSwingEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = !e.isCancelled() && executeActions(player, cc.noSwingActions, data.noSwingVL);
        }

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(FightData.getData(player).noSwingVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.FIGHT_NOSWING) && FightConfig.getConfig(player).noSwingCheck;
    }
}
