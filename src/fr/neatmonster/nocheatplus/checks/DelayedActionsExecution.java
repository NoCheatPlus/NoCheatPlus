package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.types.CancelAction;
import fr.neatmonster.nocheatplus.metrics.MetricsData;

/**
 * For scheduling actions execution. This does not check the NCPHookManager.
 * <hr>
 * Not put to ViolationData itself for the possibility of adding other data (might be considered though).
 * @author mc_dev
 *
 */
public class DelayedActionsExecution {

	protected final ViolationData violationData;
	protected final Action[] actions;

	public DelayedActionsExecution(final ViolationData violationData) {
		this.violationData = violationData;
		actions = violationData.getActions();
	}
	
	/**
	 * Execute actions and return if cancel.
	 * @return
	 */
	public boolean execute(){
		try {

       	   ViolationHistory.getHistory(violationData.player).log(getClass().getName(), violationData.addedVL);

           // Add this failed check to the Metrics data.
           MetricsData.addFailed(violationData.check.type);

           final long time = System.currentTimeMillis() / 1000L;
           boolean cancel = false;
           for (final Action action : actions)
               if (Check.getHistory(violationData.player).executeAction(violationData, action, time))
                   // The execution history said it really is time to execute the action, find out what it is and do
                   // what is needed.
                   if (action.execute(violationData)) cancel = true;

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
		for (final Action action : actions){
			if (action instanceof CancelAction) return true;
		}
		return false;
	}
}
