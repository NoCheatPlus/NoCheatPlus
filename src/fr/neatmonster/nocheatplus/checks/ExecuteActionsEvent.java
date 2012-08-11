package fr.neatmonster.nocheatplus.checks;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * MM""""""""`M                                       dP            MMP"""""""MM            dP   oo                            
 * MM  mmmmmmmM                                       88            M' .mmmm  MM            88                                 
 * M`      MMMM dP.  .dP .d8888b. .d8888b. dP    dP d8888P .d8888b. M         `M .d8888b. d8888P dP .d8888b. 88d888b. .d8888b. 
 * MM  MMMMMMMM  `8bd8'  88ooood8 88'  `"" 88    88   88   88ooood8 M  MMMMM  MM 88'  `""   88   88 88'  `88 88'  `88 Y8ooooo. 
 * MM  MMMMMMMM  .d88b.  88.  ... 88.  ... 88.  .88   88   88.  ... M  MMMMM  MM 88.  ...   88   88 88.  .88 88    88       88 
 * MM        .M dP'  `dP `88888P' `88888P' `88888P'   dP   `88888P' M  MMMMM  MM `88888P'   dP   dP `88888P' dP    dP `88888P' 
 * MMMMMMMMMMMM                                                     MMMMMMMMMMMM                                               
 * 
 * MM""""""""`M                              dP   
 * MM  mmmmmmmM                              88   
 * M`      MMMM dP   .dP .d8888b. 88d888b. d8888P 
 * MM  MMMMMMMM 88   d8' 88ooood8 88'  `88   88   
 * MM  MMMMMMMM 88 .88'  88.  ... 88    88   88   
 * MM        .M 8888P'   `88888P' dP    dP   dP   
 * MMMMMMMMMMMM                                   
 */
/**
 * This event is to be fired to execute actions in the main thread.
 * 
 * @author asofold
 */
public class ExecuteActionsEvent extends Event {

    /** The list of the handlers of this event. */
    private static final HandlerList handlers = new HandlerList();

    /**
     * Return the list of all the handlers of this event.
     * 
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /** The violation data. */
    private final ViolationData violationData;

    /** If the actions have been executed already. */
    private boolean             actionsExecuted = false;

    /** If the event is cancelled or not. */
    private boolean             cancel          = false;

    /**
     * Instantiates a new execute actions event.
     * 
     * @param violationData
     *            the violation data
     */
    public ExecuteActionsEvent(final ViolationData violationData) {
        this.violationData = violationData;
    }

    /**
     * Execute the actions.
     */
    public void executeActions() {
        if (actionsExecuted)
            return;
        cancel = violationData.check.executeActions(violationData);
        actionsExecuted = true;
    }

    /**
     * Return if the event is cancelled.
     * 
     * @return the cancellation state
     */
    public boolean getCancel() {
        return cancel;
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.Event#getHandlers()
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
