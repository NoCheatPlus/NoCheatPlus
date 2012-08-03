package fr.neatmonster.nocheatplus.players;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.nocheatplus.actions.Action;

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
    private static class ExecutionHistoryEntry {

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
        private ExecutionHistoryEntry(final int monitoredTimeFrame) {
            executionTimes = new int[monitoredTimeFrame];
        }

        /**
         * Remember an execution at the specific time.
         * 
         * @param time
         *            the time
         */
        private void addCounter(final long time) {
            // Clear out now outdated values from the array.
            if (time - lastClearedTime > 0) {
                // Clear the next few fields of the array.
                clearTimes(lastClearedTime + 1, time - lastClearedTime);
                lastClearedTime = time + 1;
            }

            executionTimes[(int) (time % executionTimes.length)]++;
            totalEntries++;
        }

        /**
         * Clean parts of the array.
         * 
         * @param start
         *            the start
         * @param length
         *            the length
         */
        private void clearTimes(final long start, long length) {

            if (length <= 0)
                return; // Nothing to do (yet).
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
    private final Map<String, Map<Action, ExecutionHistoryEntry>> executionHistories;

    /**
     * Instantiates a new execution history.
     */
    public ExecutionHistory() {
        executionHistories = new HashMap<String, Map<Action, ExecutionHistoryEntry>>();
    }

    /**
     * Returns true, if the action should be executed, because all time criteria have been met. Will add a entry with
     * the time to a list which will influence further requests, so only use once and remember the result.
     * 
     * @param check
     *            the check
     * @param action
     *            the action
     * @param time
     *            a time IN SECONDS
     * @return true, if successful
     */
    public boolean executeAction(final String check, final Action action, final long time) {

        Map<Action, ExecutionHistoryEntry> executionHistory = executionHistories.get(check);

        if (executionHistory == null) {
            executionHistory = new HashMap<Action, ExecutionHistoryEntry>();
            executionHistories.put(check, executionHistory);
        }

        ExecutionHistoryEntry entry = executionHistory.get(action);

        if (entry == null) {
            entry = new ExecutionHistoryEntry(60);
            executionHistory.put(action, entry);
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
}
