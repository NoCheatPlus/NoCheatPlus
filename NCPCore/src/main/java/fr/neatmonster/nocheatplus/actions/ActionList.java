package fr.neatmonster.nocheatplus.actions;

import fr.neatmonster.nocheatplus.checks.ViolationData;



/*
 * MMP"""""""MM            dP   oo                   M""MMMMMMMM oo            dP   
 * M' .mmmm  MM            88                        M  MMMMMMMM               88   
 * M         `M .d8888b. d8888P dP .d8888b. 88d888b. M  MMMMMMMM dP .d8888b. d8888P 
 * M  MMMMM  MM 88'  `""   88   88 88'  `88 88'  `88 M  MMMMMMMM 88 Y8ooooo.   88   
 * M  MMMMM  MM 88.  ...   88   88 88.  .88 88    88 M  MMMMMMMM 88       88   88   
 * M  MMMMM  MM `88888P'   dP   dP `88888P' dP    dP M         M dP `88888P'   dP   
 * MMMMMMMMMMMM                                      MMMMMMMMMMM                    
 */
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
