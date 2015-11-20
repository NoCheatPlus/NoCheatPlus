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
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;

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
        data.setLastBlock(block, action);
        switch(action) {
            case LEFT_CLICK_BLOCK:
                break;
            case RIGHT_CLICK_BLOCK:
                final ItemStack stack = player.getItemInHand();
                if (stack != null && stack.getType() == Material.ENDER_PEARL) {
                    if (!BlockProperties.isPassable(block.getType())) {
                        final CombinedConfig ccc = CombinedConfig.getConfig(player);
                        if (ccc.enderPearlCheck && ccc.enderPearlPreventClickBlock) {
                            event.setUseItemInHand(Result.DENY);
                        }
                    }
                }
                break;
            default:
                return;
        }

        if (event.isCancelled() && event.useInteractedBlock() != Result.ALLOW) {
            return;
        }

        final BlockInteractConfig cc = BlockInteractConfig.getConfig(player);
        boolean cancelled = false;

        final BlockFace face = event.getBlockFace();
        final Location loc = player.getLocation(useLoc);

        // Interaction speed.
        if (!cancelled && speed.isEnabled(player) && speed.check(player, data, cc)) {
            cancelled = true;
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
            } else {
                event.setCancelled(true);
                event.setUseInteractedBlock(Result.DENY);
                final ItemStack stack = player.getItemInHand();
                final Material mat = stack == null ? Material.AIR : stack.getType();
                if (mat.isEdible() || mat == Material.POTION) {
                    // TODO: Ender pearl?
                    event.setUseItemInHand(Result.ALLOW);
                } else {
                    event.setUseItemInHand(Result.DENY);
                }
            }
        }
        useLoc.setWorld(null);
    }
}
