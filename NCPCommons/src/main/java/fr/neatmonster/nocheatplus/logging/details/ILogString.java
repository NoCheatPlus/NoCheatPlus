package fr.neatmonster.nocheatplus.logging.details;

import java.util.logging.Level;

import fr.neatmonster.nocheatplus.logging.StreamID;

/**
 * Standard logging for String messages.
 * 
 * @author asofold
 *
 */
public interface ILogString {

    void debug(StreamID streamID, String message);

    void info(StreamID streamID, String message);

    void warning(StreamID streamID, String message);

    void severe(StreamID streamID, String message);

    void log(StreamID streamID, Level level, String message);

}
