package fr.neatmonster.nocheatplus.logging.details;

import java.util.logging.Level;

/**
 * Minimal interface for logging content to.
 * @author dev1mc
 *
 * @param <C>
 */
public interface ContentLogger <C> {
    
    // TODO: Not sure about generics.
    public void log(Level level, C content);
    
}
