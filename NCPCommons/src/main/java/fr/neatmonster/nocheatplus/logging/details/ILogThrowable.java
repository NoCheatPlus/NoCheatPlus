package fr.neatmonster.nocheatplus.logging.details;

import java.util.logging.Level;

import fr.neatmonster.nocheatplus.logging.StreamID;

/**
 * Standard logging for Throwable throwables.
 * 
 * @author asofold
 *
 */
public interface ILogThrowable {

    void debug(StreamID streamID, Throwable t);

    void info(StreamID streamID, Throwable t);

    void warning(StreamID streamID, Throwable t);

    void severe(StreamID streamID, Throwable t);

    void log(StreamID streamID, Level level, Throwable t);

}
