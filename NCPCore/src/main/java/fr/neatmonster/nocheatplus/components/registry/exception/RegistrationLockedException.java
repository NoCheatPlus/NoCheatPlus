package fr.neatmonster.nocheatplus.components.registry.exception;

/**
 * A registration item has been locked versus changes, but was attempted to be
 * changed.
 * 
 * @author asofold
 *
 */
public class RegistrationLockedException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -7278363049512687206L;

    public RegistrationLockedException() {
        super();
    }

    public RegistrationLockedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RegistrationLockedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistrationLockedException(String message) {
        super(message);
    }

    public RegistrationLockedException(Throwable cause) {
        super(cause);
    }

}
