/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.compat.blocks.changetracker;

import java.util.LinkedList;
import java.util.ListIterator;

import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeTracker.BlockChangeEntry;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache.IBlockCacheNode;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

/**
 * Maintain consistent past states for a block and the block above.
 * 
 * @author asofold
 *
 */
public class OnGroundReference {

    // TODO: More simplified opportunistic variant?
    // TODO: Include/exclude other edge cases :p.

    /*
     * TODO: Consider adding a super interface/abstract, to make the
     * implementation interchangeable (aiming at a very simple implementation,
     * configurable).
     */
    // TODO: (Ever detach this to be used from within BlockProperties?)

    private BlockCache blockCache = null;
    private BlockChangeReference ref = null;
    private long ignoreFlags = 0L;

    private LinkedList<BlockChangeEntry> entries = null;
    private ListIterator<BlockChangeEntry> itEntries = null;
    private BlockChangeEntry entry = null;
    private IBlockCacheNode node = null;

    private LinkedList<BlockChangeEntry> entriesAbove = null;
    private ListIterator<BlockChangeEntry> itEntriesAbove = null;
    private BlockChangeEntry entryAbove = null;
    private IBlockCacheNode nodeAbove = null;

    private int entriesAboveLockIndex = 0;

    public void init(BlockCache blockCache, BlockChangeReference ref, long ignoreFlags) {
        this.blockCache = blockCache;
        this.ref = ref;
        this.ignoreFlags = ignoreFlags;
    }

    public void setEntries(LinkedList<BlockChangeEntry> entries) {
        this.entries = entries;
        this.itEntries = null;
        this.entry = null;
        this.node = null;
    }

    public void setEntriesAbove(LinkedList<BlockChangeEntry> entriesAbove) {
        this.entriesAbove = entriesAbove;
        this.itEntriesAbove = null;
        this.entryAbove = null;
        this.nodeAbove = null;
    }

    public void moveDown() {
        entriesAbove = entries;
        itEntriesAbove = null; //  Gets overridden if not null.
        entryAbove = null;
        nodeAbove = node; // Gets overridden, if entriesAbove are not null.
        // TODO: Consider to set other fields to null.
    }

    public void updateSpan() {
        if (entry != null) {
            ref.updateSpan(entry);
        }
        if (entryAbove != null) {
            ref.updateSpan(entryAbove);
        }
    }

    /**
     * Detach all.
     */
    public void clear() {
        ignoreFlags = 0L;
        ref = null;
        entries = entriesAbove = null;
        itEntries = itEntriesAbove = null;
        entry = entryAbove = null;
        node = nodeAbove = null;
    }

    public boolean hasAnyEntries() {
        return entries != null || entriesAbove != null;
    }

    public IBlockCacheNode getNode() {
        return node;
    }

    public IBlockCacheNode getNodeAbove() {
        return nodeAbove;
    }

    /**
     * Initialize basics, nodes, etc. Only call after having ensured, that there
     * are any entries (hasAnyEntries).
     * 
     * @return Returns true, if a usable entry or pair of entries has been set.
     *         If no suitable entries exist from start, false is returned.
     */
    public boolean initEntries(final int x, final int y, final int z) {

        // TODO: Much of cleanup + tests + delegate to auxiliary methods (shrink code size in this method).
        // TODO: Evaluate which is the most likely most often called part(s) ?

        itEntries = entries == null ? null : entries.listIterator();
        itEntriesAbove = entriesAbove == null ? null : entriesAbove.listIterator();
        entriesAboveLockIndex = 0;

        // First align to the minimum time, according to ref(!).
        if (itEntries != null) {
            while(itEntries.hasNext()) {
                entry = itEntries.next();
                if (ref != null && !ref.canUpdateWith(entry)
                        || !BlockProperties.isGround(entry.previousState.getType(), ignoreFlags)) {
                    entry = null;
                }
                else {
                    // Start with this entry.
                    break;
                }
            }
        }
        // Only fetch nodes once, if no entries are there.
        if (entry == null) {
            node = blockCache.getOrCreateBlockCacheNode(x, y, z, false);
            // Fast exclusion check right here.
            if (!BlockProperties.isGround(node.getType(), ignoreFlags)) {
                entriesAbove = entries;
                return false;
            }
        }
        else {
            node = null;
        }
        itEntriesAbove = entriesAbove == null ? null : entriesAbove.listIterator();
        if (itEntriesAbove != null) {
            while(itEntriesAbove.hasNext()) {
                entryAbove = itEntriesAbove.next();
                if (ref != null && !ref.canUpdateWith(entryAbove)) {
                    entryAbove = null;
                }
                else {
                    // Start with this entry.
                    break;
                }
            }
        }
        if (entry == null && entryAbove == null) {
            // Skip these.
            entriesAbove = entries;
            return false; // Next y.
        }
        if (entry != null && entryAbove != null 
                && !entry.overlapsIntervalOfValidity(entryAbove)) {
            // Wind the "older one" of the iterators forward until first match.
            if (entryAbove.nextEntryTick >= 0 && entry.tick > entryAbove.nextEntryTick) {
                entryAbove = null;
                while (itEntriesAbove.hasNext()) {
                    entryAbove = itEntriesAbove.next();
                    if (entryAbove.nextEntryTick >= 0 && entry.tick > entryAbove.nextEntryTick) {
                        entryAbove = null;
                    }
                    else {
                        break;
                    }
                }
            }
            else if (entry.nextEntryTick >= 0 && entryAbove.tick > entry.nextEntryTick) {
                entry = null;
                while(itEntries.hasNext()) {
                    entry = itEntries.next();
                    if (entry.nextEntryTick >= 0 && entryAbove.tick > entry.nextEntryTick
                            || !BlockProperties.isGround(entry.previousState.getType(), ignoreFlags)) {
                        entry = null;
                    }
                    else {
                        break;
                    }
                }
            }
            else {
                throw new IllegalStateException("Unintended pun.");
            }
            if (entry == null && entryAbove == null) {
                return false;
            }
            if (entry == null) {
                node = blockCache.getOrCreateBlockCacheNode(x, y, z, false);
                // Fast exclusion check right here.
                if (!BlockProperties.isGround(node.getType(), ignoreFlags)) {
                    return false;
                }
            }
        }
        if (nodeAbove == null) {
            if (entryAbove == null) {
                // Use the current state.
                nodeAbove = blockCache.getOrCreateBlockCacheNode(x, y + 1, z, false);
            }
            else {
                nodeAbove = entryAbove.previousState;
            }
        }
        if (node == null) {
            if (entry == null) {
                // Use the current state.
                node= blockCache.getOrCreateBlockCacheNode(x, y , z, false);
            }
            else {
                node = entry.previousState;
            }
        }
        return true;
    }

