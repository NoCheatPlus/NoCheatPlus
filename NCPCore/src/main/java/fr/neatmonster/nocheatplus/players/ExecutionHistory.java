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
package fr.neatmonster.nocheatplus.players;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;

/**
 * Store amount of action executions for last 60 seconds for various actions.<br>
 * TODO: Once away from static access, could put this to generic (Action<D extends ActionData>).
 */
public class ExecutionHistory {

    /**
     * Represents an entry in the execution history.
     */
    public static class ExecutionHistoryEntry {

        /** The execution times. */
        private final int executionTimes[];

        /** The last execution. */
        private long      lastExecution   = 0;

        /** The total entries. */
        private int       totalEntries    = 0;

        /** The last cleared time. */
        private long      lastClearedTime = 0;

        /**
         * Instantiates a new execution history entry.
         * 
         * @param monitoredTimeFrame
         *            the monitored time frame
         */
        public ExecutionHistoryEntry(final int monitoredTimeFrame) {
            executionTimes = new int[monitoredTimeFrame];
        }

        /**
         * Remember an execution at the specific time.
         * 
         * @param time
         *            the time
         */
        public void addCounter(final long time) {
            // Clear out now outdated values from the array.
            checkCounter(time);

            executionTimes[(int) (time % executionTimes.length)]++;
            totalEntries++;
        }
        
        /**
         * Access method to adjust state to point of time.
         * @param time
         */
        public void checkCounter(final long time){
            if (time - lastClearedTime > 0) {
                // Clear the next few fields of the array.
                clearTimes(lastClearedTime + 1, time - lastClearedTime);
                lastClearedTime = time + 1;
            }
        }

        /**
         * Clean parts of the array.
         * 
         * @param start
         *            the start
         * @param length
         *            the length
         */
        protected void clearTimes(final long start, long length) {
            if (length <= 0)
                // Nothing to do (yet).
                return;
            if (length > executionTimes.length)
                length = executionTimes.length;

            int j = (int) start % executionTimes.length;

            for (int i = 0; i < length; i++) {
                if (j == executionTimes.length)
                    j = 0;

                totalEntries -= executionTimes[j];
                executionTimes[j] = 0;

                j++;
            }
        }

        /**
         * Gets the counter.
         * 
         * @return the counter
         */
        public int getCounter() {
            return totalEntries;
        }

        /**
         * Gets the last execution.
         * 
         * @return the last execution
         */
        public long getLastExecution() {
            return lastExecution;
        }

        /**
         * Sets the last execution.
         * 
         * @param time
         *            the new last execution
         */
        public void setLastExecution(final long time) {
            lastExecution = time;
        }
    }

    /** Store data between events (time + action + action-counter). **/
    private final Map<Action<ViolationData, ActionList>, ExecutionHistoryEntry> entries;

    /**
     * Instantiates a new execution history.
     */
    public ExecutionHistory() {
        entries = new HashMap<Action<ViolationData, ActionList>, ExecutionHistoryEntry>();
    }

	/**
	 * Returns true, if the action should be executed, because all time criteria
	 * have been met. Will add a entry with the time to a list which will
	 * influence further requests, so only use once and remember the result.
	 * If the action is to be executed always, it will not be added to the history.
	 * @param violationData
	 *            the violation data
	 * @param action
	 *            the action
	 * @param time
	 *            a time IN SECONDS
	 * @return true, if the action is to be executed.
	 */
	public boolean executeAction(final ViolationData violationData, final Action<ViolationData, ActionList> action, final long time)
	{
		if (action.executesAlways()) return true;
        ExecutionHistoryEntry entry = entries.get(action);
        if (entry == null) {
            entry = new ExecutionHistoryEntry(60);
            entries.put(action, entry);
        }

        // Update entry.
        entry.addCounter(time);

        if (entry.getCounter() > action.delay)
            // Execute action?
            if (entry.getLastExecution() <= time - action.repeat) {
                // Execute action!
                entry.setLastExecution(time);
                return true;
            }

        return false;
    }
    
    /**
     * Access API to check if the action would get executed.
     * @param violationData
     * @param action
     * @param time
     * @return
     */
	public boolean wouldExecute(final ViolationData violationData, final Action<ViolationData, ActionList> action, final long time)
	{
		if (action.executesAlways()) return true;
        ExecutionHistoryEntry entry = entries.get(action);
        if (entry == null) {
            return action.delay <= 0;
        }

        // Update entry (not adding).
        entry.checkCounter(time);

        if (entry.getCounter() + 1 > action.delay){
            if (entry.getLastExecution() <= time - action.repeat) return true;
        }
        return false;
    }
    
    /**
     * Access method.
     * @param action
     * @return
     */
    public ExecutionHistoryEntry getEntry(final Action<ViolationData, ActionList> action){
        return entries.get(action);
    }
}
