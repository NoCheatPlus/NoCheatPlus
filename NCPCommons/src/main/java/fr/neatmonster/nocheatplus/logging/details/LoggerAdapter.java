package fr.neatmonster.nocheatplus.logging.details;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerAdapter implements ContentLogger<String> {
    
    protected final Logger logger;

    public LoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(Level level, String content) {
        logger.log(level, content);
    }
    
}