    /**
     * Advance pair-iteration state.
     * 
     * @return Returns true, if a usable entry or pair of entries has been set.
     *         If no suitable entry could be found, false is returned.
     */
    public boolean advance() {

        // TODO: Evaluate which is the most likely most often called part(s) ?

        if (entries == null) { // TODO: Which to test for: entries or entry?
            if (itEntriesAbove.hasNext()) {
                entryAbove = itEntriesAbove.next();
                nodeAbove = entryAbove.previousState;
                return true;
            }
            else {
                // No more entries to check.
                return false;
            }
        }
        else if (entriesAbove == null) {
            entry = null;
            while (itEntries.hasNext()) {
                entry = itEntries.next();
                node = entry.previousState;
                if (BlockProperties.isGround(node.getType(), ignoreFlags)) {
                    // TODO: If nodeAbove is ground too, could exclude cases here.
                    return true;
                }
                else {
                    entry = null;
                    node = null;
                }
            }
            if (entry == null){
                // No more entries to check.
                return false;
            }
            else {
                return true;
            }
        }
        else {
            // Both not null (that case has been excluded above).
            return advanceDualEntries();
        }
    }

    private boolean advanceDualEntries() {
        // Re-iterate, if necessary.
        while (true) {
            // Try to iterate aboveEntries first.
            entryAbove = null;
            nodeAbove = null;
            while (itEntriesAbove.hasNext()) {
                entryAbove = itEntriesAbove.next();
                nodeAbove = entryAbove.previousState;
                if (entry == null) {
                    // Iterate to end. (Then stop, rewind shouldn't happen.)
                    return true;
                }
                else {
                    if (entry.nextEntryTick >= 0 && entryAbove.tick > entry.nextEntryTick) {
                        entryAbove = null;
                        nodeAbove = null;
                        break;
                    }
                    else if (entry.overlapsIntervalOfValidity(entryAbove)) {
                        // Good to use.
                        return true;
                    }
                    else {
                        entryAbove = null;
                        nodeAbove = null;
                        // Check the next one (not out of range yet).
                    }
                }
            }

            // Rewind entryAbove, advance entry.
            entry = null;
            node = null;
            if (itEntries.hasNext()) {
                entry = itEntries.next();
                node = entry.previousState;
                // TODO: Skip if not ground (!).
                // Rewind.
                while (itEntriesAbove.nextIndex() > entriesAboveLockIndex) {
                    entryAbove = itEntriesAbove.previous();
                    nodeAbove = entryAbove.previousState;
                    // TODO: Consider optimized break here.
                }
                // Advance towards next overlap.
                if (entryAbove != null) {
                    while (!entry.overlapsIntervalOfValidity(entryAbove)) {
                        if (entry.nextEntryTick >= 0 && entryAbove.tick > entry.nextEntryTick) {
                            // Try next entry.
                            entryAbove = null;
                            nodeAbove = null;
                            break;
                        }
                        if (itEntriesAbove.hasNext()) {
                            entryAbove = itEntriesAbove.next();
                            nodeAbove = entryAbove.previousState;
                            entriesAboveLockIndex = itEntriesAbove.nextIndex();
                        }
                        else {
                            // Try next entry.
                            entryAbove = null;
                            nodeAbove = null;
                            // TODO: Consider allow checking entry + null once.
                            break;
                        }
                    }
                    if (entryAbove != null) {
                        return true;
                    }
                }
            }
            else {
                /*
                 * TODO: Cover current state for entry vs. last of entriesAbove
                 * for the very last thing? (same for current above state vs.
                 * last of entries).
                 */
                entry = entryAbove = null;
                node = nodeAbove = null;
                return false;
            }

            if (entry == null && entryAbove == null) { 
                /*
                 * TODO: Should be dead code, ensure all cases except for
                 * "Try next entry." are covered above.
                 */
                return false;
            }
        } // (while: Find matching pair to continue with)
    }

}
