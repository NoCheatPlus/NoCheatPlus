package fr.neatmonster.nocheatplus.checks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.metrics.MetricsData;
import fr.neatmonster.nocheatplus.players.ExecutionHistory;

/*
 * MM'""""'YMM dP                         dP       
 * M' .mmm. `M 88                         88       
 * M  MMMMMooM 88d888b. .d8888b. .d8888b. 88  .dP  
 * M  MMMMMMMM 88'  `88 88ooood8 88'  `"" 88888"   
 * M. `MMM' .M 88    88 88.  ... 88.  ... 88  `8b. 
 * MM.     .dM dP    dP `88888P' `88888P' dP   `YP 
 * MMMMMMMMMMM                                     
 */
/**
 * The parent class of all checks.
 */
public abstract class Check {

    /** The execution histories of each check. */
    protected static Map<String, ExecutionHistory> histories = new HashMap<String, ExecutionHistory>();

    /**
     * Gets the player's history.
     * 
     * @param player
     *            the player
     * @return the history
     */
    protected static ExecutionHistory getHistory(final Player player) {
        if (!histories.containsKey(player.getName()))
            histories.put(player.getName(), new ExecutionHistory());
        return histories.get(player.getName());
    }

    /** The type. */
    protected final CheckType type;

    /**
     * Instantiates a new check.
     * 
     * @param type
     *            the type
     */
    public Check(final CheckType type) {
        this.type = type;
    }

    /**
     * Convenience method.
     * 
     * @param player
     *            the player
     * @param vL
     *            the violation level
     * @param addedVL
     *            the violation level added
     * @param actions
     *            the actions
     * @return true, if the event should be cancelled
     */
    protected boolean executeActions(final Player player, final double vL, final double addedVL,
            final ActionList actions) {
        return executeActions(new ViolationData(this, player, vL, addedVL, actions));
    }

    /**
     * Execute some actions for the specified player.
     * 
     * @param violationData
     *            the violation data
     * @return true, if the event should be cancelled
     */
    protected boolean executeActions(final ViolationData violationData) {
        ViolationHistory.getHistory(violationData.player).log(getClass().getName(), violationData.addedVL);
        try {
            // Check a bypass permission.
            if (violationData.bypassPermission != null)
                if (violationData.player.hasPermission(violationData.bypassPermission))
                    return false;

            // Dispatch the VL processing to the hook manager.
            if (NCPHookManager.shouldCancelVLProcessing(violationData))
                // One of the hooks has decided to cancel the VL processing, return false.
                return false;

            // Add this failed check to the Metrics data.
            MetricsData.addFailed(type);

            final long time = System.currentTimeMillis() / 1000L;
            boolean cancel = false;
            for (final Action action : violationData.getActions())
                if (getHistory(violationData.player).executeAction(violationData, action, time))
                    // The execution history said it really is time to execute the action, find out what it is and do
                    // what is needed.
                    cancel = cancel || action.execute(violationData);

            return cancel;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Execute actions in a thread safe manner.
     * 
     * @param player
     *            the player
     * @param vL
     *            the violation level
     * @param addedVL
     *            the violation level added
     * @param actions
     *            the actions
     * @param bypassPermission
     *            the bypass permission
     * @return true, if the event should be cancelled
     */
    public boolean executeActionsThreadSafe(final Player player, final double vL, final double addedVL,
            final ActionList actions, final String bypassPermission) {
        // Sync it into the main thread by using an event.
        return executeActionsThreadSafe(new ViolationData(this, player, vL, addedVL, actions, bypassPermission));
    }

    /**
     * Execute actions in a thread safe manner.
     * 
     * @param violationData
     *            the violation data
     * @return true, if if the event should be cancelled
     */
    public boolean executeActionsThreadSafe(final ViolationData violationData) {
        final ExecuteActionsEvent event = new ExecuteActionsEvent(violationData);
        Bukkit.getPluginManager().callEvent(event);
        return event.getCancel();
    }

    /**
     * Replace a parameter for commands or log actions with an actual value. Individual checks should override this to
     * get their own parameters handled too.
     * 
     * @param wildcard
     *            the wildcard
     * @param violationData
     *            the violation data
     * @return the parameter
     */
    public String getParameter(final ParameterName wildcard, final ViolationData violationData) {
        if (wildcard == ParameterName.CHECK)
            return getClass().getSimpleName();
        else if (wildcard == ParameterName.PLAYER)
            return violationData.player.getName();
        else if (wildcard == ParameterName.VIOLATIONS) {
            try {
                return "" + Math.round(violationData.vL);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return "";
        } else
            return "The author was lazy and forgot to define " + wildcard + ".";
    }

    /**
     * Gets the type of the check.
     * 
     * @return the type
     */
    public CheckType getType() {
        return type;
    }

    /**
     * Checks if this check is enabled for the specified player.
     * 
     * @param player
     *            the player
     * @return true, if the check is enabled
     */
    public boolean isEnabled(final Player player) {
        try {
            return type.isEnabled(player) && !player.hasPermission(type.getPermission());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
