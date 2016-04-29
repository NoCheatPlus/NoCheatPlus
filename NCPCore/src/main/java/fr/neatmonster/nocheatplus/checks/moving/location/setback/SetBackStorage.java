package fr.neatmonster.nocheatplus.checks.moving.location.setback;

import org.bukkit.Location;

import fr.neatmonster.nocheatplus.time.monotonic.Monotonic;

/**
 * Represent a set-back location storage, allowing for more or less efficient
 * keeping track of multiple set-backs for use within one overall checking
 * context (e.g. player moving, or vehicle moving). For timing values, a counter
 * is used as time and a monotonic clock for msTime (static primary thread
 * clock: Monotonic.millis()).
 * 
 * @author asofold
 *
 */
public class SetBackStorage {

    final SetBackEntry[] entries;
    final int defaultIndex;
    /** Count times setting a set-back. */
    int time;

    /**
     * 
     * @param size
     * @param defaultIndex < 0 if not meant to be used.
     */
    public SetBackStorage(final int size, final int defaultIndex) {
        entries = new SetBackEntry[size];
        for (int i = 0; i < size; i++) {
            entries[i] = new SetBackEntry();
        }
        this.defaultIndex = defaultIndex;
    }

    /**
     * Oldest by time value.
     * 
     * @return
     */
    public SetBackEntry getOldestValidEntry() {
        int oldestTime = Integer.MAX_VALUE;
        SetBackEntry oldestEntry = null;
        for (int i = 0; i < entries.length; i++) {
            final SetBackEntry entry = entries[i];
            if (entry.isValid() && entry.getTime() < oldestTime) {
                oldestTime = entry.getTime();
                oldestEntry = entry;
            }
        }
        return oldestEntry;
    }

    /**
     * Retrieve the entry at a given index, only if valid. Set fallBack to true,
     * in order to fall back to the default entry, if a default index is set,
     * and if that entry is valid
     * 
     * @param index
     * @param fallBack
     *            If to allow falling back to the default entry.
     * @return
     */
    public SetBackEntry getEntry(final int index, final boolean fallBack) {
        if (entries[index].isValid()) {
            return entries[index];
        }
        if (fallBack && defaultIndex >= 0 && entries[defaultIndex].isValid()) {
            return entries[defaultIndex];
        }
        return null;
    }

    public void invalidateAll() {
        for (int i = 0; i < entries.length; i++) {
            entries[i].setValid(false);
        }
    }

    /**
     * Hard-reset all entries to the given location. All will have the same time
     * value.
     * 
     * @param loc
     */
    public void resetAll(final Location loc) {
        time ++;
        for (int i = 0; i < entries.length; i++) {
            entries[i].set(loc, time, Monotonic.millis());
        }
    }

    /**
     * Invalidate all entries and reset the default to the given location.
     * Internal time is increased. If no default is set, resetAll is called.
     * 
     * @param loc
     */
    public void resetAllLazily(final Location loc) {
        if (defaultIndex < 0) {
            resetAll(loc);
        }
        else {
            invalidateAll();
            entries[defaultIndex].set(loc, ++time, Monotonic.millis());
        }
    }

    /**
     * Maximum number of stored locations, disregarding validity.
     * 
     * @return
     */
    public int size() {
        return entries.length;
    }

    /**
     * Test if any of the stored set-back location is valid.
     * 
     * @return
     */
    public boolean isAnySetBackValid() {
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].isValid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test if the location stored at index is valid.
     * 
     * @param index
     * @return
     */
    public boolean isSetBackValid(final int index) {
        return entries[index].isValid();
    }


    public boolean isDefaultSetBackValid() {
        return defaultIndex >= 0 && entries[defaultIndex].isValid();
    }

    // TODO: Has/get/set for location, ILocation (IPosition for keeping the world + default world?).

}
