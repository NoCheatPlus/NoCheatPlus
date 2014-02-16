package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;

/*
 * M#"""""""'M  dP                   dP       MM"""""""`YM dP                            
 * ##  mmmm. `M 88                   88       MM  mmmmm  M 88                            
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  M'        .M 88 .d8888b. .d8888b. .d8888b. 
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   MM  MMMMMMMM 88 88'  `88 88'  `"" 88ooood8 
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. MM  MMMMMMMM 88 88.  .88 88.  ... 88.  ... 
 * M#       .;M dP `88888P' `88888P' dP   `YP MM  MMMMMMMM dP `88888P8 `88888P' `88888P' 
 * M#########M                                MMMMMMMMMMMM                               
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
 * Central location to listen to events that are relevant for the block place checks.
 * 
 * @see BlockPlaceEvent
 */
public class BlockPlaceListener extends CheckListener {
	
	private static final int p1 = 73856093;
	private static final int p2 = 19349663;
	private static final int p3 = 83492791;

	private static final int getHash(final int x, final int y, final int z) {
		return p1 * x ^ p2 * y ^ p3 * z;
	}
	
	public static int getCoordHash(final Block block){
		return getHash(block.getX(), block.getY(), block.getZ());
	}
	
	public static int getBlockPlaceHash(final Block block, final Material mat){
		int hash = getCoordHash(block);
		if (mat != null){
			hash |= mat.name().hashCode();
		}
		hash |= block.getWorld().getName().hashCode();
		return hash;
	}
	
	/** Against. */
	private final Against against = addCheck(new Against());
	
	/** AutoSign. */
	private final AutoSign autoSign = addCheck(new AutoSign());

    /** The direction check. */
    private final Direction direction = addCheck(new Direction());

    /** The fast place check. */
    private final FastPlace fastPlace = addCheck(new FastPlace());

    /** The no swing check. */
    private final NoSwing   noSwing   = addCheck(new NoSwing());

    /** The reach check. */
    private final Reach     reach     = addCheck(new Reach());

    /** The speed check. */
    private final Speed     speed     = addCheck(new Speed());
    
    public BlockPlaceListener(){
    	super(CheckType.BLOCKPLACE);
    }

