package fr.neatmonster.nocheatplus.checks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.MCAccessHolder;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.ExecutionHistory;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * The parent class of all checks. Don't let this implement Listener without knowing that this might be registered as component with NCP before the check-listeners.
 */
public abstract class Check implements MCAccessHolder {

    // TODO: Do these get cleaned up ?
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
        if (!histories.containsKey(player.getName())) {
            histories.put(player.getName(), new ExecutionHistory());
        }
        return histories.get(player.getName());
    }

    /** The type. */
    protected final CheckType type;

    protected MCAccess mcAccess;

    /**
     * Instantiates a new check.
     * 
     * @param type
     *            the type
     */
    public Check(final CheckType type) {
        this.type = type;
        mcAccess = NCPAPIProvider.getNoCheatPlusAPI().getMCAccess();
        ViolationHistory.checkTypeMap.put(getClass().getName(), type);
        DataManager.registerExecutionHistory(type, histories);
    }

    /**
     * Execute actions, thread-safe.<br>
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
    public boolean executeActions(final Player player, final double vL, final double addedVL, final ActionList actions) {
        // Sync it into the main thread by using an event.
        return executeActions(new ViolationData(this, player, vL, addedVL, actions));
    }

    /**
     * Execute actions, thread safe.
     * 
     * @param violationData
     *            the violation data
     * @return true, if the event should be cancelled
     */
    public boolean executeActions(final ViolationData violationData) {

        // Dispatch the VL processing to the hook manager (now thread safe).
        if (NCPHookManager.shouldCancelVLProcessing(violationData)) {
            // One of the hooks has decided to cancel the VL processing, return false.
            return false;
        }

        final boolean hasCancel = violationData.hasCancel(); 

        if (Bukkit.isPrimaryThread()) {
            return violationData.executeActions();
        }
        else {
            // Always schedule to add to ViolationHistory.
            TickTask.requestActionsExecution(violationData);
        }

        // (Design change: Permission checks are moved to cached permissions, lazily updated.)
        return hasCancel;
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
     * Checks both configuration flags and if the player is exempted from this
     * check (hasBypass).
     * 
     * @param player
     *            the player
     * @return true, if the check is enabled
     */
    public boolean isEnabled(final Player player) {
        if (!type.isEnabled(player)) {
            return false;
        }
        return !hasBypass(player);
    }

    /**
     * Check if the player is exempted by permissions or otherwise.
     * 
     * @param player
     * @return
     */
    public boolean hasBypass(final Player player) {
        // TODO: Checking for the thread might be a temporary measure.
        if (Bukkit.isPrimaryThread()) {
            // Check permissions directly.
            final String permission = type.getPermission();
            if (permission != null && player.hasPermission(permission)) {
                return true;
            }
        }
        else if (type.hasCachedPermission(player)) {
            // Assume asynchronously running check.
            return true;
        }
        // TODO: ExemptionManager relies on initial setup (problematic).
        return !NCPExemptionManager.isExempted(player, type);
    }

    @Override
    public void setMCAccess(MCAccess mcAccess) {
        this.mcAccess = mcAccess;
    }

    @Override
    public MCAccess getMCAccess() {
        return mcAccess;
    }

}
