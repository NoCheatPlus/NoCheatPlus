package fr.neatmonster.nocheatplus.logging.details;

import java.util.logging.Level;

/**
 * A log message to be executed within a task from a queue, hiding the generics.
 * @author dev1mc
 *
 */
public class LogRecord<C> implements Runnable {
    
    private final LogNode<C> node;
    private final Level level;
    private final C content;

    public LogRecord(LogNode<C> node, Level level, C content) {
        this.node = node;
        this.level = level;
        this.content = content;
    }
    
    @Override
    public void run() {
        // TODO: Checks / try-catch where? 
        node.logger.log(level, content);
    }
    
}
