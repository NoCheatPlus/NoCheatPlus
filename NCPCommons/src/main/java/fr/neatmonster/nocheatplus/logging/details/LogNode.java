package fr.neatmonster.nocheatplus.logging.details;

import fr.neatmonster.nocheatplus.logging.LoggerID;


public class LogNode<C> {
    
    public final LoggerID loggerID;
    public final ContentLogger<C> logger;
    public final LogOptions options;

    public LogNode(LoggerID loggerID, ContentLogger<C> logger, LogOptions options) {
        this.loggerID = loggerID;
        this.logger = logger;
        this.options = new LogOptions(options);
    }
    
}
