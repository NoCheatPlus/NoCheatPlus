package fr.neatmonster.nocheatplus.logging;

/**
 * Restrictions for registration:
 * <li>Unique instances.</li>
 * <li>Unique names.</li>
 * <li>Custom registrations can not start with the default prefix (see AbstractLogManager).</li>
 * 
 * @author dev1mc
 *
 */
public class StreamID {
    
    public final String name;
    
    public StreamID(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "<StreamID " + name + ">";
    }
    
}
