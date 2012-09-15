package fr.neatmonster.nocheatplus.checks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.metrics.MetricsData;
import fr.neatmonster.nocheatplus.players.ExecutionHistory;
import fr.neatmonster.nocheatplus.utilities.TickTask;

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
        ViolationHistory.checkTypeMap.put(getClass().getName(), type);
    }
    
    /**
     * Execute actions, possibly thread safe according to the isMainThread flag.<br>
     * This does not use extra synchronization.
     * 
     * @param player
     *            the player
     * @param VL
     *            the vL
     * @param actions
     *            the actions
     * @param isMainThread
     *            if the thread the main thread
     * @return true, if successful
     */
    public boolean executeActions(final Player player, final double vL, final double addedVL,
            final ActionList actions, boolean isMainThread) {
        // Sync it into the main thread by using an event.
        return executeActions(new ViolationData(this, player, vL, addedVL, actions), isMainThread);
    }

    /**
     * Convenience method: Execute actions from the main thread only.
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
        return executeActions(new ViolationData(this, player, vL, addedVL, actions), true);
    }
    
    /**
     * Execute some actions for the specified player, only for the main thread.
     * 
     * @param violationData
     *            the violation data
     * @return true, if the event should be cancelled
     */
    protected boolean executeActions(final ViolationData violationData){
    	return executeActions(violationData, true);
    }

    /**
     * Execute some actions for the specified player.
     * 
     * @param violationData
     *            the violation data
     * @param isMainThread If this is called from within the main thread. If true, this must really be the main thread and not from synchronized code coming form another thread.
     * @return true, if the event should be cancelled
     */
    protected boolean executeActions(final ViolationData violationData, final boolean isMainThread) {
    	
    	// Dispatch the VL processing to the hook manager (now thread safe).
        if (NCPHookManager.shouldCancelVLProcessing(violationData))
            // One of the hooks has decided to cancel the VL processing, return false.
            return false;
        
        // Add this failed check to the Metrics data (async!).
        MetricsData.addFailed(violationData.check.type);
   
        final boolean hasCancel = violationData.hasCancel(); 
    	
        if (isMainThread) 
        	return violationData.executeActions();
        else 
        	// Always schedule to add to ViolationHistory.
        	TickTask.requestActionsExecution(violationData);
        
    	// (Design change: Permission checks are moved to cached permissions, lazily updated.)
    	return hasCancel;
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
            if (!type.isEnabled(player) || player.hasPermission(type.getPermission()))
                return false;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return !NCPExemptionManager.isExempted(player, type);
    }
}
