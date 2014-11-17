package fr.neatmonster.nocheatplus.logging.details;

import java.util.logging.Level;

/**
 * Handle logging on LogNode instances (log directly, skip, schedule to log within tasks).
 * @author dev1mc
 *
 */
public interface LogNodeDispatcher { // TODO: Name.
    
    public <C> void dispatch(LogNode<C> node, Level level, C content);

    /**
     * Cancel asynchronous tasks and remove all logs based on policy (log all or clear), default is to log
     * all. Should be called from the primary thread, if it exists.
     * @param ms Milliseconds to wait in case there is something being processed by asynchronous tasks.
     */
    public abstract void flush(long ms);

    /**
     * Allow to add a logger, for logging errors to the init stream. Must not use the queues. Can be null (no logging).
     * @param logger
     */
    void setInitLogger(ContentLogger<String> logger);
    
    /**
     * Set maximum queue size. After reaching that size queues will be reduced by dropping elements (asynchronous and primaray thread individually).
     * @param maxQueueSize
     */
    void setMaxQueueSize(int maxQueueSize);
    
}
