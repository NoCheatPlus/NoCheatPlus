package fr.neatmonster.nocheatplus.actions;

import fr.neatmonster.nocheatplus.checks.ViolationData;

/*
 * MMP"""""""MM            dP   oo                   
 * M' .mmmm  MM            88                        
 * M         `M .d8888b. d8888P dP .d8888b. 88d888b. 
 * M  MMMMM  MM 88'  `""   88   88 88'  `88 88'  `88 
 * M  MMMMM  MM 88.  ...   88   88 88.  .88 88    88 
 * M  MMMMM  MM `88888P'   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMMM                                      
 */
/**
 * An action gets executed as the result of a failed check. If it 'really' gets executed depends on how many executions
 * have occurred within the last 60 seconds and how much time was between this and the previous execution.
 */
public abstract class Action {
    /**
     * The name of the action, to identify it, e.g. in the configuration file.
     */
    public final String name;

    /**
     * Delay in violations. An "ExecutionHistory" will use this info to make sure that there were at least "delay"
     * attempts to execute this action before it really gets executed.
     */
    public final int    delay;

    /**
     * Repeat only every "repeat" seconds. An "ExecutionHistory" will use this info to make sure that there were at
     * least "repeat" seconds between the last execution of this action and this execution.
     */
    public final int    repeat;

    /**
     * Instantiates a new action.
     * 
     * @param name
     *            the name
     * @param delay
     *            the delay
     * @param repeat
     *            the repetition delay
     */
    public Action(final String name, final int delay, final int repeat) {
        this.name = name;
        this.delay = delay;
        this.repeat = repeat;
    }

    /**
     * Execute the action.
     * 
     * @param violationData
     *            the violation data
     * @return true, if successful
     */
    public abstract boolean execute(final ViolationData violationData);
    
    /**
     * Check if parameters are needed at all for faster processing.
     * @return
     */
    public boolean needsParameters(){
    	return false;
    }

	/**
	 * Indicates that the action will be executed in any case (delay, repeat).
	 * executed at all.
	 * 
	 * @return
	 */
	public boolean executesAlways() {
		return delay == 0 && repeat == 0;
	}
}
