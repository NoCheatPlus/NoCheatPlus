package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.GameMode;
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

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.inventory.Items;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/*
 * M#"""""""'M  dP                   dP       M#"""""""'M                             dP       
 * ##  mmmm. `M 88                   88       ##  mmmm. `M                            88       
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  #'        .M 88d888b. .d8888b. .d8888b. 88  .dP  
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   M#  MMMb.'YM 88'  `88 88ooood8 88'  `88 88888"   
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. M#  MMMM'  M 88       88.  ... 88.  .88 88  `8b. 
 * M#       .;M dP `88888P' `88888P' dP   `YP M#       .;M dP       `88888P' `88888P8 dP   `YP 
 * M#########M                                M#########M                                      
 * 
 * M""MMMMMMMM oo            dP                                       
 * M  MMMMMMMM               88                                       
 * M  MMMMMMMM dP .d8888b. d8888P .d8888b. 88d888b. .d8888b. 88d888b. 
 * M  MMMMMMMM 88 Y8ooooo.   88   88ooood8 88'  `88 88ooood8 88'  `88 
 * M  MMMMMMMM 88       88   88   88.  ... 88    88 88.  ... 88       
 * M         M dP `88888P'   dP   `88888P' dP    dP `88888P' dP       
 * MMMMMMMMMMM                                                        
 */
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
    
    private boolean isInstaBreak = false;
    
    public BlockBreakListener(){
    	super(CheckType.BLOCKBREAK);
    }

    /**
     * We listen to BlockBreak events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onBlockBreak(final BlockBreakEvent event) {
        /*
         *  ____  _            _      ____                 _    
         * | __ )| | ___   ___| | __ | __ ) _ __ ___  __ _| | __
         * |  _ \| |/ _ \ / __| |/ / |  _ \| '__/ _ \/ _` | |/ /
         * | |_) | | (_) | (__|   <  | |_) | | |  __/ (_| |   < 
         * |____/|_|\___/ \___|_|\_\ |____/|_|  \___|\__,_|_|\_\
         */
        
        final Player player = event.getPlayer();
        
        // Illegal enchantments hotfix check.
        if (Items.checkIllegalEnchantments(player, player.getItemInHand())) event.setCancelled(true);
        
    	// Cancelled events only leads to resetting insta break.
    	if (event.isCancelled()){
    		isInstaBreak = false;
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
        if (wrongBlock.isEnabled(player) && wrongBlock.check(player, block, cc, data, isInstaBreak))
        	cancelled = true;

        // Has the player broken more blocks per second than allowed?
        if (!cancelled && frequency.isEnabled(player) && frequency.check(player, cc, data))
        	cancelled = true;
        	
        // Has the player broken blocks faster than possible?
        if (!cancelled && gameMode != GameMode.CREATIVE && fastBreak.isEnabled(player) && fastBreak.check(player, block, isInstaBreak, cc, data))
            cancelled = true;

        // Did the arm of the player move before breaking this block?
        if (!cancelled && noSwing.isEnabled(player) && noSwing.check(player, data))
            cancelled = true;

        // Is the block really in reach distance?
        if (!cancelled && reach.isEnabled(player) && reach.check(player, block, data))
            cancelled = true;

        // Did the player look at the block at all?
        if (!cancelled && direction.isEnabled(player) && direction.check(player, block, data))
            cancelled = true;
        
        // Destroying liquid blocks.
        if (!cancelled && BlockProperties.isLiquid(block.getTypeId()) && !player.hasPermission(Permissions.BLOCKBREAK_BREAK_LIQUID) && !NCPExemptionManager.isExempted(player, CheckType.BLOCKBREAK_BREAK)){
            cancelled = true;
        }

        // At least one check failed and demanded to cancel the event.
        if (cancelled){
        	event.setCancelled(cancelled);
        	// Reset damage position:
    		data.clickedX = block.getX();
    		data.clickedY = block.getY();
    		data.clickedZ = block.getZ();
        }
        else{
        	// Invalidate last damage position:
//        	data.clickedX = Integer.MAX_VALUE;
        }
        
        if (isInstaBreak){
        	data.wasInstaBreak = now;
        }
        else
        	data.wasInstaBreak = 0;
        
        // Adjust data.
        data.fastBreakBreakTime = now;
//        data.fastBreakfirstDamage = now;
        isInstaBreak = false;
    }

    /**
     * We listen to PlayerAnimation events because it is (currently) equivalent to "player swings arm" and we want to
     * check if he did that between block breaks.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerAnimation(final PlayerAnimationEvent event) {
        /*
         *  ____  _                            _          _                 _   _             
         * |  _ \| | __ _ _   _  ___ _ __     / \   _ __ (_)_ __ ___   __ _| |_(_) ___  _ __  
         * | |_) | |/ _` | | | |/ _ \ '__|   / _ \ | '_ \| | '_ ` _ \ / _` | __| |/ _ \| '_ \ 
         * |  __/| | (_| | |_| |  __/ |     / ___ \| | | | | | | | | | (_| | |_| | (_) | | | |
         * |_|   |_|\__,_|\__, |\___|_|    /_/   \_\_| |_|_|_| |_| |_|\__,_|\__|_|\___/|_| |_|
         *                |___/                                                               
         */
        // Just set a flag to true when the arm was swung.
//    	System.out.println("Animation");
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
        /*
         *  ____  _                         ___       _                      _   
         * |  _ \| | __ _ _   _  ___ _ __  |_ _|_ __ | |_ ___ _ __ __ _  ___| |_ 
         * | |_) | |/ _` | | | |/ _ \ '__|  | || '_ \| __/ _ \ '__/ _` |/ __| __|
         * |  __/| | (_| | |_| |  __/ |     | || | | | ||  __/ | | (_| | (__| |_ 
         * |_|   |_|\__,_|\__, |\___|_|    |___|_| |_|\__\___|_|  \__,_|\___|\__|
         *                |___/                                                  
         */
    	
//    	System.out.println("Interact("+event.isCancelled()+"): " + event.getClickedBlock());
    	// The following is to set the "first damage time" for a block.
    	
    	// Return if it is not left clicking a block. 
    	// (Allows right click to be ignored.)
    	isInstaBreak = false;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        checkBlockDamage(event.getPlayer(), event.getClickedBlock(), event);
        
    }
    
    @EventHandler(
    		ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onBlockDamage(final BlockDamageEvent event) {
//    	System.out.println("Damage("+event.isCancelled()+"): " + event.getBlock());
    	if (!event.isCancelled() && event.getInstaBreak()) isInstaBreak = true;
    	else isInstaBreak = false;
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
        if (block == null)
            return;
        
        final int tick = TickTask.getTick();
        // Skip if already set to the same block without breaking within one tick difference.
        if (tick < data.clickedTick);
        else if (data.fastBreakBreakTime < data.fastBreakfirstDamage && data.clickedX == block.getX() &&  data.clickedZ == block.getZ() &&  data.clickedY == block.getY()){
        	if (tick - data.clickedTick <= 1 ) return;
        }
        
        // (Always set, the interact event only fires once: the first time.)
        // Only record first damage:
        data.fastBreakfirstDamage = now;
        // Also set last clicked blocks position.
        data.clickedX = block.getX();
        data.clickedY = block.getY();
        data.clickedZ = block.getZ();
        data.clickedTick = tick;
    }
    
}
