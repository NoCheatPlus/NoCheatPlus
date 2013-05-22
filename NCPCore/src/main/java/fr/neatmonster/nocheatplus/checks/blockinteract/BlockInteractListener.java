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

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;

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

    /** The looking-direction check. */
    private final Direction direction = addCheck(new Direction());

    /** The reach-distance check. */
    private final Reach     reach     = addCheck(new Reach());
    
    /** Interact with visible blocks. */
    private final Visible visible = addCheck(new Visible());
    
    /** Speed of interaction. */
    private final Speed speed = addCheck(new Speed());
    
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
            ignoreCancelled = false, priority = EventPriority.LOWEST)
    protected void onPlayerInteract(final PlayerInteractEvent event) {
        /*
         *  ____  _                         ___       _                      _   
         * |  _ \| | __ _ _   _  ___ _ __  |_ _|_ __ | |_ ___ _ __ __ _  ___| |_ 
         * | |_) | |/ _` | | | |/ _ \ '__|  | || '_ \| __/ _ \ '__/ _` |/ __| __|
         * |  __/| | (_| | |_| |  __/ |     | || | | | ||  __/ | | (_| | (__| |_ 
         * |_|   |_|\__,_|\__, |\___|_|    |___|_| |_|\__\___|_|  \__,_|\___|\__|
         *                |___/                                                  
         */
    	
    	// TODO: Re-arrange for interact spam, possibly move ender pearl stuff to a method.
    	final Action action = event.getAction();
    	final Block block = event.getClickedBlock();
        
        if (block == null){
        	return;
        }
    	final Player player = event.getPlayer();
        
        switch(action){
        case LEFT_CLICK_BLOCK:
        	break;
        case RIGHT_CLICK_BLOCK:
        	final ItemStack stack = player.getItemInHand();
    		if (stack != null && stack.getTypeId() == Material.ENDER_PEARL.getId()){
    			if (!BlockProperties.isPassable(block.getTypeId())){
    				final CombinedConfig ccc = CombinedConfig.getConfig(player);
    				if (ccc.enderPearlCheck && ccc.enderPearlPreventClickBlock){
    					event.setUseItemInHand(Result.DENY);
    				}
    			}
    		}
        	break;
    	default:
    		return;
        }
        
        if (event.isCancelled()){
        	return;
        }
        
        final BlockInteractData data = BlockInteractData.getData(player);
        final BlockInteractConfig cc = BlockInteractConfig.getConfig(player);

        boolean cancelled = false;
        
        final BlockFace face = event.getBlockFace();
        final Location loc = player.getLocation();
        
        // Interaction speed.
        if (!cancelled && speed.isEnabled(player) && speed.check(player, data, cc)){
        	cancelled = true;
        }

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
