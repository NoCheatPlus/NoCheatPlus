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
import fr.neatmonster.nocheatplus.checks.moving.location.LocUtil;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

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
    @EventHandler(
            ignoreCancelled = false, priority = EventPriority.LOWEST)
    protected void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        // Cancel interact events for dead players.
        if (player.isDead() && BridgeHealth.getHealth(player) <= 0.0) {
            // Auto-soup after death.
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
            event.setCancelled(true);
            counters.addPrimaryThread(idCancelDead, 1);
            return;
        }

        // TODO: Re-arrange for interact spam, possibly move ender pearl stuff to a method.
        final Action action = event.getAction();
        final Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        final BlockInteractData data = BlockInteractData.getData(player);
        final int previousLastTick = data.lastTick;
        data.setLastBlock(block, action);
        final BlockFace face = event.getBlockFace();
        switch(action) {
            case LEFT_CLICK_BLOCK:
                break;
            case RIGHT_CLICK_BLOCK:
                final ItemStack stack = Bridge1_9.getUsedItem(player, event);
                if (stack != null && stack.getType() == Material.ENDER_PEARL) {
                    if (!BlockProperties.isPassable(block.getType())) {
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
                break;
            default:
                return;
        }

        if (event.isCancelled() && event.useInteractedBlock() != Result.ALLOW) {
            data.subsequentCancel ++;
            return;
        }

        final BlockInteractConfig cc = BlockInteractConfig.getConfig(player);
        boolean cancelled = false;
        boolean preventUseItem = false;

        final Location loc = player.getLocation(useLoc);

        // Interaction speed.
        if (!cancelled && speed.isEnabled(player) && speed.check(player, data, cc)) {
            cancelled = true;
            preventUseItem = true;
        }

        // First the reach check.
        if (!cancelled && reach.isEnabled(player) && reach.check(player, loc, block, data, cc)) {
            cancelled = true;
        }

        // Second the direction check
        if (!cancelled && direction.isEnabled(player) && direction.check(player, loc, block, data, cc)) {
            cancelled = true;
        }

        // Ray tracing for freecam use etc.
        if (!cancelled && visible.isEnabled(player) && visible.check(player, loc, block, face, action, data, cc)) {
            cancelled = true;
        }

        // If one of the checks requested to cancel the event, do so.
        if (cancelled) {
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
                        || !CheckUtils.isConsumable(Bridge1_9.getUsedItem(player, event))
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
        else {
            data.subsequentCancel = 0;
        }
        useLoc.setWorld(null);
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
        builder.append(") block: ");
        builder.append(block.getWorld().getName() + "/" + LocUtil.simpleFormat(block));
        builder.append(" type: " + BlockProperties.getId(block.getType()));
        builder.append(" data: " + BlockProperties.getData(block));
        builder.append(" face: " + face);
        if (data.rateLimitSkip > 0) {
            builder.append(" skipped(rate-limit: " + data.rateLimitSkip);
            data.rateLimitSkip = 0;
        }
        debug(player, builder.toString());
    }

}
