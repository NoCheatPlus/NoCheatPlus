package fr.neatmonster.nocheatplus.checks.moving.location.setback;

import org.bukkit.Location;

/**
 * Represent a set-back location storage, allowing for more or less efficient
 * keeping track of multiple set-backs for use within one overall checking
 * context (e.g. player moving, or vehicle moving).
 * 
 * @author asofold
 *
 */
public abstract class SetBackStorage {

    final SetBackEntry[] entries;
    final int defaultIndex;
    /** Count times setting a set-back. */
    int time;

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
     * @param msTime
     */
    public void resetAll(final Location loc, final long msTime) {
        time ++;
        for (int i = 0; i < entries.length; i++) {
            entries[i].set(loc, time, msTime);
        }
    }

    /**
     * Resets all entries to the given location. Internal time is increased.
     * 
     * @param loc
     * @param msTime
     */
    public void resetLazily(final Location loc, final long msTime) {
        invalidateAll();
        entries[defaultIndex].set(loc, ++time, msTime);
    }

}
