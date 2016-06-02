/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.actions;

import fr.neatmonster.nocheatplus.config.ConfigFileWithActions;

/**
 * An action gets executed as the result of a failed check. If it 'really' gets executed depends on how many executions
 * have occurred within the last 60 seconds and how much time was between this and the previous execution.
 */
public abstract class Action <D extends ActionData, L extends AbstractActionList<D, L>>{
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
     */
    public abstract void execute(final D violationData);

    /**
     * Check if parameters are needed at all for faster processing.
     * <hr>
     * This method must be thread-safe, if an execution of this might happen
     * outside of the primary thread.
     * 
     * @return
     */
    public boolean needsParameters() {
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

    /**
     * Get an optimized copy, given the config in use. The default implementation returns this instance.<br>
     * TODO: "Copy" does not match this.
     * @param config
     * @param threshold
     * @return Can return this (unchanged), null (not to be executed ever) or a new instance (changed, optimized).
     */
    public Action<D, L> getOptimizedCopy(final ConfigFileWithActions<D, L> config, final Integer threshold) {
        return this;
    }

    /**
     * Test if this action is (like) an optimized version of this action type,
     * like would be returned by getOptimizedCopy.
     * 
     * @return
     */
    public boolean isOptimized() {
        return true;
    }

}
