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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Directional;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.BridgeMaterial;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder;
import fr.neatmonster.nocheatplus.event.mini.MiniListener;
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

    /** Default tag for listeners. */
    private final String defaultTag = "system.nocheatplus.blockchangetracker";

    /**
     * NOTE: Using MiniListenerWithOrder (and @Override before @EventHandler)
     * would make the registry attempt to register with Bukkit for 'Object'.
     */
    private final MiniListener<?>[] miniListeners = new MiniListener<?>[] {
        new MiniListener<BlockRedstoneEvent>() {
            @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
            @RegisterMethodWithOrder(tag = defaultTag)
            @Override
            public void onEvent(BlockRedstoneEvent event) {
                if (enabled) {
                    onBlockRedstone(event);
                }
            }
        },
        new MiniListener<EntityChangeBlockEvent>() {
            @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
            @RegisterMethodWithOrder(tag = defaultTag)
            @Override
            public void onEvent(EntityChangeBlockEvent event) {
                if (enabled) {
                    onEntityChangeBlock(event);
                }
            }
        },
        new MiniListener<BlockPistonExtendEvent>() {
            @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
            @RegisterMethodWithOrder(tag = defaultTag)
            @Override
            public void onEvent(BlockPistonExtendEvent event) {
                if (enabled) {
                    onPistonExtend(event);
                }
            }
        },
        new MiniListener<BlockPistonRetractEvent>() {
            @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
            @RegisterMethodWithOrder(tag = defaultTag)
            @Override
            public void onEvent(BlockPistonRetractEvent event) {
                if (enabled) {
                    onPistonRetract(event);
                }
            }
        },
        new MiniListener<PlayerInteractEvent>() {
            // Include cancelled events, due to the use-block part.
            @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
            @RegisterMethodWithOrder(tag = defaultTag)
            @Override
            public void onEvent(PlayerInteractEvent event) {
                if (enabled) {
                    onPlayerInteract(event);
                }
            }
        },
        new MiniListener<BlockFormEvent>() {
            @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
            @RegisterMethodWithOrder(tag = defaultTag)
            @Override
            public void onEvent(BlockFormEvent event) {
                if (enabled) {
                    onBlockForm(event);
                }
            }
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
    };

    public BlockChangeListener(final BlockChangeTracker tracker) {
        this.tracker = tracker;
        if (ReflectionUtil.getMethodNoArgs(BlockPistonRetractEvent.class, "getBlocks") == null) {
            retractHasBlocks = false;
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Assume legacy piston behavior.");
        }
        else {
            retractHasBlocks = true;
        }
    }

    /**
     * Register actual listener(s).
     */
    public void register() {
        // TODO: Replace 'if (enabled)' by actually unregistering the listeners.
        final NoCheatPlusAPI api = NCPAPIProvider.getNoCheatPlusAPI();
        for (final MiniListener<?> listener : miniListeners) {
            api.addComponent(listener);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private BlockFace getDirection(final Block pistonBlock) {
        // TODO: Register/store a fetcher thing (DirectionalFromBlock)
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

    private void onPistonExtend(final BlockPistonExtendEvent event) {
        final BlockFace direction = event.getDirection();
        tracker.addPistonBlocks(event.getBlock().getRelative(direction), direction, event.getBlocks());
    }

    private void onPistonRetract(final BlockPistonRetractEvent event) {
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
        tracker.addPistonBlocks(pistonBlock.getRelative(direction.getOppositeFace()), direction, blocks);
    }

    private void onBlockRedstone(final BlockRedstoneEvent event) {
        final int oldCurrent = event.getOldCurrent();
        final int newCurrent = event.getNewCurrent();
        if (oldCurrent == newCurrent || oldCurrent > 0 && newCurrent > 0) {
            return;
        }
        // TODO: Fine grained enabling state (pistons, doors, other).
        final Block block = event.getBlock();
        // TODO: Abstract method for a block and a set of materials (redstone, interact, ...).
        if (block == null 
                || (BlockProperties.getBlockFlags(block.getType()) | BlockProperties.F_VARIABLE_REDSTONE) == 0) {
            return;
        }
        addRedstoneBlock(block);
    }

    private void addRedstoneBlock(final Block block) {
        addBlockWithAttachedPotential(block, BlockProperties.F_VARIABLE_REDSTONE);
    }

    private void onEntityChangeBlock(final EntityChangeBlockEvent event) {
        final Block block = event.getBlock();
        if (block != null) {
            tracker.addBlocks(block); // E.g. falling blocks like sand.
        }
    }

    private void onPlayerInteract(final PlayerInteractEvent event) {
        // Check preconditions.
        final org.bukkit.event.block.Action action = event.getAction();
        if (action == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            onRightClickBlock(event);
        }
        else if (!event.isCancelled()) {
            if (action == org.bukkit.event.block.Action.PHYSICAL) {
                onInteractPhysical(event);
            }
        }
    }

    private void onInteractPhysical(final PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (block != null) {
            final Material type = block.getType();
            // TODO: Consider a flag.
            if (type == BridgeMaterial.FARMLAND) {
                tracker.addBlocks(block);
            }
        }
    }

    private void onRightClickBlock(final PlayerInteractEvent event) {
        final Result result = event.useInteractedBlock();
        if ((result == Result.ALLOW 
                || result == Result.DEFAULT && !event.isCancelled())) {
            final Block block = event.getClickedBlock();
            if (block != null) {
                final Material type = block.getType();
                // TODO: Dirt/Grass (/Podzol+-spelling) -> flag. Add, if a hoe is used.
                if ((BlockProperties.getBlockFlags(type) | BlockProperties.F_VARIABLE_USE) != 0L) {
                    addBlockWithAttachedPotential(block, BlockProperties.F_VARIABLE_USE);
                }
            }
        }
    }

    private void onBlockForm(final BlockFormEvent event) {
        final Block block = event.getBlock();
        if (block != null) {
            // TODO: Filter by player activity.
            tracker.addBlocks(block);
        }
    }

    /**
     * Add a past state for this block, extending for the other block in case of
     * doors. This is for the case of interaction or redstone level change.
     * 
     * @param block
     * @param relevantFlags
     */
    private void addBlockWithAttachedPotential(final Block block, final long relevantFlags) {
        final MaterialData materialData = block.getState().getData();
        if (materialData instanceof Door) {
            final Door door = (Door) materialData;
            final Block otherBlock = block.getRelative(door.isTopHalf() ? BlockFace.DOWN : BlockFace.UP);
            /*
             * TODO: In case of redstone: Double doors... detect those too? Is it still more
             * efficient than using BlockPhysics with lazy delayed updating
             * (TickListener...). Hinge corner... possibilities?
             */
            if (otherBlock != null // Top of the map / special case.
                    && (BlockProperties.getBlockFlags(otherBlock.getType()) 
                            | relevantFlags) == 0) {
                tracker.addBlocks(block, otherBlock);
                return;
            }
        }
        // Only add the block in question itself.
        tracker.addBlocks(block);
    }

}