    /**
     * We listen to BlockPlace events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(final BlockPlaceEvent event) {
    	
        final Block block = event.getBlockPlaced();
        final Block blockAgainst = event.getBlockAgainst();
        // Skip any null blocks.
        if (block == null || blockAgainst == null)
            return;
        
        final Material mat = block.getType();
        final Player player = event.getPlayer();
        boolean cancelled = false;
        
        final BlockPlaceData data = BlockPlaceData.getData(player);
        final BlockPlaceConfig cc = BlockPlaceConfig.getConfig(player);
        
        if (mat == Material.SIGN_POST || mat == Material.WALL_SIGN){
        	data.autoSignPlacedTime = System.currentTimeMillis();
        	// Always hash as sign post for improved compatibility with Lockette etc.
        	data.autoSignPlacedHash = getBlockPlaceHash(block, Material.SIGN_POST);
        }

        // Fast place check.
        if (fastPlace.isEnabled(player)){
        	if (fastPlace.check(player, block, data, cc)) {
        		cancelled = true;
        	} else {
        		// Feed the improbable.
                Improbable.feed(player, 0.5f, System.currentTimeMillis());
        	}
        }

        // No swing check (player doesn't swing their arm when placing a lily pad).
        if (!cancelled && mat != Material.WATER_LILY && noSwing.isEnabled(player) && noSwing.check(player, data, cc)) {
        	// Consider skipping all insta placables or using simplified version (true or true within time frame).
        	cancelled = true;
        }

        // Reach check (distance).
        if (!cancelled && reach.isEnabled(player) && reach.check(player, block, data, cc)) {
        	cancelled = true;
        }

        // Direction check.
        if (!cancelled && direction.isEnabled(player) && direction.check(player, block, blockAgainst, data, cc)) {
        	cancelled = true;
        }
        
        // Surrounding material.
        if (!cancelled && against.isEnabled(player) && against.check(player, block, mat, blockAgainst, data, cc)) {
        	cancelled = true;
        }

        // If one of the checks requested to cancel the event, do so.
        if (cancelled) {
        	event.setCancelled(cancelled);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent event){
    	if (event.getClass() != SignChangeEvent.class){
    		// Built in plugin compatibility.
    		// TODO: Don't understand why two consecutive events editing the same block are a problem.
    		return;
    	}
    	final Player player = event.getPlayer();
    	final Block block = event.getBlock();
    	final String[] lines = event.getLines();
    	if (block == null || lines == null || player == null){
    		// Somewhat defensive.
    		return;
    	}
    	if (autoSign.isEnabled(player) && autoSign.check(player, block, lines)){
    		event.setCancelled(true);
    	}
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
        /*
         *  ____  _                            _          _                 _   _             
         * |  _ \| | __ _ _   _  ___ _ __     / \   _ __ (_)_ __ ___   __ _| |_(_) ___  _ __  
         * | |_) | |/ _` | | | |/ _ \ '__|   / _ \ | '_ \| | '_ ` _ \ / _` | __| |/ _ \| '_ \ 
         * |  __/| | (_| | |_| |  __/ |     / ___ \| | | | | | | | | | (_| | |_| | (_) | | | |
         * |_|   |_|\__,_|\__, |\___|_|    /_/   \_\_| |_|_|_| |_| |_|\__,_|\__|_|\___/|_| |_|
         *                |___/                                                               
         */
        // Just set a flag to true when the arm was swung.
        BlockPlaceData.getData(event.getPlayer()).noSwingArmSwung = true;
    }

    /**
     * We listener to PlayerInteract events to prevent players from spamming the server with monster eggs.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        /*
         *  ____  _                         ___       _                      _   
         * |  _ \| | __ _ _   _  ___ _ __  |_ _|_ __ | |_ ___ _ __ __ _  ___| |_ 
         * | |_) | |/ _` | | | |/ _ \ '__|  | || '_ \| __/ _ \ '__/ _` |/ __| __|
         * |  __/| | (_| | |_| |  __/ |     | || | | | ||  __/ | | (_| | (__| |_ 
         * |_|   |_|\__,_|\__, |\___|_|    |___|_| |_|\__\___|_|  \__,_|\___|\__|
         *                |___/                                                  
         */
    	if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    	final Player player = event.getPlayer();
    	
    	final ItemStack stack = player.getItemInHand();
    	if (stack == null) return;
    	
    	final Material type = stack.getType();
    	
    	if (type == Material.BOAT){
    		// Check boats-anywhere.
        	final org.bukkit.block.Block block = event.getClickedBlock();
        	final Material mat = block.getType();
        	
        	// TODO: allow lava ?
        	if (mat == Material.WATER || mat == Material.STATIONARY_WATER) return;
        	
        	final org.bukkit.block.Block relBlock = block.getRelative(event.getBlockFace());
        	final Material relMat = relBlock.getType();
        	
        	if (relMat == Material.WATER || relMat == Material.STATIONARY_WATER) return;
        	
        	if (!player.hasPermission(Permissions.BLOCKPLACE_BOATSANYWHERE)){
        		event.setCancelled(true);
        	}
            
    	}
    	else if (type == Material.MONSTER_EGG){
    		// Check blockplace.speed.
    		if (speed.isEnabled(player) && speed.check(player))
                // If the check was positive, cancel the event.
                event.setCancelled(true);
    	} 
    }

    /**
     * We listen to ProjectileLaunch events to prevent players from launching projectiles too quickly.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        // The shooter needs to be a player.
    	final Projectile projectile = event.getEntity();
    	final Player player = BridgeMisc.getShooterPlayer(projectile);
        if (player == null) {
        	return;
        }

        // And the projectile must be one the following:
        EntityType type = event.getEntityType();
        switch (type) {
        case ENDER_PEARL:
            break;
        case ENDER_SIGNAL:
            break;
        case EGG:
            break;
        case SNOWBALL:
            break;
        case THROWN_EXP_BOTTLE:
            break;
        case SPLASH_POTION:
            break;
        default:
            return;
        }

        // Do the actual check...
        boolean cancel = false;
        if (speed.isEnabled(player)){
            final long now = System.currentTimeMillis();
            final Location loc = player.getLocation();
            if (Combined.checkYawRate(player, loc.getYaw(), now, loc.getWorld().getName())){
            	// Yawrate (checked extra).
            	cancel = true;
            }
            if (speed.check(player)){
                // If the check was positive, cancel the event.
            	cancel = true;
            }
            else if (Improbable.check(player, 0.6f, now, "blockplace.speed")){
                // Combined fighting speed.
            	cancel = true;
            }
        }
        
        // Ender pearl glitch (ab-) use.
        if (!cancel && type == EntityType.ENDER_PEARL){
        	if (!CombinedConfig.getConfig(player).enderPearlCheck){
        		// Do nothing !
        		// TODO: Might have further flags?
        	}
        	else if (!BlockProperties.isPassable(projectile.getLocation())){
        		// Launch into a block.
        		// TODO: This might be a general check later.       		
        		cancel = true;
        	}
        	else{
            	if (!BlockProperties.isPassable(player.getEyeLocation(), projectile.getLocation())){
            		// Something between player 
            		// TODO: This might be a general check later.
            		cancel = true;
            	}
            	else{
            		final Material mat = player.getLocation().getBlock().getType();
            		final long flags = BlockProperties.F_CLIMBABLE | BlockProperties.F_LIQUID | BlockProperties.F_IGN_PASSABLE;
            		if (mat != Material.AIR && (BlockProperties.getBlockFlags(mat.getId()) & flags) == 0 && !mcAccess.hasGravity(mat)){
            			// Still fails on piston traps etc.
            			if (!BlockProperties.isPassable(player.getLocation(), projectile.getLocation()) && !BlockProperties.isOnGroundOrResetCond(player, player.getLocation(), MovingConfig.getConfig(player).yOnGround)){
            				cancel = true;
            			}
            		}
            	}
        	}
        }
         
        // Cancelled ?
        if (cancel){
        	event.setCancelled(true);
        }
    }
}
