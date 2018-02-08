package fr.neatmonster.nocheatplus.components.registry.exception;

/**
 * An item is not registered, although that is demanded in this context.
 * 
 * @author asofold
 *
 */
public class NotRegisteredException extends RegistryException {

    /**
     * 
     */
    private static final long serialVersionUID = 6240601169826276653L;

    public NotRegisteredException() {
        super();
    }

    public NotRegisteredException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotRegisteredException(String message) {
        super(message);
    }

    public NotRegisteredException(Throwable cause) {
        super(cause);
    }

}
