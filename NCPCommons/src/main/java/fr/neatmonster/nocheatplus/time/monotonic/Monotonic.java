package fr.neatmonster.nocheatplus.time.monotonic;


/**
 * Static provider for monotonic clocks. Note that some calls can only be made
 * from the server/application main thread, thread safe methods are prefixed
 * with "synch", however all call results may have different offsets.
 */
public class Monotonic {
    
    private static final MonotonicClock nanos = new MonotonicNanosClock();
    private static final MonotonicClock millis = new MonotonicMillisClock();
    private static final MonotonicClock synchMillis = new MonotonicSynchClock(new MonotonicMillisClock());
    private static final MonotonicClock synchNanos = new MonotonicSynchClock(new MonotonicNanosClock());
    
    /**
     * Monotonic nanoseconds time, corresponding to System.nanoTime(). <br>
     * <b>Not thread-safe, only call from the main server/application
     * thread.</b>
     * 
     * @return Monotonic time in nanoseconds.
     */
    public static long nanos() {
        return nanos.clock();
    }
    
    /**
     * Monotonic milliseconds time, corresponding to System.currentTimeMillis(). <br>
     * <b>Not thread-safe, only call from the main server/application
     * thread.</b>
     * 
     * @return Monotonic time in milliseconds.
     */
    public static long millis() {
        return millis.clock();
    }
    
    /**
     * Monotonic nanoseconds time, corresponding to System.nanoTime(). <br>
     * Thread-safe.
     * 
     * @return Monotonic time in nanoseconds.
     */
    public static long synchNanos() {
        return synchNanos.clock();
    }
    
    /**
     * Monotonic milliseconds time, corresponding to System.currentTimeMillis(). <br>
     * Thread-safe.
     * 
     * @return Monotonic time in milliseconds.
     */
    public static long synchMillis() {
        return synchMillis.clock();
    }
    
}
