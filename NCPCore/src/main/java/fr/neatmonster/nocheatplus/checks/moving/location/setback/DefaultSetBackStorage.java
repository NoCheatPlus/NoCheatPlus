package fr.neatmonster.nocheatplus.checks.moving.location.setback;

/**
 * A default extension of SetBackStorage, featuring convenience methods for
 * set-back locations with standard naming, including a default set-back.
 * <ul>
 * <li>0: Default. Typically after teleport/join.</li>
 * <li>1: Mid-term. Typically used by more packets checks.</li>
 * <li>2: Safe-medium. Typically last ground/reset-condition or a default-medium
 * in case of boats.</li>
 * <li>3: Last valid. The latest end point of a move, not prevented by any
 * check.</li>
 * </ul>
 * 
 * @author asofold
 *
 */
public class DefaultSetBackStorage extends SetBackStorage {

    // TODO: In case of keeping track of past moves, last valid could be skipped (vehicles are currently not planned to have past moves accessible).

    public DefaultSetBackStorage() {
        super(4, 0);
    }

}
