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
public class LoggerID {
    
    public final String name;
    
    public LoggerID(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "<LoggerID " + name + ">";
    }
    
}
