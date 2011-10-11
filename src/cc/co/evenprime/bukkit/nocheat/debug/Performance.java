package cc.co.evenprime.bukkit.nocheat.debug;


public class Performance {

    private long totalTime = 0;
    private long counter = 1; // start with 1 to avoid DIV/0 errors
    private final boolean enabled;
    
    
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
}
