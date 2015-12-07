package fr.neatmonster.nocheatplus.compat.blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.ds.map.LinkedCoordHashMap;
import fr.neatmonster.nocheatplus.utilities.ds.map.LinkedCoordHashMap.MoveOrder;


public class BlockChangeTracker {
    /** These blocks certainly can't be pushed nor pulled. */
    public static long F_MOVABLE_IGNORE = BlockProperties.F_LIQUID;
    /** These blocks might be pushed or pulled. */
    public static long F_MOVABLE = BlockProperties.F_GROUND | BlockProperties.F_SOLID;

    public static enum Direction {
        NONE,
        X_POS,
        X_NEG,
        Y_POS,
        Y_NEG,
        Z_POS,
        Z_NEG;

        public static Direction getDirection(final BlockFace blockFace) {
            final int x = blockFace.getModX();
            if (x == 1) {
                return X_POS;
            }
            else if (x == -1) {
                return X_NEG;
            }
            final int y = blockFace.getModY();
            if (y == 1) {
                return Y_POS;
            }
            else if (y == -1) {
                return Y_NEG;
            }
            final int z = blockFace.getModZ();
            if (z == 1) {
                return Z_POS;
            }
            else if (z == -1) {
                return Z_NEG;
            }
            return NONE;
        }

    }

    public static class WorldNode {
        public final LinkedCoordHashMap<LinkedList<BlockChangeEntry>> blocks = new LinkedCoordHashMap<LinkedList<BlockChangeEntry>>();
        // TODO: Filter mechanism for player activity by chunks or chunk sections (some margin, only add if activity, let expire by tick).
        /** Tick of last change. */
        public int lastChangeTick = 0;

        /** Total number of BlockChangeEntry instances. */
        public int size = 0;

        public final UUID worldId;

        public WorldNode(UUID worldId) {
            this.worldId = worldId;
        }

        public void clear() {
            blocks.clear();
            size = 0;
        }
    }

    /**
     * Record a state of a block.
     * 
     * @author asofold
     *
     */
    public static class BlockChangeEntry {
        public final long id;
        public final int tick, x, y, z;
        public final Direction direction;

        /**
         * A push entry.
         * @param id
         * @param tick
         * @param x
         * @param y
         * @param z
         * @param direction
         */
        public BlockChangeEntry(long id,  int tick, int x, int y, int z, Direction direction) {
            this.id = id;
            this.tick = tick;
            this.x = x;
            this.y = y;
            this.z = z;
            this.direction = direction;
        }

        // Might follow: Id, data, block shape. Convenience methods for testing.
    }

    public static class BlockChangeListener implements Listener {
        private final BlockChangeTracker tracker;
        private final boolean retractHasBlocks;
        private boolean enabled = true;
        public BlockChangeListener(final BlockChangeTracker tracker) {
            this.tracker = tracker;
            if (ReflectionUtil.getMethodNoArgs(BlockPistonRetractEvent.class, "getBlocks") == null) {
                retractHasBlocks = false;
                NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Assume legacy piston behavior.");
            } else {
                retractHasBlocks = true;
            }
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }

        private BlockFace getDirection(final Block pistonBlock) {
            final MaterialData data = pistonBlock.getState().getData();
            if (data instanceof Directional) {
                Directional directional = (Directional) data;
                return directional.getFacing();
            }
            return null;
        }

