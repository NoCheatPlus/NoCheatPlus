package fr.neatmonster.nocheatplus.players;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.checks.ViolationData;

/*
 * MM""""""""`M                                       dP   oo                   
 * MM  mmmmmmmM                                       88                        
 * M`      MMMM dP.  .dP .d8888b. .d8888b. dP    dP d8888P dP .d8888b. 88d888b. 
 * MM  MMMMMMMM  `8bd8'  88ooood8 88'  `"" 88    88   88   88 88'  `88 88'  `88 
 * MM  MMMMMMMM  .d88b.  88.  ... 88.  ... 88.  .88   88   88 88.  .88 88    88 
 * MM        .M dP'  `dP `88888P' `88888P' `88888P'   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMMM                                                                 

 * M""MMMMM""MM oo            dP                              
 * M  MMMMM  MM               88                              
 * M         `M dP .d8888b. d8888P .d8888b. 88d888b. dP    dP 
 * M  MMMMM  MM 88 Y8ooooo.   88   88'  `88 88'  `88 88    88 
 * M  MMMMM  MM 88       88   88   88.  .88 88       88.  .88 
 * M  MMMMM  MM dP `88888P'   dP   `88888P' dP       `8888P88 
 * MMMMMMMMMMMM                                           .88 
 *                                                    d8888P  
 */
/**
 * Store amount of action executions for last 60 seconds for various actions.
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
    private final Map<Action, ExecutionHistoryEntry> entries;

    /**
     * Instantiates a new execution history.
     */
    public ExecutionHistory() {
        entries = new HashMap<Action, ExecutionHistoryEntry>();
    }

    /**
     * Returns true, if the action should be executed, because all time criteria have been met. Will add a entry with
     * the time to a list which will influence further requests, so only use once and remember the result.
     * 
     * @param violationData
     *            the violation data
     * @param action
     *            the action
     * @param time
     *            a time IN SECONDS
     * @return true, if the action is to be executed.
     */
    public boolean executeAction(final ViolationData violationData, final Action action, final long time) {

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
     * Access API to check if the aciton would get executed.
     * @param violationData
     * @param action
     * @param time
     * @return
     */
    public boolean wouldExecute(final ViolationData violationData, final Action action, final long time) {
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
    public ExecutionHistoryEntry getEntry(final Action action){
        return entries.get(action);
    }
}
