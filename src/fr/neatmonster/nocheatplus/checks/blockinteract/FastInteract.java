package fr.neatmonster.nocheatplus.checks.blockinteract;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;

/*
 * MM""""""""`M                     dP   M""M            dP                                         dP   
MM  mmmmmmmM                     88   M  M            88                                         88   
M'      MMMM .d8888b. .d8888b. d8888P M  M 88d888b. d8888P .d8888b. 88d888b. .d8888b. .d8888b. d8888P 
MM  MMMMMMMM 88'  `88 Y8ooooo.   88   M  M 88'  `88   88   88ooood8 88'  `88 88'  `88 88'  `""   88   
MM  MMMMMMMM 88.  .88       88   88   M  M 88    88   88   88.  ... 88       88.  .88 88.  ...   88   
MM  MMMMMMMM `88888P8 `88888P'   dP   M  M dP    dP   dP   `88888P' dP       `88888P8 `88888P'   dP   
MMMMMMMMMMMM                          MMMM                                                            
 */
/**
 * A check used to verify if the player isn't interacting with blocks too quickly.
 */
public class FastInteract extends Check {

    /**
     * The event triggered by this check.
     */
    public class FastInteractEvent extends CheckEvent {

        /**
         * Instantiates a new fast interact event.
         * 
         * @param player
         *            the player
         */
        public FastInteractEvent(final Player player) {
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

        // Has the player interacted with blocks too quickly?
        if (data.fastInteractLastTime != 0
                && System.currentTimeMillis() - data.fastInteractLastTime < cc.fastInteractInterval) {
            if (!LagMeasureTask.skipCheck()) {
                if (data.fastInteractLastRefused) {
                    // He failed, increase his violation level.
                    data.fastInteractVL += cc.fastInteractInterval - System.currentTimeMillis()
                            + data.fastInteractLastTime;

                    // Distance a fast interact event (API).
                    final FastInteractEvent e = new FastInteractEvent(player);
                    Bukkit.getPluginManager().callEvent(e);

                    // Execute whatever actions are associated with this check and the violation level and find out if
                    // we should cancel the event.
                    cancel = !e.isCancelled() && executeActions(player, cc.fastInteractActions, data.fastInteractVL);
                }

                data.fastInteractLastRefused = true;
            }
        } else {
            // Reward him by lowering his violation level.
            data.fastInteractVL *= 0.9D;
            data.fastInteractLastRefused = false;
        }

        data.fastInteractLastTime = System.currentTimeMillis();

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(BlockInteractData.getData(player).fastInteractVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.BLOCKINTERACT_FASTINTERACT)
                && BlockInteractConfig.getConfig(player).fastInteractCheck;
    }
}
