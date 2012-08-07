package fr.neatmonster.nocheatplus.checks.blockinteract;

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
 * M  MMMMM  M `88888P' Mb.     .dM 8888P Y8P  dP dP    dP `8888P88 
 * MMMMMMMMMMM          MMMMMMMMMMM                             .88 
 *                                                          d8888P  
 */
/**
 * We require that the player moves his arm between block breaks, this is what gets checked here.
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
        final BlockInteractConfig cc = BlockInteractConfig.getConfig(player);
        final BlockInteractData data = BlockInteractData.getData(player);

        boolean cancel = false;

        if (System.currentTimeMillis() - data.noSwingLastTime > 3L)
            // Did he swing his arm before?
            if (data.noSwingArmSwung) {
                // "Consume" the flag.
                data.noSwingArmSwung = false;
                // Reward with lowering of the violation level.
                data.noSwingVL *= 0.9D;
            } else {
                // He failed, increase violation level.
                data.noSwingVL += 1D;

                // Dispatch a no swing event (API).
                final NoSwingEvent e = new NoSwingEvent(player);
                Bukkit.getPluginManager().callEvent(e);

                // Execute whatever actions are associated with this check and the violation level and find out if we
                // should
                // cancel the event.
                cancel = !e.isCancelled() && executeActions(player, cc.noSwingActions, data.noSwingVL);
            }

        data.noSwingLastTime = System.currentTimeMillis();

        return cancel;

    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(BlockInteractData.getData(player).noSwingVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.BLOCKINTERACT_NOSWING)
                && BlockInteractConfig.getConfig(player).noSwingCheck;
    }
}
