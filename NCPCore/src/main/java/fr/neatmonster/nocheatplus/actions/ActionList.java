package fr.neatmonster.nocheatplus.actions;

import fr.neatmonster.nocheatplus.checks.ViolationData;

/**
 * A list of actions, that associates actions to thresholds. It allows to retrieve all actions that match a certain
 * threshold.
 * <hr>
 * TODO: refactor to an array of Actions entries (threshold + Action[]) + sort that one.
 */
public class ActionList extends AbstractActionList<ViolationData, ActionList>{

	public static final ActionListFactory<ViolationData, ActionList> listFactory = new ActionListFactory<ViolationData, ActionList>() {

		@Override
		public ActionList getNewActionList(String permissionSilent) {
			return new ActionList(permissionSilent);
		}
		
	}; 
	
	public ActionList(String permissionSilent) {
		super(permissionSilent, listFactory);
	}
	
}
