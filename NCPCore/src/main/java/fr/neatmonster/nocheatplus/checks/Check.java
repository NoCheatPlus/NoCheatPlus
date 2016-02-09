package fr.neatmonster.nocheatplus.checks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.MCAccessHolder;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.ExecutionHistory;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * The parent class of all checks. Don't let this implement Listener without
 * knowing that this might be registered as component with NCP before the
 * check-listeners.
 * <hr>
 * Note on enabling and bypassing:<br>
 * <li>The full Check.isEnabled test will check the config flag
 * (config.isEnabled) and test Check.hasBypass (permissions, exemption).</li>
 * <li>Currently the code for isEnabled and hasBypasss is in
 * fr.neatmonster.nocheatplus.utilities.CheckUtils .</li>
 * <li>Currently the hasBypass check will test for a permission if in the main
 * thread, or test for a cached permission if off the main thread, then check
 * for exemption with fr.neatmonster.nocheatplus.hooks.NCPExemptionManager, for
 * the case that no bypass permission is present.</li>
 * <li>For checks run off the primary thread, permissions are cached. Updates
 * must be requested with the TickTask explicitly, e.g. in the listener. This
 * depends on the definition of which checks might run asynchronously, as given
 * in fr.neatmonster.nocheatplus.hooks.APIUtils and the default permissions
 * defined in ICheckConfig implementations.</li>
 * <li>At present exemption checking is only thread-safe for the checks that are
 * set to run off main thread (APIUtils).</li>
 * <br>
 * Note on performance:
 * <li>You might check the configuration flag directly with a given
 * configuration, which is equivalent to but supposedly faster than calling
 * CheckType.isEnabled(player) or CheckConfig.isEnabled(player). Then call
 * hasBypass extra to that.</li>
 * <li>For very simple checks, you might skip checking hasBypass for the normal
 * case, and check it lazily only in case of a violation.</li>
 * <li>The method signatures that take ICheckData and ICheckConfig as extra
 * arguments will perform better, they also allow to pass null for config and
 * data (also see fr.neatmonster.nocheatplus.utilities.CheckUtils).</li>
 * <li>In case simplicity of code is demanded, just check isEnabled(player) or
 * for better performance isEnabled(player, data, config), before running the
 * actual check.</li>
 * 
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
     * @return The ViolationData instance that is created for execution of
     *         actions.
     */
    public ViolationData executeActions(final Player player, final double vL, final double addedVL, final ActionList actions) {
        return executeActions(new ViolationData(this, player, vL, addedVL, actions));
    }

    /**
     * Execute actions, thread safe.
     * 
     * @param violationData
     *            the violation data
     * @return The ViolationData instanced passed to this method.
     */
    public ViolationData executeActions(final ViolationData violationData) {

        // Dispatch the VL processing to the hook manager (now thread safe).
        if (NCPHookManager.shouldCancelVLProcessing(violationData)) {
            // One of the hooks has decided to cancel the VL processing, return false.
            violationData.preventCancel();
        }
        else if (Bukkit.isPrimaryThread()) {
            violationData.executeActions();
        }
        else {
            // Always schedule to add to ViolationHistory.
            // TODO: Might clear input-specific effects (stored ones will be handled extra to those).
            TickTask.requestActionsExecution(violationData);
        }

        // (Permission checks are moved to cached permissions, lazily updated.)
        return violationData;
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
     * check (hasBypass). Intended for higher efficiency with multiple calls.
     * 
     * @param player
     * @param data
     * @param cc
     * @return
     */
    public boolean isEnabled(final Player player, final ICheckData data, final ICheckConfig cc) {
        return cc.isEnabled(type) && !CheckUtils.hasBypass(type, player, data);
    }

    /**
     * Checks both configuration flags and if the player is exempted from this
     * check (hasBypass). Intended for higher efficiency with multiple calls.
     * 
     * @param player
     * @param cc
     * @return
     */
    public boolean isEnabled(final Player player, final ICheckConfig cc) {
        return cc.isEnabled(type) && !CheckUtils.hasBypass(type, player, null);
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
        return type.isEnabled(player) && !CheckUtils.hasBypass(type, player, null);
    }

    /**
     * Check if the player is exempted by permissions or otherwise.<br>
     * 
     * 
     * @param player
     * @return
     */
    public boolean hasBypass(final Player player) {
        return CheckUtils.hasBypass(type, player, null);
    }

    @Override
    public void setMCAccess(MCAccess mcAccess) {
        this.mcAccess = mcAccess;
    }

    @Override
    public MCAccess getMCAccess() {
        return mcAccess;
    }

    /**
     * Output a message for a player with the standard format (see
     * CheckUtils.debug(Player, CheckType, String).
     * 
     * @param player
     *            Can be null.
     * @param message
     */
    public void debug(final Player player, final String message) {
        CheckUtils.debug(player, type, message);
    }

}
