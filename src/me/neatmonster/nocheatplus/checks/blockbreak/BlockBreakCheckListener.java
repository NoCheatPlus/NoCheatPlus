package me.neatmonster.nocheatplus.checks.blockbreak;

import java.util.LinkedList;
import java.util.List;

import me.neatmonster.nocheatplus.EventManager;
import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.config.Permissions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Central location to listen to events that are
 * relevant for the blockbreak checks
 * 
 */
public class BlockBreakCheckListener implements Listener, EventManager {

    private final FastBreakCheck fastBreakCheck;
    private final NoswingCheck   noswingCheck;
    private final ReachCheck     reachCheck;
    private final DirectionCheck directionCheck;
    private final NoCheatPlus    plugin;

    public BlockBreakCheckListener(final NoCheatPlus plugin) {

        fastBreakCheck = new FastBreakCheck(plugin);
        noswingCheck = new NoswingCheck(plugin);
        reachCheck = new ReachCheck(plugin);
        directionCheck = new DirectionCheck(plugin);

        this.plugin = plugin;
    }

    /**
     * We listen to PlayerAnimationEvent because it is (currently) equivalent
     * to "player swings arm" and we want to check if he did that between
     * blockbreaks.
     * 
     * @param event
     *            The PlayerAnimation Event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void armSwing(final PlayerAnimationEvent event) {
        // Just set a flag to true when the arm was swung
        BlockBreakCheck.getData(plugin.getPlayer(event.getPlayer())).armswung = true;
    }

    /**
     * We listen to blockBreak events for obvious reasons
     * 
     * @param event
     *            The blockbreak event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void blockBreak(final BlockBreakEvent event) {

        boolean cancelled = false;

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final BlockBreakConfig cc = BlockBreakCheck.getConfig(player);
        final BlockBreakData data = BlockBreakCheck.getData(player);

        // Remember the location of the block that will be broken
        data.brokenBlockLocation.set(event.getBlock());

        // Only if the block got damaged directly before, do the check(s)
        if (!data.brokenBlockLocation.equals(data.lastDamagedBlock)) {
            // Something caused a blockbreak event that's not from the player
            // Don't check it at all
            data.lastDamagedBlock.reset();
            return;
        }

        // Now do the actual checks, if still needed. It's a good idea to make
        // computationally cheap checks first, because it may save us from
        // doing the computationally expensive checks.

        // First FastPlace: Has the player broken blocks too quickly?
        if (cc.fastBreakCheck && !player.hasPermission(Permissions.BLOCKBREAK_FASTBREAK))
            cancelled = fastBreakCheck.check(player, data, cc);

        // Second NoSwing: Did the arm of the player move before breaking this
        // block?
        if (!cancelled && cc.noswingCheck && !player.hasPermission(Permissions.BLOCKBREAK_NOSWING))
            cancelled = noswingCheck.check(player, data, cc);

        // Third Reach: Is the block really in reach distance
        if (!cancelled && cc.reachCheck && !player.hasPermission(Permissions.BLOCKBREAK_REACH))
            cancelled = reachCheck.check(player, data, cc);

        // Forth Direction: Did the player look at the block at all
        if (!cancelled && cc.directionCheck && !player.hasPermission(Permissions.BLOCKBREAK_DIRECTION))
            cancelled = directionCheck.check(player, data, cc);

        // At least one check failed and demanded to cancel the event
        if (cancelled)
            event.setCancelled(cancelled);
    }

    /**
     * We listen to BlockDamage events to grab the information if it has been
     * an "insta-break". That info may come in handy later.
     * 
     * @param event
     *            The BlockDamage event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void blockHit(final BlockDamageEvent event) {

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final BlockBreakData data = BlockBreakCheck.getData(player);

        // Only interested in insta-break events here
        if (event.getInstaBreak())
            // Remember this location. We handle insta-breaks slightly
            // different in some of the blockbreak checks.
            data.instaBrokenBlockLocation.set(event.getBlock());

    }

    /**
     * We listen to BlockInteract events to be (at least in many cases) able
     * to distinguish between blockbreak events that were triggered by players
     * actually digging and events that were artificially created by plugins.
     * 
     * @param event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void blockInteract(final PlayerInteractEvent event) {

        // Do not care about null blocks
        if (event.getClickedBlock() == null)
            return;

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final BlockBreakData data = BlockBreakCheck.getData(player);
        // Remember this location. Only blockbreakevents for this specific
        // block will be handled at all
        data.lastDamagedBlock.set(event.getClickedBlock());
    }

    @Override
    public List<String> getActiveChecks(final ConfigurationCacheStore cc) {
        final LinkedList<String> s = new LinkedList<String>();

        final BlockBreakConfig bb = BlockBreakCheck.getConfig(cc);

        if (bb.fastBreakCheck)
            s.add("blockbreak.fastbreak");
        if (bb.directionCheck)
            s.add("blockbreak.direction");
        if (bb.reachCheck)
            s.add("blockbreak.reach");
        if (bb.noswingCheck)
            s.add("blockbreak.noswing");

        return s;
    }
}
