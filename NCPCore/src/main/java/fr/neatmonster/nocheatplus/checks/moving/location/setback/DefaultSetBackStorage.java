package fr.neatmonster.nocheatplus.checks.moving.location.setback;

import org.bukkit.Location;

import fr.neatmonster.nocheatplus.components.location.ILocationWithLook;
import fr.neatmonster.nocheatplus.time.monotonic.Monotonic;

/**
 * A default extension of SetBackStorage, featuring convenience methods for
 * set-back locations with standard naming, including a default set-back.
 * <ul>
 * <li>0: Default. Typically after teleport/join.</li>
 * <li>1: Mid-term. Typically used by more packets checks.</li>
 * <li>2: Safe-medium. Typically last ground/reset-condition or a default-medium
 * in case of boats.</li>
 * <li>3: Last-move. The latest end point of a move, not prevented by any check.
 * This should usually be the most recent entry.</li>
 * </ul>
 * 
 * @author asofold
 *
 */
public class DefaultSetBackStorage extends SetBackStorage {

    // TODO: In case of keeping track of past moves, last valid could be skipped (vehicles are currently not planned to have past moves accessible).

    public static final int indexDefault = 0;
    public static final int indexMidTerm = 1;
    public static final int indexSafeMedium = 2;
    public static final int indexLastMove = 3;

    public DefaultSetBackStorage() {
        super(4, 0);
    }

    public boolean isMidTermEntryValid() {
        return isEntryValid(indexMidTerm);
    }

    public boolean isSafeMediumEntryValid() {
        return isEntryValid(indexSafeMedium);
    }

    public boolean isLastMoveEntryValid() {
        return isEntryValid(indexLastMove);
    }

    /**
     * Get the 'mid-term' set-back entry, disregarding validity.
     * 
     * @return
     */
    public SetBackEntry getMidTermEntry() {
        return entries[indexMidTerm];
    }

    /**
     * Get the 'safe-medium' set-back entry, disregarding validity.
     * 
     * @return
     */
    public SetBackEntry getSafeMediumEntry() {
        return entries[indexSafeMedium];
    }

    /**
     * Get the 'last-move' set-back entry, disregarding validity of the entry
     * itself.
     * 
     * @return
     */
    public SetBackEntry getLastMoveEntry() {
        return entries[indexLastMove];
    }

    /**
     * Update the location for the default entry in-place. Time values are set
     * as well.
     * 
     * @param loc
     */
    public void setMidTermEntry(final Location loc) {
        getMidTermEntry().set(loc, ++time, Monotonic.millis());
    }

    /**
     * Update the location for the default entry in-place. Time values are set
     * as well.
     * 
     * @param loc
     */
    public void setMidTermEntry(final ILocationWithLook loc) {
        getMidTermEntry().set(loc, ++time, Monotonic.millis());
    }

    /**
     * Update the location for the default entry in-place. Time values are set
     * as well.
     * 
     * @param loc
     */
    public void setSafeMediumEntry(final Location loc) {
        getSafeMediumEntry().set(loc, ++time, Monotonic.millis());
    }

    /**
     * Update the location for the default entry in-place. Time values are set
     * as well.
     * 
     * @param loc
     */
    public void setSafeMediumEntry(final ILocationWithLook loc) {
        getSafeMediumEntry().set(loc, ++time, Monotonic.millis());
    }

    /**
     * Update the location for the default entry in-place. Time values are set
     * as well.
     * 
     * @param loc
     */
    public void setLastMoveEntry(final Location loc) {
        getLastMoveEntry().set(loc, ++time, Monotonic.millis());
    }

    /**
     * Update the location for the default entry in-place. Time values are set
     * as well.
     * 
     * @param loc
     */
    public void setLastMoveEntry(final ILocationWithLook loc) {
        getLastMoveEntry().set(loc, ++time, Monotonic.millis());
    }

    /**
     * Get a valid 'mid-term' entry. If that entry is not valid, fall back to
     * the default entry. If neither entry is valid, return null. returned.
     * 
     * @param world
     * @return
     */
    public SetBackEntry getValidMidTermEntry() {
        return getValidEntry(indexMidTerm, true);
    }

    /**
     * Get a valid 'safe-medium' entry. If that entry is not valid, fall back to
     * the default entry. If neither entry is valid, return null.
     * 
     * @param world
     * @return
     */
    public SetBackEntry getValidSafeMediumEntry() {
        return getValidEntry(indexSafeMedium, true);
    }

    /**
     * Get a valid 'last-move' entry. If that entry is not valid, fall back to
     * the default entry. If neither entry is valid, return null.
     * 
     * @param world
     * @return
     */
    public SetBackEntry getValidLastMoveEntry() {
        return getValidEntry(indexLastMove, true);
    }

}
