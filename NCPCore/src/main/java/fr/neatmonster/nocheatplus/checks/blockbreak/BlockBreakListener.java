package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.inventory.Items;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * Central location to listen to events that are relevant for the block break checks.
 * 
 * @see BlockBreakEvent
 */
public class BlockBreakListener extends CheckListener {

    /** The direction check. */
    private final Direction direction = addCheck(new Direction());

    /** The fast break check (per block breaking speed). */
    private final FastBreak fastBreak = addCheck(new FastBreak());

    /** The frequency check (number of blocks broken) */
    private final Frequency frequency = addCheck(new Frequency());

    /** The no swing check. */
    private final NoSwing   noSwing   = addCheck(new NoSwing());

    /** The reach check. */
    private final Reach     reach     = addCheck(new Reach());

    /** The wrong block check. */
    private final WrongBlock wrongBlock = addCheck(new WrongBlock());

    private AlmostBoolean isInstaBreak = AlmostBoolean.NO;

    private final Counters counters = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class);
    private final int idCancelDIllegalItem = counters.registerKey("illegalitem");

    public BlockBreakListener(){
        super(CheckType.BLOCKBREAK);
    }

    /**
     * We listen to BlockBreak events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onBlockBreak(final BlockBreakEvent event) {

        final Player player = event.getPlayer();

        // Illegal enchantments hotfix check.
        if (Items.checkIllegalEnchantments(player, player.getItemInHand())) {
            event.setCancelled(true);
            counters.addPrimaryThread(idCancelDIllegalItem, 1);
        }

        // Cancelled events only leads to resetting insta break.
        if (event.isCancelled()) {
            isInstaBreak = AlmostBoolean.NO;
            return;
        }

        // TODO: maybe invalidate instaBreak on some occasions.


        final Block block = event.getBlock();

        boolean cancelled = false;

        // Do the actual checks, if still needed. It's a good idea to make computationally cheap checks first, because
        // it may save us from doing the computationally expensive checks.

        final BlockBreakConfig cc = BlockBreakConfig.getConfig(player);
        final BlockBreakData data = BlockBreakData.getData(player);
        final long now = System.currentTimeMillis();

        final GameMode gameMode = player.getGameMode();

        // Has the player broken a block that was not damaged before?
        if (wrongBlock.isEnabled(player) && wrongBlock.check(player, block, cc, data, isInstaBreak)) {
            cancelled = true;
        }

        // Has the player broken more blocks per second than allowed?
        if (!cancelled && frequency.isEnabled(player) && frequency.check(player, cc, data)) {
            cancelled = true;
        }

        // Has the player broken blocks faster than possible?
        if (!cancelled && gameMode != GameMode.CREATIVE && fastBreak.isEnabled(player) && fastBreak.check(player, block, isInstaBreak, cc, data)) {
            cancelled = true;
        }

        // Did the arm of the player move before breaking this block?
        if (!cancelled && noSwing.isEnabled(player) && noSwing.check(player, data)) {
            cancelled = true;
        }

        // Is the block really in reach distance?
        if (!cancelled && reach.isEnabled(player) && reach.check(player, block, data)) {
            cancelled = true;
        }

        // Did the player look at the block at all?
        if (!cancelled && direction.isEnabled(player) && direction.check(player, block, data)) {
            cancelled = true;
        }

        // Destroying liquid blocks.
        if (!cancelled && BlockProperties.isLiquid(block.getType()) && !player.hasPermission(Permissions.BLOCKBREAK_BREAK_LIQUID) && !NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK_BREAK)){
            cancelled = true;
        }

        // On cancel...
        if (cancelled) {
            event.setCancelled(cancelled);
            // Reset damage position:
            // TODO: Review this (!), check if set at all !?
            data.clickedX = block.getX();
            data.clickedY = block.getY();
            data.clickedZ = block.getZ();
        }
        else{
            // Invalidate last damage position:
            //        	data.clickedX = Integer.MAX_VALUE;
            // Debug log (only if not cancelled, to avoid spam).
            if (data.debug) {
                NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " block break(" + block.getType() + "): " + block.getX() + ", " + block.getY() + ", " + block.getZ());
            }
        }

        if (isInstaBreak.decideOptimistically()) {
            data.wasInstaBreak = now;
        }
        else {
            data.wasInstaBreak = 0;
        }

        // Adjust data.
        data.fastBreakBreakTime = now;
        //        data.fastBreakfirstDamage = now;
        isInstaBreak = AlmostBoolean.NO;
    }

    /**
     * We listen to PlayerAnimation events because it is (currently) equivalent to "player swings arm" and we want to
     * check if they did that between block breaks.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerAnimation(final PlayerAnimationEvent event) {
        // Just set a flag to true when the arm was swung.
        //    	NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, "Animation");
        BlockBreakData.getData(event.getPlayer()).noSwingArmSwung = true;
    }

    /**
     * We listen to BlockInteract events to be (at least in many cases) able to distinguish between block break events
     * that were triggered by players actually digging and events that were artificially created by plugins.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        //    	NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, "Interact("+event.isCancelled()+"): " + event.getClickedBlock());
        // The following is to set the "first damage time" for a block.

        // Return if it is not left clicking a block. 
        // (Allows right click to be ignored.)
        isInstaBreak = AlmostBoolean.NO;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        checkBlockDamage(event.getPlayer(), event.getClickedBlock(), event);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onBlockDamageLowest(final BlockDamageEvent event) {
        if (event.getInstaBreak()) {
            // Indicate that this might have been set by CB/MC.
            isInstaBreak = AlmostBoolean.MAYBE;
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onBlockDamage(final BlockDamageEvent event) {
        if (!event.isCancelled() && event.getInstaBreak()) {
            // Keep MAYBE.
            if (isInstaBreak != AlmostBoolean.MAYBE) {
                isInstaBreak = AlmostBoolean.YES;
            }
        }
        else {
            isInstaBreak = AlmostBoolean.NO;
        }
        checkBlockDamage(event.getPlayer(), event.getBlock(), event);
    }

    private void checkBlockDamage(final Player player, final Block block, final Cancellable event){
        final long now = System.currentTimeMillis();
        final BlockBreakData data = BlockBreakData.getData(player); 

        //        if (event.isCancelled()){
        //        	// Reset the time, to avoid certain kinds of cheating. => WHICH ?
        //        	data.fastBreakfirstDamage = now;
        //        	data.clickedX = Integer.MAX_VALUE; // Should be enough to reset that one.
        //        	return;
        //        }

        // Do not care about null blocks.
        if (block == null) {
            return;
        }

        final int tick = TickTask.getTick();
        // Skip if already set to the same block without breaking within one tick difference.
        final ItemStack stack = player.getItemInHand();
        final Material tool = stack == null ? null: stack.getType();
        if (data.toolChanged(tool)) {
            // Update.
        } else if (tick < data.clickedTick || now < data.fastBreakfirstDamage || now < data.fastBreakBreakTime) {
            // Time/tick ran backwards: Update.
            // Tick running backwards should not happen in the main thread unless for reload. A plugin could reset it (not intended).
        } else if (data.fastBreakBreakTime < data.fastBreakfirstDamage && data.clickedX == block.getX() &&  data.clickedZ == block.getZ() &&  data.clickedY == block.getY()){
            // Preserve first damage time.
            if (tick - data.clickedTick <= 1 ) {
                return;
            }
        }
        // (Always set, the interact event only fires once: the first time.)
        // Only record first damage:
        data.setClickedBlock(block, tick, now, tool);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onItemHeld(final PlayerItemHeldEvent event) {
        // Reset clicked block.
        // TODO: Not for 1.5.2 and before?
        final Player player = event.getPlayer();
        final BlockBreakData data = BlockBreakData.getData(player);
        if (data.toolChanged(player.getInventory().getItem(event.getNewSlot()))) {
            data.resetClickedBlock();
        }
    }

}
