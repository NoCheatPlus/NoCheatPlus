package cc.co.evenprime.bukkit.nocheat.actions.history;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cc.co.evenprime.bukkit.nocheat.actions.types.Action;

/**
 * Store last 60 seconds of action executions
 * 
 * @author Evenprime
 * 
 */
public class ActionHistory {

    private class ExecutionHistoryEntry {

        private final LinkedList<Long> executionTimes     = new LinkedList<Long>();
        private final long             monitoredTimeFrame = 60000;
        private long                   lastExecution      = 0;

        public ExecutionHistoryEntry() {}

        public void addCounter(Long time) {

            synchronized(executionTimes) {
                while(executionTimes.size() > 0 && executionTimes.getFirst() < time - monitoredTimeFrame) {
                    executionTimes.removeFirst();
                }

                executionTimes.add(time);
            }
        }

        public int getCounter() {
            return executionTimes.size();
        }

        public long getLastExecution() {
            return lastExecution;
        }

        public void setLastExecution(long time) {
            this.lastExecution = time;
        }
    }

    // Store data between Events
    // time + action + action-counter
    private final Map<Action, ExecutionHistoryEntry> executionHistory = new HashMap<Action, ExecutionHistoryEntry>();

    public ActionHistory() {}

    /**
     * Returns true, if the action should be executed, because all time
     * criteria have been met. Will add a entry with the time to a list
     * which will influence further requests, so only use once per
     * check!
     * 
     * @param action
     * @param time
     * @return
     */
    public boolean executeAction(Action action, long time) {

        ExecutionHistoryEntry entry = executionHistory.get(action);

        if(entry == null) {
            entry = new ExecutionHistoryEntry();
            executionHistory.put(action, entry);
        }

        // update entry
        entry.addCounter(time);

        if(entry.getCounter() > action.delay) {
            // Execute action?
            if(entry.getLastExecution() < time - action.repeat * 1000) {
                // Execute action!
                entry.setLastExecution(time);
                return true;
            }
        }

        return false;
    }
}
