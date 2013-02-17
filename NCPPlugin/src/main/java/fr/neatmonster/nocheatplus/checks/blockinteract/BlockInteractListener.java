package fr.neatmonster.nocheatplus.checks.blockinteract;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;

/*
 * M#"""""""'M  dP                   dP       M""M            dP                                         dP   
 * ##  mmmm. `M 88                   88       M  M            88                                         88   
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  M  M 88d888b. d8888P .d8888b. 88d888b. .d8888b. .d8888b. d8888P 
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   M  M 88'  `88   88   88ooood8 88'  `88 88'  `88 88'  `""   88   
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. M  M 88    88   88   88.  ... 88       88.  .88 88.  ...   88   
 * M#       .;M dP `88888P' `88888P' dP   `YP M  M dP    dP   dP   `88888P' dP       `88888P8 `88888P'   dP   
 * M#########M                                MMMM                                                            
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
 * Central location to listen to events that are relevant for the block interact checks.
 * 
 * @see BlockInteractEvent
 */
public class BlockInteractListener extends CheckListener {

    /** The direction check. */
    private final Direction direction = addCheck(new Direction());

    /** The reach check. */
    private final Reach     reach     = addCheck(new Reach());
    
    /** The Visible check. */
    private final Visible visible = addCheck(new Visible());
    
    public BlockInteractListener(){
    	super(CheckType.BLOCKINTERACT);
    }

    /**
     * We listen to PlayerInteractEvent events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void onPlayerInteract(final PlayerInteractEvent event) {
        /*
         *  ____  _                         ___       _                      _   
         * |  _ \| | __ _ _   _  ___ _ __  |_ _|_ __ | |_ ___ _ __ __ _  ___| |_ 
         * | |_) | |/ _` | | | |/ _ \ '__|  | || '_ \| __/ _ \ '__/ _` |/ __| __|
         * |  __/| | (_| | |_| |  __/ |     | || | | | ||  __/ | | (_| | (__| |_ 
         * |_|   |_|\__,_|\__, |\___|_|    |___|_| |_|\__\___|_|  \__,_|\___|\__|
         *                |___/                                                  
         */
    	
    	// TODO: Cancelled events: might still have to check for use of blocks etc? 
    	
        final Player player = event.getPlayer();

        final Action action = event.getAction();
        
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK){
        	return;
        }

        final Block block = event.getClickedBlock();
        
        if (block == null){
        	return;
        }
        
        final BlockInteractData data = BlockInteractData.getData(player);
        final BlockInteractConfig cc = BlockInteractConfig.getConfig(player);

        boolean cancelled = false;
        
        final BlockFace face = event.getBlockFace();
        final Location loc = player.getLocation();
        
        // TODO: fast-interact !

        // First the reach check.
        if (!cancelled && reach.isEnabled(player) && reach.check(player, loc, block, data, cc)){
        	cancelled = true;
        }

        // Second the direction check
        if (!cancelled && direction.isEnabled(player) && direction.check(player, loc, block, data, cc)){
        	cancelled = true;
        }

        // Ray tracing for freecam use etc.
        if (!cancelled && visible.isEnabled(player) && visible.check(player, loc, block, face, action, data, cc)){
        	cancelled = true;
        }
        
        // If one of the checks requested to cancel the event, do so.
        if (cancelled) {
        	event.setUseInteractedBlock(Result.DENY);
        	event.setUseItemInHand(Result.DENY);
        	event.setCancelled(true);
        }
    }
}
