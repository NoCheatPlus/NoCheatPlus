package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionData;

/*
 * MM'""""'YMM                                     dP MMP"""""""MM            dP   oo                   
 * M' .mmm. `M                                     88 M' .mmmm  MM            88                        
 * M  MMMMMooM .d8888b. 88d888b. .d8888b. .d8888b. 88 M         `M .d8888b. d8888P dP .d8888b. 88d888b. 
 * M  MMMMMMMM 88'  `88 88'  `88 88'  `"" 88ooood8 88 M  MMMMM  MM 88'  `""   88   88 88'  `88 88'  `88 
 * M. `MMM' .M 88.  .88 88    88 88.  ... 88.  ... 88 M  MMMMM  MM 88.  ...   88   88 88.  .88 88    88 
 * MM.     .dM `88888P8 dP    dP `88888P' `88888P' dP M  MMMMM  MM `88888P'   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMM                                        MMMMMMMMMMMM                                      
 */
/**
 * Do something check-specific. Usually that is to cancel the event, undo something the player did, or do something the
 * server should've done.
 */
public class CancelAction<D extends ActionData, L extends AbstractActionList<D, L>> extends Action<D, L> {

    /**
     * Instantiates a new cancel action.
     */
    public CancelAction() {
        super("cancel", 0, 0);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.actions.Action#execute(fr.neatmonster.nocheatplus.checks.ViolationData)
     */
    @Override
    public boolean execute(final D data) {
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "cancel";
    }
}
