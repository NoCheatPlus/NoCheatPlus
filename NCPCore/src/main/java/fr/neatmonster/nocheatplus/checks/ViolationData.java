package fr.neatmonster.nocheatplus.checks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionData;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.CancelAction;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.logging.StaticLog;

/**
 * Violation specific data, for executing actions.<br>
 * This is meant to capture a violation incident in a potentially thread safe way.
 * <hr>
 * TODO: Re-think visibility questions.
 * @author asofold
 */
public class ViolationData implements IViolationInfo, ActionData{

    /** The actions to be executed. */
    public final ActionList actions;

    /** The actions applicable for the violation level. */
    public final Action<ViolationData, ActionList>[] applicableActions;

    /** The violation level added. */
    public final double     addedVL;

    /** The check. */
    public final Check      check;

    /** The player. */
    public final Player     player;

    /** The violation level. */
    public final double     vL;

    /** Filled in parameters. */
    private Map<ParameterName, String> parameters;

    private boolean needsParameters = false;

    /**
     * Instantiates a new violation data.
     * 
     * @param check
     *            the check
     * @param player
     *            the player
     * @param vL
     *            the violation level
     * @param addedVL
     *            the violation level added
     * @param actions
     *            the actions
     */
    public ViolationData(final Check check, final Player player, final double vL, final double addedVL,
            final ActionList actions) {
        this.check = check;
        this.player = player;
        this.vL = vL;
        this.addedVL = addedVL;
        this.actions = actions;
        this.applicableActions = actions.getActions(vL);
        boolean needsParameters = false;
        for (int i = 0; i < applicableActions.length; i++) {
            if (applicableActions[i].needsParameters()) {
                needsParameters = true;
                break;
            }
        }
        parameters = needsParameters ? check.getParameterMap(this) : null;
        this.needsParameters = needsParameters;
    }

    /**
     * Gets the actions.
     * 
     * @return the actions
     */
    public Action<ViolationData, ActionList> [] getActions() {
        return applicableActions;
    }

    /**
     * Execute actions and return if cancel. Does add it to history.
     * @return
     */
    public boolean executeActions() {
        try {
            // Statistics.
            ViolationHistory.getHistory(player).log(check.getClass().getName(), addedVL);
            // TODO: the time is taken here, which makes sense for delay, but otherwise ?
            final long time = System.currentTimeMillis() / 1000L;
            boolean cancel = false;
            for (final Action<ViolationData, ActionList>  action : getActions()) {
                if (Check.getHistory(player).executeAction(this, action, time)) {
                    // The execution history said it really is time to execute the action, find out what it is and do
                    // what is needed.
                    if (action.execute(this)) {
                        cancel = true;
                    }
                }
            }
            return cancel;
        } catch (final Exception e) {
            StaticLog.logSevere(e);
            // On exceptions cancel events.
            return true;
        }
    }

    /**
     * Check if the actions contain a cancel. 
     * @return
     */
    @Override
    public boolean hasCancel() {
        for (final Action<ViolationData, ActionList>  action : applicableActions) {
            if (action instanceof CancelAction) return true;
        }
        return false;
    }

    @Override
    public String getParameter(final ParameterName parameterName) {
        if (parameterName == null) {
            return "<???>";
        }
        switch (parameterName) {
        case CHECK:
            return check.getClass().getSimpleName();
        case IP:
            return player.getAddress().toString().substring(1).split(":")[0];
        case PLAYER:
        case PLAYER_NAME:
            return player.getName();
        case PLAYER_DISPLAY_NAME:
            return player.getDisplayName();
        case UUID:
            return player.getUniqueId().toString();
        case VIOLATIONS:
            return String.valueOf((long) Math.round(vL));
        default:
            break;
        }
        if (parameters == null) {
            // Return what would have been parsed to get the parameter.
            return "[" + parameterName.getText() + "]";
        }
        final String value = parameters.get(parameterName);
        return(value == null) ? ("[" + parameterName.getText() + "]") : value;
    }

    @Override
    public void setParameter(final ParameterName parameterName, String value) {
        if (parameters == null) {
            parameters = new HashMap<ParameterName, String>();
        }
        parameters.put(parameterName, value);
    }

    @Override
    public boolean needsParameters() {
        return needsParameters;
    }

    @Override
    public boolean hasParameters() {
        return parameters != null && !parameters.isEmpty();
    }

    @Override
    public double getAddedVl() {
        return addedVL;
    }

    @Override
    public double getTotalVl() {
        return vL;
    }

    public String getPermissionSilent() {
        return actions.permissionSilent;
    }

    public ActionList getActionList() {
        return actions;
    }
}