        /**
         * Get the direction, in which blocks are or would be moved (towards the piston).
         * 
         * @param pistonBlock
         * @param eventDirection
         * @return
         */
        private BlockFace getRetractDirection(final Block pistonBlock, final BlockFace eventDirection) {
            // Tested for pistons directed upwards.
            // TODO: Test for pistons directed downwards, N, W, S, E.
            // TODO: distinguish sticky vs. not sticky.
            final BlockFace pistonDirection = getDirection(pistonBlock);
            if (pistonDirection == null) {
                return eventDirection;
            }
            else {
                return eventDirection.getOppositeFace();
            }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPistonExtend(final BlockPistonExtendEvent event) {
            if (!enabled) {
                return;
            }
            final BlockFace direction = event.getDirection();
            //DebugUtil.debug("EXTEND event=" + event.getDirection() + " piston=" + getDirection(event.getBlock()));
            tracker.addPistonBlocks(event.getBlock().getRelative(direction), direction, event.getBlocks());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPistonRetract(final BlockPistonRetractEvent event) {
            if (!enabled) {
                return;
            }
            final List<Block> blocks;
            if (retractHasBlocks) {
                // TODO: Legacy: Set flag in constructor (getRetractLocation).
                blocks = event.getBlocks();
            }
            else {
                // TODO: Use getRetractLocation.
                @SuppressWarnings("deprecation")
                final Location retLoc = event.getRetractLocation();
                if (retLoc == null) {
                    blocks = null;
                } else {
                    final Block retBlock = retLoc.getBlock();
                    final long flags = BlockProperties.getBlockFlags(retBlock.getType());
                    if ((flags & F_MOVABLE_IGNORE) == 0L && (flags & F_MOVABLE) != 0L) {
                        blocks = new ArrayList<Block>(1);
                        blocks.add(retBlock);
                    } else {
                        blocks = null;
                    }
                }
            }
            // TODO: Special cases (don't push upwards on retract, with the resulting location being a solid block).
            final Block pistonBlock = event.getBlock();
            final BlockFace direction = getRetractDirection(pistonBlock, event.getDirection());
            //DebugUtil.debug("RETRACT event=" + event.getDirection() + " piston=" + getDirection(event.getBlock()) + " decide=" + getRetractDirection(event.getBlock(),  event.getDirection()));
            tracker.addPistonBlocks(pistonBlock.getRelative(direction.getOppositeFace()), direction, blocks);
        }
    }

    /** Change id/count, increasing with each entry added internally. */
    private long maxChangeId = 0;

    private int expirationAgeTicks = 80; // TODO: Configurable.
    private int worldNodeSkipSize = 500; // TODO: Configurable.

    /**
     * Store the WorldNode instances by UUID, containing the block change
     * entries (and filters). Latest entries must be sorted to the end.
     */
    private final Map<UUID, WorldNode> worldMap = new LinkedHashMap<UUID, BlockChangeTracker.WorldNode>();

    /** Use to avoid duplicate entries with pistons. Always empty after processing. */
    private final Set<Block> processBlocks = new HashSet<Block>();

    // TODO: Consider tracking regions of player activity (chunk sections, with a margin around the player) and filter.

    /**
     * Process the data, as given by a BlockPistonExtendEvent or
     * BlockPistonRetractEvent.
     * 
     * @param pistonBlock
     *            This block is added directly, unless null.
     * @param blockFace
     * @param movedBlocks
     *            Unless null, each block and the relative block in the given
     *            direction (!) are added.
     */
    public void addPistonBlocks(final Block pistonBlock, final BlockFace blockFace, final List<Block> movedBlocks) {
        final int tick = TickTask.getTick();
        final UUID worldId = pistonBlock.getWorld().getUID();
        WorldNode worldNode = worldMap.get(worldId);
        if (worldNode == null) {
            // TODO: With activity tracking this should be a return.
            worldNode = new WorldNode(worldId);
            worldMap.put(worldId, worldNode);
        }
        // TODO: (else) With activity tracking still check if lastActivityTick is too old (lazily expire entire worlds).
        final long changeId = ++maxChangeId;
        // Avoid duplicates by adding to a set.
        if (pistonBlock != null) {
            processBlocks.add(pistonBlock);
        }
        if (movedBlocks != null) {
            for (final Block movedBlock : movedBlocks) {
                processBlocks.add(movedBlock);
                processBlocks.add(movedBlock.getRelative(blockFace));
            }
        }
        // Process queued blocks.
        for (final Block block : processBlocks) {
            addPistonBlock(changeId, tick, worldNode, block, blockFace);
        }
        processBlocks.clear();
    }

    /**
     * Add a block moved by a piston (or the piston itself).
     * 
     * @param changeId
     * @param tick
     * @param worldId
     * @param block
     * @param blockFace
     */
    private void addPistonBlock(final long changeId, final int tick, final WorldNode worldNode, final Block targetBlock, final BlockFace blockFace) {
        // TODO: A filter for regions of player activity.
        // TODO: Test which ones can actually push a player (and what type of push).
        // Add this block.
        addBlockChange(changeId, tick, worldNode, targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), Direction.getDirection(blockFace));
    }

    /**
     * Add a block change. Simplistic version (no actual block states/shapes are
     * stored).
     * 
     * @param x
     * @param y
     * @param z
     * @param direction
     *            If not NONE, pushing into that direction is assumed.
     */
    private void addBlockChange(final long changeId, final int tick, final WorldNode worldNode, final int x, final int y, final int z, final Direction direction) {
        worldNode.lastChangeTick = tick;
        final BlockChangeEntry entry = new BlockChangeEntry(changeId, tick, x, y, z, direction);
        LinkedList<BlockChangeEntry> entries = worldNode.blocks.get(x, y, z, MoveOrder.END);
        if (entries == null) {
            entries = new LinkedList<BlockChangeTracker.BlockChangeEntry>();
            worldNode.blocks.put(x, y, z, entries, MoveOrder.END); // Add to end.
        } else {
            // Lazy expiration check for this block.
            if (!entries.isEmpty() && entries.getFirst().tick < tick - expirationAgeTicks) {
                worldNode.size -= expireEntries(tick - expirationAgeTicks, entries);
            }
        }
        // With tracking actual block states/shapes, an entry for the previous state must be present (update last time or replace last or create first).
        entries.add(entry); // Add latest to the end always.
        worldNode.size ++;
        //DebugUtil.debug("Add block change: " + x + "," + y + "," + z + " " + direction + " " + changeId); // TODO: REMOVE
    }

