package fr.neatmonster.nocheatplus.checks;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.CancelAction;

/*
 * M""MMMMM""M oo          dP            dP   oo                   M""""""'YMM            dP            
 * M  MMMMM  M             88            88                        M  mmmm. `M            88            
 * M  MMMMP  M dP .d8888b. 88 .d8888b. d8888P dP .d8888b. 88d888b. M  MMMMM  M .d8888b. d8888P .d8888b. 
 * M  MMMM' .M 88 88'  `88 88 88'  `88   88   88 88'  `88 88'  `88 M  MMMMM  M 88'  `88   88   88'  `88 
 * M  MMP' .MM 88 88.  .88 88 88.  .88   88   88 88.  .88 88    88 M  MMMM' .M 88.  .88   88   88.  .88 
 * M     .dMMM dP `88888P' dP `88888P8   dP   dP `88888P' dP    dP M       .MM `88888P8   dP   `88888P8 
 * MMMMMMMMMMM                                                     MMMMMMMMMMM                          
 */
/**
 * Violation specific data, for executing actions.<br>
 * This is meant to capture a violation incident in a potentially thread safe way.
 * 
 * @author asofold
 */
public class ViolationData {

    /** The actions to be executed. */
    public final ActionList actions;
    
    /** The actions applicable for the violation level. */
    public final Action[] applicableActions;

    /** The violation level added. */
    public final double     addedVL;

    /** The check. */
    public final Check      check;

    /** The player. */
    public final Player     player;

    /** The violation level. */
    public final double     vL;
    
    /** Filled in parameters. */
    private final Map<ParameterName, String> parameters;

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
        for (int i = 0; i < applicableActions.length; i++){
        	if (applicableActions[i].needsParameters()){
        		needsParameters = true;
        		break;
        	}
        }
        parameters = needsParameters ? check.getParameterMap(this) : null;
    }

    /**
     * Gets the actions.
     * 
     * @return the actions
     */
    public Action[] getActions() {
        return applicableActions;
    }
    
	/**
	 * Execute actions and return if cancel. Does add it to history.
	 * @return
	 */
	public boolean executeActions(){
		try {

       	   ViolationHistory.getHistory(player).log(check.getClass().getName(), addedVL);

           // TODO: the time is taken here, which makes sense for delay, but otherwise ?
           final long time = System.currentTimeMillis() / 1000L;
           boolean cancel = false;
           for (final Action action : getActions())
               if (Check.getHistory(player).executeAction(this, action, time))
                   // The execution history said it really is time to execute the action, find out what it is and do
                   // what is needed.
                   if (action.execute(this)) cancel = true;

           return cancel;
       } catch (final Exception e) {
           e.printStackTrace();
           // On exceptions cancel events.
           return true;
       }
	}
    
	/**
	 * Check if the actions contain a cancel. 
	 * @return
	 */
	public boolean hasCancel(){
		for (final Action action : applicableActions){
			if (action instanceof CancelAction) return true;
		}
		return false;
	}
	
	/**
	 * Get the parameters value for this violation.
	 * @param parameterName
	 * @return Will always return some string, if not set: "<?PARAMETERNAME>".
	 */
	public String getParameter(final ParameterName parameterName){
		if (parameterName == null) return "<???>";
		switch (parameterName) {
		case CHECK:
			return check.getClass().getSimpleName();
		case PLAYER:
			return player.getName();
		case VIOLATIONS:
			return String.valueOf((long) Math.round(vL));
		default:
			break;
		}
		if (parameters == null) return "<?" + parameterName + ">";
		final String value = parameters.get(parameterName);
		return(value == null) ? ("<?" + parameterName + ">") : value;
	}
	
	public void setParameter(final ParameterName parameterName, String value){
		if (parameters != null) parameters.put(parameterName, value);
	}

    public boolean needsParameters() {
        return parameters != null;
    }
   
}
