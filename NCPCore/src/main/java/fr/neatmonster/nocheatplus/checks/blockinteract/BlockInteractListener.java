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
package fr.neatmonster.nocheatplus.checks.blockinteract;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.util.MovingUtil;
import fr.neatmonster.nocheatplus.checks.net.FlyingQueueHandle;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.InventoryUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

/**
 * Central location to listen to events that are relevant for the block interact checks.
 * 
 * @see BlockInteractEvent
 */
public class BlockInteractListener extends CheckListener {

    /** The looking-direction check. */
    private final Direction direction = addCheck(new Direction());

    /** The reach-distance check. */
    private final Reach     reach     = addCheck(new Reach());

    /** Interact with visible blocks. */
    private final Visible visible = addCheck(new Visible());

    /** Speed of interaction. */
    private final Speed speed = addCheck(new Speed());

    /** For temporary use: LocUtil.clone before passing deeply, call setWorld(null) after use. */
    private final Location useLoc = new Location(null, 0, 0, 0);

    private final Counters counters = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class);
    private final int idCancelDead = counters.registerKey("canceldead");

    public BlockInteractListener() {
        super(CheckType.BLOCKINTERACT);
    }

    /**
     * We listen to PlayerInteractEvent events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    protected void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        // Early cancel for interact events with dead players and other.
        final int cancelId;
        if (player.isDead() && BridgeHealth.getHealth(player) <= 0.0) { // TODO: Should be dead !?.
            // Auto-soup after death.
            /*
             * TODO: Allow physical interact after death? Risks could be command
             * blocks used etc.
             */
            cancelId = idCancelDead;
        }
        else if (MovingUtil.hasScheduledPlayerSetBack(player)) {
            // Might log.
            cancelId = -1; // No counters yet, but do prevent.
        }
        else {
            cancelId = Integer.MIN_VALUE;
        }
        if (cancelId != Integer.MIN_VALUE) {
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            event.setCancelled(true);
            if (cancelId >= 0) {
                counters.addPrimaryThread(cancelId, 1);
            }
            return;
        }

        // TODO: Re-arrange for interact spamming. (With ProtocolLib something else is in place as well.)
        final Action action = event.getAction();
        final Block block = event.getClickedBlock();
        final BlockInteractData data = BlockInteractData.getData(player);
        final int previousLastTick = data.lastTick;
        // TODO: Last block setting: better on monitor !?.
        if (block == null) {
            data.resetLastBlock();
        }
        else {
            data.setLastBlock(block, action);
        }
        final BlockFace face = event.getBlockFace();
        final ItemStack stack;
        switch(action) {
            case RIGHT_CLICK_AIR:
                // TODO: What else to adapt?
            case LEFT_CLICK_AIR:
                // TODO: What else to adapt?
            case LEFT_CLICK_BLOCK:
                stack = null;
                break;
            case RIGHT_CLICK_BLOCK:
                stack = Bridge1_9.getUsedItem(player, event);
                if (stack != null && stack.getType() == Material.ENDER_PEARL) {
                    checkEnderPearlRightClickBlock(player, block, face, event, previousLastTick, data);
                }
                break;
            default:
                return;
        }

        boolean cancelled = false;
        if (event.isCancelled() && event.useInteractedBlock() != Result.ALLOW) {
            data.subsequentCancel ++;
            return;
        }

        final BlockInteractConfig cc = BlockInteractConfig.getConfig(player);
        boolean preventUseItem = false;

        final Location loc = player.getLocation(useLoc);
        final FlyingQueueHandle flyingHandle = new FlyingQueueHandle(player);

        // TODO: Always run all checks, also for !isBlock ?

        // Interaction speed.
        if (!cancelled && speed.isEnabled(player) && speed.check(player, data, cc)) {
            cancelled = true;
            preventUseItem = true;
        }

        if (block != null) {
            // First the reach check.
            if (!cancelled && reach.isEnabled(player) 
                    && reach.check(player, loc, block, flyingHandle, data, cc)) {
                cancelled = true;
            }

            // Second the direction check
            if (!cancelled && direction.isEnabled(player) 
                    && direction.check(player, loc, block, flyingHandle, data, cc)) {
                cancelled = true;
            }

            // Ray tracing for freecam use etc.
            if (!cancelled && visible.isEnabled(player) 
                    && visible.check(player, loc, block, face, action, flyingHandle, data, cc)) {
                cancelled = true;
            }
        }

        // If one of the checks requested to cancel the event, do so.
        if (cancelled) {
            onCancelInteract(player, block, face, event, previousLastTick, preventUseItem, data, cc);
        }
        else {
            data.subsequentCancel = 0;
        }
        useLoc.setWorld(null);
    }

    private void onCancelInteract(final Player player, final Block block, final BlockFace face, 
            final PlayerInteractEvent event, final int previousLastTick, final boolean preventUseItem, 
            final BlockInteractData data, final BlockInteractConfig cc) {
        if (event.isCancelled()) {
            // Just prevent using the block.
            event.setUseInteractedBlock(Result.DENY);
            if (data.debug) {
                genericDebug(player, block, face, event, "already cancelled: deny use block", previousLastTick, data, cc);
            }
        } else {
            final Result previousUseItem = event.useItemInHand();
            event.setCancelled(true);
            event.setUseInteractedBlock(Result.DENY);
            if (
                    previousUseItem == Result.DENY || preventUseItem
                    // Allow consumption still.
                    || !InventoryUtil.isConsumable(Bridge1_9.getUsedItem(player, event))
                    ) {
                event.setUseItemInHand(Result.DENY);
                if (data.debug) {
                    genericDebug(player, block, face, event, "deny item use", previousLastTick, data, cc);
                }
            }
            else {
                // Consumable and not prevented otherwise.
                // TODO: Ender pearl?
                event.setUseItemInHand(Result.ALLOW);
                if (data.debug) {
                    genericDebug(player, block, face, event, "allow edible item use", previousLastTick, data, cc);
                }
            }
        }
        data.subsequentCancel ++;
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onPlayerInteractMonitor(final PlayerInteractEvent event) {
        // Elytra boost.
        if (event.getAction() == Action.RIGHT_CLICK_AIR 
                && event.isCancelled() && event.useItemInHand() != Result.DENY) {
            final Player player = event.getPlayer();
            final ItemStack stack = Bridge1_9.getUsedItem(player, event);
            if (stack != null && BridgeMisc.maybeElytraBoost(player, stack.getType())) {
                final BlockInteractData data = BlockInteractData.getData(player);
                final int power = BridgeMisc.getFireworksPower(stack);
                final MovingData mData = MovingData.getData(player);
                final int ticks = Math.max((1 + power) * 20, 30);
                mData.fireworksBoostDuration = ticks;
                // Expiration tick: not general latency, rather a minimum margin for sudden congestion.
                mData.fireworksBoostTickExpire = TickTask.getTick() + ticks;
                // TODO: Invalidation mechanics: by tick/time well ?
                // TODO: Implement using it in CreativeFly.
                if (data.debug) {
                    debug(player, "Elytra boost (power " + power + "): " + stack);
                }
            }
        }
    }

    private void checkEnderPearlRightClickBlock(final Player player, final Block block, 
            final BlockFace face, final PlayerInteractEvent event, 
            final int previousLastTick, final BlockInteractData data) {
        if (block == null || !BlockProperties.isPassable(block.getType())) {
            final CombinedConfig ccc = CombinedConfig.getConfig(player);
            if (ccc.enderPearlCheck && ccc.enderPearlPreventClickBlock) {
                event.setUseItemInHand(Result.DENY);
                if (data.debug) {
                    final BlockInteractConfig cc = BlockInteractConfig.getConfig(player);
                    genericDebug(player, block, face, event, "click block: deny use ender pearl", previousLastTick, data, cc);
                }
            }
        }
    }

    private void genericDebug(final Player player, final Block block, final BlockFace face, 
            final PlayerInteractEvent event, final String tag, final int previousLastTick, 
            final BlockInteractData data, final BlockInteractConfig cc) {
        final StringBuilder builder = new StringBuilder(512);
        // Rate limit.
        if (data.lastTick == previousLastTick && data.subsequentCancel > 0) {
            data.rateLimitSkip ++;
            return;
        }
        // Debug log.
        builder.append("Interact cancel: " + event.isCancelled());
        builder.append(" (");
        builder.append(tag);
        if (block == null) {
            builder.append(") block: null");
        }
        else {
            builder.append(") block: ");
            builder.append(block.getWorld().getName() + "/" + LocUtil.simpleFormat(block));
            builder.append(" type: " + BlockProperties.getId(block.getType()));
            builder.append(" data: " + BlockProperties.getData(block));
            builder.append(" face: " + face);
        }

        if (data.rateLimitSkip > 0) {
            builder.append(" skipped(rate-limit: " + data.rateLimitSkip);
            data.rateLimitSkip = 0;
        }
        debug(player, builder.toString());
    }

}