    private int expireEntries(final int olderThanTick, final LinkedList<BlockChangeEntry> entries) {
        int removed = 0;
        final Iterator<BlockChangeEntry> it = entries.iterator();
        while (it.hasNext()) {
            if (it.next().tick < olderThanTick) {
                it.remove();
                removed ++;
            } else {
                return removed;
            }
        }
        return removed;
    }

    /**
     * Check expiration on tick.
     * 
     * @param currentTick
     */
    public void checkExpiration(final int currentTick) {
        final int olderThanTick = currentTick - expirationAgeTicks;
        final Iterator<Entry<UUID, WorldNode>> it = worldMap.entrySet().iterator();
        while (it.hasNext()) {
            final WorldNode worldNode = it.next().getValue();
            if (worldNode.lastChangeTick < olderThanTick) {
                worldNode.clear();
                it.remove();
            } else {
                // Check for expiration of individual blocks.
                if (worldNode.size < worldNodeSkipSize) {
                    continue;
                }
                final Iterator<fr.neatmonster.nocheatplus.utilities.ds.map.CoordMap.Entry<LinkedList<BlockChangeEntry>>> blockIt = worldNode.blocks.iterator();
                while (blockIt.hasNext()) {
                    final LinkedList<BlockChangeEntry> entries = blockIt.next().getValue();
                    if (!entries.isEmpty()) {
                        if (entries.getFirst().tick < olderThanTick) {
                            worldNode.size -= expireEntries(olderThanTick, entries);
                        }
                    }
                    if (entries.isEmpty()) {
                        blockIt.remove();
                    }
                }
                if (worldNode.size == 0) {
                    // TODO: With activity tracking, nodes get removed based on last activity only.
                    it.remove();
                }
            }
        }
    }

    /**
     * Query if there is a push available into the indicated direction.
     * 
     * @param gtChangeId
     *            A matching entry must have a greater id than the given one
     *            (all ids are greater than 0).
     * @param tick
     *            The current tick. Used for lazy expiration.
     * @param worldId
     * @param x
     *            Block Coordinates where a push might have happened.
     * @param y
     * @param z
     * @param direction
     *            Desired direction of the push.
     * @return The id of a matching entry, or -1 if there is no matching entry.
     */
    public long getChangeIdPush(final long gtChangeId, final long tick, final UUID worldId, final int x, final int y, final int z, final Direction direction) {
        final WorldNode worldNode = worldMap.get(worldId);
        if (worldNode == null) {
            return -1;
        }
        return getChangeIdPush(gtChangeId, tick, worldNode, x, y, z, direction);
    }

    /**
     * Query if there is a push available into the indicated direction.
     * 
     * @param gtChangeId
     *            A matching entry must have a greater id than the given one
     *            (all ids are greater than 0).
     * @param tick
     *            The current tick. Used for lazy expiration.
     * @param worldNode
     * @param x
     *            Block Coordinates where a push might have happened.
     * @param y
     * @param z
     * @param direction
     *            Desired direction of the push. Pass null to ignore direction.
     * @return The id of the oldest matching entry, or -1 if there is no
     *         matching entry.
     */
    private long getChangeIdPush(final long gtChangeId, final long tick, final WorldNode worldNode, final int x, final int y, final int z, final Direction direction) {
        // TODO: Might add some policy (start at age, oldest first, newest first).
        final long olderThanTick = tick - expirationAgeTicks;
        // Lazy expiration of entire world nodes.
        if (worldNode.lastChangeTick < olderThanTick) {
            worldNode.clear();
            worldMap.remove(worldNode.worldId);
            //DebugUtil.debug("EXPIRE WORLD"); // TODO: REMOVE
            return -1;
        }
        // Check individual entries.
        final LinkedList<BlockChangeEntry> entries = worldNode.blocks.get(x, y, z);
        if (entries == null) {
            //DebugUtil.debug("NO ENTRIES: " + x + "," + y + "," + z);
            return -1;
        }
        //DebugUtil.debug("Entries at: " + x + "," + y + "," + z);
        final Iterator<BlockChangeEntry> it = entries.iterator();
        while (it.hasNext()) {
            final BlockChangeEntry entry = it.next();
            if (entry.tick < olderThanTick) {
                //DebugUtil.debug("Lazy expire: " + x + "," + y + "," + z + " " + entry.id);
                it.remove();
            } else {
                if (entry.id > gtChangeId && (direction == null || entry.direction == direction)) {
                    return entry.id;
                }
            }
        }
        // Remove entries from map + remove world if empty.
        if (entries.isEmpty()) {
            worldNode.blocks.remove(x, y, z);
            if (worldNode.size == 0) {
                worldMap.remove(worldNode.worldId);
            }
        }
        return -1;
    }

    public void clear() {
        for (final WorldNode worldNode : worldMap.values()) {
            worldNode.clear();
        }
        worldMap.clear();
    }

    public int size() {
        int size = 0;
        for (final WorldNode worldNode : worldMap.values()) {
            size += worldNode.size;
        }
        return size;
    }

}
