package cc.co.evenprime.bukkit.nocheat.debug;


public class Performance {

    private long totalTime = 0;
    private long counter = 1; // start with 1 to avoid DIV/0 errors
    private final boolean enabled;
    
    private static final long            NANO   = 1;
    private static final long            MICRO  = NANO * 1000;
    private static final long            MILLI  = MICRO * 1000;
    private static final long            SECOND = MILLI * 1000;
    private static final long            MINUTE = SECOND * 60;
    
    
    public Performance(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void addTime(long nanoTime) {
        counter++;
        this.totalTime += nanoTime;
    }
    
    public long getTotalTime() {
        return this.totalTime;
    }
    
    public long getRelativeTime() {
        return this.totalTime / this.counter;
    }
    
    
    public long getCounter() {
        return this.counter;
    }
    
    
    public boolean isEnabled() {
        return enabled;
    }
    
    private static String getAppropriateUnit(long timeInNanoseconds) {

        // more than 10 minutes
        if(timeInNanoseconds > MINUTE * 10) {
            return "minutes";
        }
        // more than 10 seconds
        else if(timeInNanoseconds > SECOND * 10) {
            return "seconds";
        }
        // more than 10 milliseconds
        else if(timeInNanoseconds > MILLI * 10) {
            return "milliseconds";
        }
        // more than 10 microseconds
        else if(timeInNanoseconds > MICRO * 10) {
            return "microseconds";
        } else {
            return "nanoseconds";
        }
    }

    private static long convertToAppropriateUnit(long timeInNanoseconds) {
        // more than 10 minutes
        if(timeInNanoseconds > MINUTE * 10) {
            return timeInNanoseconds / MINUTE;
        }
        // more than 10 seconds
        else if(timeInNanoseconds > SECOND * 10) {
            return timeInNanoseconds / SECOND;
        }
        // more than 10 milliseconds
        else if(timeInNanoseconds > MILLI * 10) {
            return timeInNanoseconds / MILLI;
        }
        // more than 10 microseconds
        else if(timeInNanoseconds > MICRO * 10) {
            return timeInNanoseconds / MICRO;
        } else {
            return timeInNanoseconds / NANO;
        }
    }
    
    public static String toString(long timeInNanoseconds) {
        return convertToAppropriateUnit(timeInNanoseconds) + " " + getAppropriateUnit(timeInNanoseconds);
    }
}
