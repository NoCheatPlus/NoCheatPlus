package fr.neatmonster.nocheatplus.components.registry.exception;

/**
 * A registry item has already been registered (for a given context), and the
 * registry does not support overriding via register(...) - it might still
 * support unregister(...) and register(...), in case what to unregister is
 * known.
 * 
 * @author asofold
 *
 */
public class AlreadyRegisteredException extends RegistryException {

    private static final long serialVersionUID = -72557863263954102L;

    public AlreadyRegisteredException() {
        super();
    }

    public AlreadyRegisteredException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyRegisteredException(String message) {
        super(message);
    }

    public AlreadyRegisteredException(Throwable cause) {
        super(cause);
    }

}
