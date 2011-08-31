package cc.co.evenprime.bukkit.nocheat.actions.types;

/**
 * An action gets executed as the result of a failed check. If it 'really' gets
 * executed depends on how many executions have occured within the last 60
 * seconds and how much time was between this and the previous execution
 * 
 * @author Evenprime
 * 
 */
public abstract class Action {

    /**
     * Delay in violations (only do if there were more than "delay" exceptions
     * in last 60 seconds)
     */
    public final int delay;

    /**
     * Repeat only every "repeat" seconds
     */
    public final int repeat;

    public Action(int delay, int repeat) {

        this.delay = delay;
        this.repeat = repeat;
    }
}
