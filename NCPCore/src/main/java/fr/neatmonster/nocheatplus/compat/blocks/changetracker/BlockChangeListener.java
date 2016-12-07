package fr.neatmonster.nocheatplus.compat.blocks.changetracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.material.Directional;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

public class BlockChangeListener implements Listener {

    // TODO: Fine grained configurability (also switch flag in MovingListener to a sub-config).
    // TODO: Coarse player activity filter?

    /** These blocks certainly can't be pushed nor pulled. */
    public static long F_MOVABLE_IGNORE = BlockProperties.F_LIQUID;
    /** These blocks might be pushed or pulled. */
    public static long F_MOVABLE = BlockProperties.F_GROUND | BlockProperties.F_SOLID;

    private final BlockChangeTracker tracker;
    private final boolean retractHasBlocks;
    private boolean enabled = true;
    private final Set<Material> redstoneMaterials = new HashSet<Material>();

    public BlockChangeListener(final BlockChangeTracker tracker) {
        this.tracker = tracker;
        if (ReflectionUtil.getMethodNoArgs(BlockPistonRetractEvent.class, "getBlocks") == null) {
            retractHasBlocks = false;
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Assume legacy piston behavior.");
        }
        else {
            retractHasBlocks = true;
        }
        // TODO: Make an access method to test this/such in BlockProperties!
        for (Material material : Material.values()) {
            if (material.isBlock()) {
                final String name = material.name().toLowerCase();
                if (name.indexOf("door") >= 0 || name.indexOf("gate") >= 0) {
                    redstoneMaterials.add(material);
                }
            }
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
            blocks = event.getBlocks();
        }
        else {
            @SuppressWarnings("deprecation")
            final Location retLoc = event.getRetractLocation();
            if (retLoc == null) {
                blocks = null;
            }
            else {
                final Block retBlock = retLoc.getBlock();
                final long flags = BlockProperties.getBlockFlags(retBlock.getType());
                if ((flags & F_MOVABLE_IGNORE) == 0L && (flags & F_MOVABLE) != 0L) {
                    blocks = new ArrayList<Block>(1);
                    blocks.add(retBlock);
                }
                else {
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

    //        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    //        public void onBlockPhysics (final BlockPhysicsEvent event) {
    //            if (!enabled) {
    //                return;
    //            }
    //            // TODO: Fine grained enabling state (pistons, doors, other).
    //            final Block block = event.getBlock();
    //            if (block == null || !physicsMaterials.contains(block.getType())) {
    //                return;
    //            }
    //            // TODO: MaterialData -> Door, upper/lower half needed ?
    //            tracker.addBlocks(block); // TODO: Skip too fast changing states?
    //            DebugUtil.debug("BlockPhysics: " + block); // TODO: REMOVE
    //        }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockRedstone(final BlockRedstoneEvent event) {
        if (!enabled) {
            return;
        }
        final int oldCurrent = event.getOldCurrent();
        final int newCurrent = event.getNewCurrent();
        if (oldCurrent == newCurrent || oldCurrent > 0 && newCurrent > 0) {
            return;
        }
        // TODO: Fine grained enabling state (pistons, doors, other).
        final Block block = event.getBlock();
        // TODO: Abstract method for a block and a set of materials (redstone, interact, ...).
        if (block == null || !redstoneMaterials.contains(block.getType())) {
            return;
        }
        addRedstoneBlock(block);
    }

    private void addRedstoneBlock(final Block block) {
        final MaterialData materialData = block.getState().getData();
        if (materialData instanceof Door) {
            final Door door = (Door) materialData;
            final Block otherBlock = block.getRelative(door.isTopHalf() ? BlockFace.DOWN : BlockFace.UP);
            /*
             * TODO: Double doors... detect those too? Is it still more
             * efficient than using BlockPhysics with lazy delayed updating
             * (TickListener...). Hinge corner... possibilities?
             */
            if (redstoneMaterials.contains(otherBlock.getType())) {
                tracker.addBlocks(block, otherBlock);
                // DebugUtil.debug("BlockRedstone door: " + block + " / " + otherBlock); // TODO: REMOVE
                return;
            }
        }
        // Only the single block remains.
        tracker.addBlocks(block);
        // DebugUtil.debug("BlockRedstone: " + block); // TODO: REMOVE
    }

    //    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    //    public void onEntityFormBlock(final EntityBlockFormEvent event) {
    //        if (!enabled) {
    //            return;
    //        }
    //        final Block block = event.getBlock();
    //        if (block != null) {
    //            // TODO: Filters?
    //            tracker.addBlocks(block);
    //            DebugUtil.debug("EntityFormBlock: " + block); // TODO: REMOVE
    //        }
    //    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        if (!enabled) {
            return;
        }
        final Block block = event.getBlock();
        if (block != null) {
            // TODO: Filters?
            tracker.addBlocks(block); // E.g. falling blocks like sand.
            //DebugUtil.debug("EntityChangeBlock: " + block); // TODO: REMOVE
        }
    }

}
