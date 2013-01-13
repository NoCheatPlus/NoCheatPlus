package fr.neatmonster.nocheatplus.checks.moving;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.BedLeave;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.LogUtil;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/*
 * M"""""`'"""`YM                   oo                   
 * M  mm.  mm.  M                                        
 * M  MMM  MMM  M .d8888b. dP   .dP dP 88d888b. .d8888b. 
 * M  MMM  MMM  M 88'  `88 88   d8' 88 88'  `88 88'  `88 
 * M  MMM  MMM  M 88.  .88 88 .88'  88 88    88 88.  .88 
 * M  MMM  MMM  M `88888P' 8888P'   dP dP    dP `8888P88 
 * MMMMMMMMMMMMMM                                    .88 
 *                                               d8888P  
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
 * Central location to listen to events that are relevant for the moving checks.
 * 
 * @see MovingEvent
 */
public class MovingListener extends CheckListener{

	private static final class MoveInfo{
		public final BlockCache cache;
        public final PlayerLocation from;
        public final PlayerLocation to;
        
        public MoveInfo(final MCAccess mcAccess){
        	cache = mcAccess.getBlockCache(null);
        	from = new PlayerLocation(mcAccess, null);
        	to = new PlayerLocation(mcAccess, null);
        }
        
        /**
         * Demands at least setting from.
         * @param player
         * @param from
         * @param to
         * @param yOnGround
         */
        public final void set(final Player player, final Location from, final Location to, final double yOnGround){
            this.from.set(from, player, yOnGround);
            this.cache.setAccess(from.getWorld());
            this.from.setBlockCache(cache);
            if (to != null){
                this.to.set(to, player, yOnGround);
                this.to.setBlockCache(cache);
            }
        }
        public final void cleanup(){
            from.cleanup();
            to.cleanup();
            cache.cleanup();
        }
    }

    /** The instance of NoCheatPlus. */
    private final NoCheatPlus        plugin             = (NoCheatPlus) Bukkit.getPluginManager().getPlugin(
                                                                "NoCheatPlus");
    /** The no fall check. **/
    public final NoFall noFall = new NoFall();
    
    /** The creative fly check. */
    private final CreativeFly        creativeFly        = new CreativeFly();

    /** The more packets check. */
    private final MorePackets        morePackets        = new MorePackets();

    /** The more packets vehicle check. */
    private final MorePacketsVehicle morePacketsVehicle = new MorePacketsVehicle();

    /** The survival fly check. */
    private final SurvivalFly        survivalFly        = new SurvivalFly();
    
    /** The Passable (simple no-clip) check.*/
    private final Passable passable = new Passable();
    
	/** Combined check but handled here (subject to change!) */
	private final BedLeave bedLeave = new BedLeave();
    
    /**
     * Unused instances.<br>
     * TODO: Not sure this is needed by contract, might be better due to cascading events in case of actions.
     */
    private final List<MoveInfo> parkedInfo = new ArrayList<MoveInfo>(10);
    
    public MovingListener() {
		super(CheckType.MOVING);
	}

    /**
     * A workaround for players placing blocks below them getting pushed off the block by NoCheatPlus.
     * 
     * It essentially moves the "setbackpoint" to the top of the newly placed block, therefore tricking NoCheatPlus into
     * thinking the player was already on top of that block and should be allowed to stay there.
     * 
     * It also prevent players from placing a block on a liquid (which is impossible without a modified version of
     * Minecraft).
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event) {
        /*
         *  ____  _            _      ____  _                
         * | __ )| | ___   ___| | __ |  _ \| | __ _  ___ ___ 
         * |  _ \| |/ _ \ / __| |/ / | |_) | |/ _` |/ __/ _ \
         * | |_) | | (_) | (__|   <  |  __/| | (_| | (_|  __/
         * |____/|_|\___/ \___|_|\_\ |_|   |_|\__,_|\___\___|
         */
        final Player player = event.getPlayer();

		// Ignore players inside a vehicle.
		if (player.isInsideVehicle())
			return;

		final org.bukkit.block.Block block = event.getBlock();
		if (block == null) return;
		final int blockY = block.getY();
		
		final Material mat = block.getType();

		final MovingData data = MovingData.getData(player);
		if (!creativeFly.isEnabled(player) && !survivalFly.isEnabled(player)) return;
		
		if (!data.hasSetBack() || blockY + 1D < data.getSetBackY()) return;
		
		final Location loc = player.getLocation();
		if (Math.abs(loc.getX() - 0.5 - block.getX()) <= 1D
				&& Math.abs(loc.getZ() - 0.5 - block.getZ()) <= 1D
				&& loc.getY() - blockY > 0D && loc.getY() - blockY < 2D
				&& (mcAccess.Block_i(mat.getId()) || BlockProperties.isLiquid(mat.getId()))) {
			// The creative fly and/or survival fly check is enabled, the
			// block was placed below the player and is
			// solid, so do what we have to do.
			data.setSetBackY(blockY + 1D);
			data.sfJumpPhase = 0;
		}
    }

    /**
     * We listen to this event to prevent player from flying by sending bed leaving packets.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
        /*
         *  ____  _                         ____           _   _____       _            
         * |  _ \| | __ _ _   _  ___ _ __  | __ )  ___  __| | | ____|_ __ | |_ ___ _ __ 
         * | |_) | |/ _` | | | |/ _ \ '__| |  _ \ / _ \/ _` | |  _| | '_ \| __/ _ \ '__|
         * |  __/| | (_| | |_| |  __/ |    | |_) |  __/ (_| | | |___| | | | ||  __/ |   
         * |_|   |_|\__,_|\__, |\___|_|    |____/ \___|\__,_| |_____|_| |_|\__\___|_|   
         *                |___/                                                         
         */
        CombinedData.getData(event.getPlayer()).wasInBed = true;
    }

    /**
     * We listen to this event to prevent player from flying by sending bed leaving packets.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerBedLeave(final PlayerBedLeaveEvent event) {
        /*
         *  ____  _                         ____           _   _                         
         * |  _ \| | __ _ _   _  ___ _ __  | __ )  ___  __| | | |    ___  __ ___   _____ 
         * | |_) | |/ _` | | | |/ _ \ '__| |  _ \ / _ \/ _` | | |   / _ \/ _` \ \ / / _ \
         * |  __/| | (_| | |_| |  __/ |    | |_) |  __/ (_| | | |__|  __/ (_| |\ V /  __/
         * |_|   |_|\__,_|\__, |\___|_|    |____/ \___|\__,_| |_____\___|\__,_| \_/ \___|
         *                |___/                                                          
         */
        final Player player = event.getPlayer();
        
		if (bedLeave.isEnabled(player) && bedLeave.checkBed(player)) {
			// Check if the player has to be reset.
			// To "cancel" the event, we teleport the player.
			final Location loc = player.getLocation();
			final MovingData data = MovingData.getData(player);
			final MovingConfig cc = MovingConfig.getConfig(player); 
			Location target = null;
			final boolean sfCheck = shouldCheckSurvivalFly(player, data, cc);
			if (sfCheck) target = data.getSetBack(loc);
			if (target == null){
				// TODO: Add something to guess the best set back location (possibly data.guessSetBack(Location)).
				target = loc;
			}
			if (target != null){
				// Actually this should not possibly be null, this is a block for "future" purpose, feel free to criticize it.
				if (sfCheck && noFall.isEnabled(player)){
					// Check if to deal damage.
					double y = loc.getY();
					if (data.hasSetBack()) y = Math.min(y, data.getSetBackY());
					noFall.checkDamage(player, data, y);
				}
				// Teleport.
				data.setTeleported(target); // Should be enough. | new Location(target.getWorld(), target.getX(), target.getY(), target.getZ(), target.getYaw(), target.getPitch());
				player.teleport(target, TeleportCause.PLUGIN);// TODO: schedule / other measures ?
			}
		}
		else{
			// Reset bed ...
			CombinedData.getData(player).wasInBed = false;
		}
    }

    /**
     * Just for security, if a player switches between worlds, reset the fly and more packets checks data, because it is
     * definitely invalid now.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        /*
         *  ____  _                          ____ _                                _  __        __         _     _ 
         * |  _ \| | __ _ _   _  ___ _ __   / ___| |__   __ _ _ __   __ _  ___  __| | \ \      / /__  _ __| | __| |
         * | |_) | |/ _` | | | |/ _ \ '__| | |   | '_ \ / _` | '_ \ / _` |/ _ \/ _` |  \ \ /\ / / _ \| '__| |/ _` |
         * |  __/| | (_| | |_| |  __/ |    | |___| | | | (_| | | | | (_| |  __/ (_| |   \ V  V / (_) | |  | | (_| |
         * |_|   |_|\__,_|\__, |\___|_|     \____|_| |_|\__,_|_| |_|\__, |\___|\__,_|    \_/\_/ \___/|_|  |_|\__,_|
         *                |___/                                     |___/                                          
         */
        // Maybe this helps with people teleporting through Multiverse portals having problems?
    	final Player player = event.getPlayer();
        final MovingData data = MovingData.getData(player);
        data.clearFlyData();
        data.clearMorePacketsData();
        // TODO: Might omit this if neither check is activated.
        data.setSetBack(player.getLocation());
    }

    /**
     * When a player changes his gamemode, all information related to the moving checks becomes invalid.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerGameModeChange(final PlayerGameModeChangeEvent event) {
        /*
         *  ____  _                          ____                      __  __           _      
         * |  _ \| | __ _ _   _  ___ _ __   / ___| __ _ _ __ ___   ___|  \/  | ___   __| | ___ 
         * | |_) | |/ _` | | | |/ _ \ '__| | |  _ / _` | '_ ` _ \ / _ \ |\/| |/ _ \ / _` |/ _ \
         * |  __/| | (_| | |_| |  __/ |    | |_| | (_| | | | | | |  __/ |  | | (_) | (_| |  __/
         * |_|   |_|\__,_|\__, |\___|_|     \____|\__,_|_| |_| |_|\___|_|  |_|\___/ \__,_|\___|
         *                |___/                                                                
         *   ____ _                            
         *  / ___| |__   __ _ _ __   __ _  ___ 
         * | |   | '_ \ / _` | '_ \ / _` |/ _ \
         * | |___| | | | (_| | | | | (_| |  __/
         *  \____|_| |_|\__,_|_| |_|\__, |\___|
         *                          |___/      
         */
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getNewGameMode() == GameMode.CREATIVE){
            final MovingData data = MovingData.getData(event.getPlayer());
            data.clearFlyData();
            data.clearMorePacketsData();
        }
    }

    /**
     * When a player moves, he will be checked for various suspicious behaviors.<br>
     * (lowest priority)
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerMove(final PlayerMoveEvent event) {
        /*
         *  _____  _                         __  __                
         * |  __ \| |                       |  \/  |               
         * | |__) | | __ _ _   _  ___ _ __  | \  / | _____   _____ 
         * |  ___/| |/ _` | | | |/ _ \ '__| | |\/| |/ _ \ \ / / _ \
         * | |    | | (_| | |_| |  __/ |    | |  | | (_) \ V /  __/
         * |_|    |_|\__,_|\__, |\___|_|    |_|  |_|\___/ \_/ \___|
         *                  __/ |                                  
         *                 |___/                                   
         */
		final Player player = event.getPlayer();
		
		// Ignore players in vehicles.
		if (player.isInsideVehicle()) return;
		
		// Ignore dead players.
		if (player.isDead()) return;
		
		// Ignore sleeping players.
		if (player.isSleeping()){
			// TODO: check (which cb!) System.out.println("-> " + player.isSleepingIgnored());
			return;
		}

		final Location from = event.getFrom();
		final Location to = event.getTo();
		
		// Ignore changing worlds.
		if (!from.getWorld().equals(to.getWorld())) return;

        // Use existent locations if possible.
        final MoveInfo moveInfo;
        final PlayerLocation pFrom, pTo;
        if (parkedInfo.isEmpty()) moveInfo = new MoveInfo(mcAccess);
        else moveInfo = parkedInfo.remove(parkedInfo.size() - 1);
        pFrom = moveInfo.from;
        pTo = moveInfo.to;
        
        final MovingConfig cc = MovingConfig.getConfig(player);
        moveInfo.set(player, from, to, cc.yOnGround);
          
        if (cc.debug) {
			StringBuilder builder = new StringBuilder(250);
			final Location loc = player.getLocation();
			builder.append(player.getName());
			builder.append(" " + from.getWorld().getName() + " " + CheckUtils.fdec3.format(from.getX()) + (from.getX() == loc.getX() ? "" : ("(" + CheckUtils.fdec3.format(loc.getX()) + ")")));
			builder.append(", " + CheckUtils.fdec3.format(from.getY()) + (from.getY() == loc.getY() ? "" : ("(" + CheckUtils.fdec3.format(loc.getY()) + ")")));
			builder.append(", " + CheckUtils.fdec3.format(from.getZ()) + (from.getZ() == loc.getZ() ? "" : ("(" + CheckUtils.fdec3.format(loc.getZ()) + ")")));
			builder.append(" -> " + CheckUtils.fdec3.format(to.getX()) + ", " + CheckUtils.fdec3.format(to.getY()) + ", " + CheckUtils.fdec3.format(to.getZ()));
			System.out.print(builder.toString());
		}
        
		final MovingData data = MovingData.getData(player);
		data.noFallAssumeGround = false;
		data.resetTeleported();
		
		// Check for illegal move and bounding box etc.
		if (pFrom.isIllegal() || pTo.isIllegal()) {
			handleIllegalMove(event, player, data);
			moveInfo.cleanup();
			parkedInfo.add(moveInfo);
			return;
		}
        pFrom.collectBlockFlags(cc.noFallyOnGround);
        pTo.collectBlockFlags(cc.noFallyOnGround);
        
		// Potion effect "Jump".
		final double jumpAmplifier = MovingListener.getJumpAmplifier(player);
		if (jumpAmplifier > 0D && cc.debug) System.out.println(player.getName() + " Jump effect: " + jumpAmplifier);
		if (jumpAmplifier > data.jumpAmplifier) data.jumpAmplifier = jumpAmplifier;

        // Just try to estimate velocities over time. Not very precise, but works good enough most of the time. Do
        // general data modifications one for each event.
        if (data.horizontalVelocityCounter > 0D)
            data.horizontalVelocityCounter--;
        else if (data.horizontalFreedom > 0.001D)
            data.horizontalFreedom *= 0.90D;

        if (data.verticalVelocity <= 0.1D)
            data.verticalVelocityCounter--;
        if (data.verticalVelocityCounter > 0D) {
            data.verticalFreedom += data.verticalVelocity;
            data.verticalVelocity *= 0.93D;
        } else if (data.verticalFreedom > 0.001D)
            // Counter has run out, now reduce the vertical freedom over time.
            data.verticalFreedom *= 0.93D;

		Location newTo = null;

		final Location passableTo;
		// Check passable in any case (!)
		if (cc.passableCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_PASSABLE) && !player.hasPermission(Permissions.MOVING_PASSABLE)) {
			// Passable is checked first to get the original set-back locations from the other checks, if needed. 
			passableTo = passable.check(player, pFrom, pTo, data, cc);
		}
		else passableTo = null;
        
        // Optimized checking, giving creativefly permission precedence over survivalfly.
        if (!player.hasPermission(Permissions.MOVING_CREATIVEFLY)){
        	// Either survivalfly or speed check.
        	if ((cc.ignoreCreative || player.getGameMode() != GameMode.CREATIVE) && (cc.ignoreAllowFlight || !player.getAllowFlight()) 
        			&& cc.survivalFlyCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_SURVIVALFLY) && !player.hasPermission(Permissions.MOVING_SURVIVALFLY)){
                // If he is handled by the survival fly check, execute it.
                newTo = survivalFly.check(player, pFrom, pTo, data, cc);
				// Check NoFall if no reset is done.
				if (cc.noFallCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_NOFALL) && !player.hasPermission(Permissions.MOVING_NOFALL)) {
					if (passableTo != null){
						// Deal damage if necessary.
						// Leaving out: player.getLocation().getY()
						noFall.checkDamage(player, data, Math.min(from.getY(), to.getY()));
					}
					else if (newTo == null) {
						// NOTE: noFall might set yOnGround for the positions.
						noFall.check(player, pFrom, pTo, data, cc);
					}
					else{
						// Deal damage if necessary.
						// Leaving out: player.getLocation().getY()
						noFall.checkDamage(player, data, Math.min(from.getY(), to.getY()));
					}
				}
        	}
        	else if (cc.creativeFlyCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_CREATIVEFLY)){
        		// If the player is handled by the creative fly check, execute it.
                newTo = creativeFly.check(player, pFrom, pTo, data, cc);
        	}
        	else data.clearFlyData();
        }
        else data.clearFlyData();

		if (newTo == null && cc.morePacketsCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_MOREPACKETS) && !player.hasPermission(Permissions.MOVING_MOREPACKETS)) {
			// If he hasn't been stopped by any other check and is handled by the more packets check, execute it.
			newTo = morePackets.check(player, pFrom, pTo, data, cc);
		} else {
			// Otherwise we need to clear his data.
			data.clearMorePacketsData();
		}
		
		// Prefer the location returned by passable.
		if (passableTo != null) newTo = passableTo;

        // Did one of the checks decide we need a new "to"-location?
        if (newTo != null) {
            // Yes, so set it.
            event.setTo(newTo);

            // Remember where we send the player to.
            data.setTeleported(newTo);
            if (cc.debug){
            	System.out.println(player.getName() + " set back to: " + newTo.getWorld() + CheckUtils.fdec3.format(newTo.getX()) + ", " + CheckUtils.fdec3.format(newTo.getY()) + ", " + CheckUtils.fdec3.format(newTo.getZ()));
            }
        }
        
        // Set positions.
        // TODO: Should these be set on monitor ?
        data.fromX = from.getX();
        data.fromY = from.getY();
        data.fromZ = from.getZ();
        data.toX = to.getX();
        data.toY = to.getY();
        data.toZ = to.getZ();
        
        // Cleanup.
        moveInfo.cleanup();
        parkedInfo.add(moveInfo);
    }


	public static void handleIllegalMove(final PlayerMoveEvent event, final Player player, final MovingData data)
	{
		// This might get extended to a check-like thing.
		boolean restored = false;
		final PlayerLocation pLoc = new PlayerLocation(NoCheatPlus.getMCAccess(), null);
		// (Mind that we don't set the block cache here).
		final Location loc = player.getLocation();
		if (!restored && data.hasSetBack()) {
			final Location setBack = data.getSetBack(loc); 
			pLoc.set(setBack, player);
			if (!pLoc.isIllegal()){
				event.setFrom(setBack);
				event.setTo(setBack);
				restored = true;
			}
			else data.resetSetBack();
		} 
		if (!restored){
			pLoc.set(loc, player);
			if (!pLoc.isIllegal()) {
				event.setFrom(loc);
				event.setTo(loc);
				restored = true;
			}
		}
		pLoc.cleanup();
		if (!restored){
			 // TODO: correct the location ?
			NoCheatPlus.denyLogin(player.getName(), 24L * 60L * 60L * 1000L);
			LogUtil.logSevere("[NCP] could not restore location for " + player.getName() + " deny login for 24 hours");
		}
		// TODO: reset the bounding box of the player ?
		CheckUtils.onIllegalMove(player);
	}

	/**
     * A workaround for cancelled PlayerMoveEvents.
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMoveHighest(final PlayerMoveEvent event) {
        /*
         *  _____  _                         __  __                
         * |  __ \| |                       |  \/  |               
         * | |__) | | __ _ _   _  ___ _ __  | \  / | _____   _____ 
         * |  ___/| |/ _` | | | |/ _ \ '__| | |\/| |/ _ \ \ / / _ \
         * | |    | | (_| | |_| |  __/ |    | |  | | (_) \ V /  __/
         * |_|    |_|\__,_|\__, |\___|_|    |_|  |_|\___/ \_/ \___|
         *                  __/ |                                  
         *                 |___/                                   
         */
        // No typo here. I really only handle cancelled events and ignore others.
        if (!event.isCancelled() || event.getPlayer().isDead())
            return;

        // Fix a common mistake that other developers make (cancelling move events is crazy, rather set the target
        // location to the from location).
        event.setCancelled(false);
        event.setTo(event.getFrom()); // TODO: revise this (old!) strategy, cancelled events just teleport to from, basically.
    }
    
    /**
     * Monitor level PlayerMoveEvent.
     * @param event
     */
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = false)
    public final void onPlayerMoveMonitor(final PlayerMoveEvent event){
    	// TODO: revise: cancelled events.
        final long now = System.currentTimeMillis();
        final Player player = event.getPlayer();
        if (player.isDead()) return;
        final Location to = event.getTo(); // player.getLocation();
        final String worldName = to.getWorld().getName();
        
        // Feed combined check.
        final CombinedData data = CombinedData.getData(player);
        data.lastMoveTime = now;
        
        // Just add the yaw to the list.
        Combined.feedYawRate(player, to.getYaw(), now, worldName, data);
        
        final Location from = event.getFrom();
        // TODO: Might enforce data.teleported (!).
        
        // TODO: maybe even not count vehicles at all ?
        if (!from.getWorld().equals(to.getWorld()) || player.isInsideVehicle()){
            MovingData.getData(player).resetPositions(to);
            return;
        }
    }

    /**
     * When a player uses a portal, all information related to the moving checks becomes invalid.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPortal(final PlayerPortalEvent event) {
        /*
         *  ____  _                         ____            _        _ 
         * |  _ \| | __ _ _   _  ___ _ __  |  _ \ ___  _ __| |_ __ _| |
         * | |_) | |/ _` | | | |/ _ \ '__| | |_) / _ \| '__| __/ _` | |
         * |  __/| | (_| | |_| |  __/ |    |  __/ (_) | |  | || (_| | |
         * |_|   |_|\__,_|\__, |\___|_|    |_|   \___/|_|   \__\__,_|_|
         *                |___/                                        
         */
        final MovingData data = MovingData.getData(event.getPlayer());
        data.clearFlyData();
        data.clearMorePacketsData();
    }

	/**
	 * When a player respawns, all information related to the moving checks
	 * becomes invalid.
	 * 
	 * @param event
	 *            the event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(final PlayerRespawnEvent event) {
        /*
         *  ____  _                         ____                                      
         * |  _ \| | __ _ _   _  ___ _ __  |  _ \ ___  ___ _ __   __ ___      ___ __  
         * | |_) | |/ _` | | | |/ _ \ '__| | |_) / _ \/ __| '_ \ / _` \ \ /\ / / '_ \ 
         * |  __/| | (_| | |_| |  __/ |    |  _ <  __/\__ \ |_) | (_| |\ V  V /| | | |
         * |_|   |_|\__,_|\__, |\___|_|    |_| \_\___||___/ .__/ \__,_| \_/\_/ |_| |_|
         *                |___/                           |_|                         
         */
		final Player player = event.getPlayer();
		final MovingData data = MovingData.getData(player);
		data.clearFlyData();
		data.clearMorePacketsData();
		data.setSetBack(event.getRespawnLocation());
		// TODO: consider data.resetPositions(data.setBack);
	}
	
	/**
	 * Clear fly data on death.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final MovingData data = MovingData.getData(player);
		data.clearFlyData();
		data.clearMorePacketsData();
		data.setSetBack(player.getLocation()); // TODO: Monitor this change (!).
	}

    /**
     * If a player gets teleported, it may have two reasons. Either it was NoCheat or another plugin. If it was
     * NoCheatPlus, the target location should match the "data.teleportedTo" value.
     * 
     * On teleports, reset some movement related data that gets invalid.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        /*
         *  ____  _                         _____    _                       _   
         * |  _ \| | __ _ _   _  ___ _ __  |_   _|__| | ___ _ __   ___  _ __| |_ 
         * | |_) | |/ _` | | | |/ _ \ '__|   | |/ _ \ |/ _ \ '_ \ / _ \| '__| __|
         * |  __/| | (_| | |_| |  __/ |      | |  __/ |  __/ |_) | (_) | |  | |_ 
         * |_|   |_|\__,_|\__, |\___|_|      |_|\___|_|\___| .__/ \___/|_|   \__|
         *                |___/                            |_|                   
         */
		final Player player = event.getPlayer();
		final MovingData data = MovingData.getData(player);

		final Location teleported = data.getTeleported();

		// If it was a teleport initialized by NoCheatPlus, do it anyway even if another plugin said "no".
		final Location to = event.getTo();
		final Location ref;
		if (teleported != null && teleported.equals(to)) {
			// Teleport by NCP.
			// Prevent cheaters getting rid of flying data (morepackets, other).
			// TODO: even more strict enforcing ?
			if (event.isCancelled()) {
				event.setCancelled(false);
				event.setTo(teleported); // ?
				event.setFrom(teleported);
				ref = teleported;
			}
			else{
				// Not cancelled but NCP teleport.
				ref = to;
			}
			// TODO: This could be done on MONITOR.
			data.onSetBack(teleported);
		} else {
			// Only if it wasn't NoCheatPlus, drop data from more packets check.
			// TODO: check if to do with cancelled teleports !
			data.clearMorePacketsData();
			data.clearFlyData();
			ref = event.isCancelled() ? event.getFrom() : to;
			data.resetPositions(ref);
		}

        // TODO: NoFall might be necessary to be checked here ?
        data.resetTeleported();
        
        // Reset yawrate (experimental: might help preventing cascading improbable with rubberbanding).
        Combined.resetYawRate(player, ref.getYaw(), System.currentTimeMillis(), true);
    }

    /**
     * Player got a velocity packet. The server can't keep track of actual velocity values (by design), so we have to
     * try and do that ourselves. Very rough estimates.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerVelocity(final PlayerVelocityEvent event) {
        /*
         *  ____  _                        __     __   _            _ _         
         * |  _ \| | __ _ _   _  ___ _ __  \ \   / /__| | ___   ___(_) |_ _   _ 
         * | |_) | |/ _` | | | |/ _ \ '__|  \ \ / / _ \ |/ _ \ / __| | __| | | |
         * |  __/| | (_| | |_| |  __/ |      \ V /  __/ | (_) | (__| | |_| |_| |
         * |_|   |_|\__,_|\__, |\___|_|       \_/ \___|_|\___/ \___|_|\__|\__, |
         *                |___/                                           |___/ 
         */
        final Player player = event.getPlayer();
        final MovingData data = MovingData.getData(player);
        final MovingConfig cc = MovingConfig.getConfig(player);
        
        final Vector velocity = event.getVelocity();
        
        if (cc.debug) System.out.println(event.getPlayer().getName() + " new velocity: " + velocity);
        
        double newVal = velocity.getY();
        if (newVal >= 0D) {
            data.verticalVelocity += newVal;
            data.verticalFreedom += data.verticalVelocity;
        }

        data.verticalVelocityCounter = 50;

        newVal = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
        if (newVal > 0D) {
            data.horizontalFreedom += newVal;
            data.horizontalVelocityCounter = 30;
        }
    }

    /**
     * When a vehicle moves, its player will be checked for various suspicious behaviors.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onVehicleMove(final VehicleMoveEvent event) {
        /*
         * __     __   _     _      _        __  __                
         * \ \   / /__| |__ (_) ___| | ___  |  \/  | _____   _____ 
         *  \ \ / / _ \ '_ \| |/ __| |/ _ \ | |\/| |/ _ \ \ / / _ \
         *   \ V /  __/ | | | | (__| |  __/ | |  | | (_) \ V /  __/
         *    \_/ \___|_| |_|_|\___|_|\___| |_|  |_|\___/ \_/ \___|
         */
    	final Vehicle vehicle = event.getVehicle();
    	final Entity passenger = vehicle.getPassenger();
        // Don't care if a player isn't inside the vehicle, for movements that are very high distance or to another
        // world (such that it is very likely the event data was modified by another plugin before we got it).
        if (passenger == null || !(passenger instanceof Player)) return;
        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (!from.getWorld().equals(to.getWorld())) return;

        final Player player = (Player) event.getVehicle().getPassenger();

        Location newTo = null;

        if (morePacketsVehicle.isEnabled(player))
            // If the player is handled by the more packets vehicle check, execute it.
            newTo = morePacketsVehicle.check(player, from, to);
        else
            // Otherwise we need to clear his data.
            MovingData.getData(player).clearMorePacketsData();

        // Did one of the checks decide we need a new "to"-location?
        if (newTo != null)
            // Yes, so schedule a delayed task to teleport back the vehicle (this event isn't cancellable and we can't
            // teleport the vehicle within the event).
        	// TODO: cleanup?
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                private Vehicle  vehicle;
                private Location location;

                @Override
                public void run() {
                    vehicle.teleport(location, TeleportCause.PLUGIN);
                }

                public Runnable set(final Vehicle vehicle, final Location location) {
                    this.vehicle = vehicle;
                    this.location = location;
                    return this;
                }
            }.set(vehicle, newTo), 1L);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityDamage(final EntityDamageEvent event){
        if (event.getCause() != DamageCause.FALL) return;
        final Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        final Player player = (Player) entity;
        final MovingData data = MovingData.getData(player);
        final MovingConfig cc = MovingConfig.getConfig(player);
        if (event.isCancelled() || !shouldCheckSurvivalFly(player, data, cc) || !noFall.isEnabled(player)){
            data.clearNoFallData();
            return;
        }
        final Location loc = player.getLocation();
        boolean allowReset = true;
        if (!data.noFallSkipAirCheck){
        	final MoveInfo moveInfo;
        	if (parkedInfo.isEmpty()) moveInfo = new MoveInfo(mcAccess);
            else moveInfo = parkedInfo.remove(parkedInfo.size() - 1);
        	moveInfo.set(player, loc, null, cc.noFallyOnGround);
        	// NOTE: No isIllegal check here.
        	final PlayerLocation pLoc = moveInfo.from;
        	moveInfo.from.collectBlockFlags(cc.noFallyOnGround);
        	if (!pLoc.isOnGround() && !pLoc.isResetCond() && !pLoc.isAboveLadder() && !pLoc.isAboveStairs()){
        		// Likely a new style no-fall bypass (damage in mid-air).
        		data.noFallVL += 1.0;
        		if (noFall.executeActions(player, data.noFallVL, 1.0, cc.noFallActions, true) && data.hasSetBack()){
        			// Cancel the event and restore fall distance.
        			// NoFall data will not be reset 
        			allowReset = false;
        		}
        	}
        	moveInfo.cleanup();
        	parkedInfo.add(moveInfo);
        }
        final float fallDistance = player.getFallDistance();
        final int damage = event.getDamage();
        final float yDiff = (float) (data.noFallMaxY - loc.getY());
        if (cc.debug) System.out.println(player.getName() + " damage(FALL): " + damage + " / dist=" + player.getFallDistance() + " nf=" + data.noFallFallDistance + " yDiff=" + yDiff);
        // Fall-back check.
        final int maxD = NoFall.getDamage(Math.max(yDiff, Math.max(data.noFallFallDistance, fallDistance))) + (allowReset ? 0 : 3);
        if (maxD > damage){
            // TODO: respect dealDamage ?
            event.setDamage(maxD);
            if (cc.debug) System.out.println(player.getName() + " Adjust fall damage to: " + maxD);
        }
        if (allowReset){
        	// Normal fall damage, reset data.
        	data.clearNoFallData();
        }
        else{
        	// Minecraft/NCP bug or cheating.
        	// Cancel the event, apply damage later.
        	event.setCancelled(true);
        	// TODO: Add player to hover checks ?.
        }
        // Entity fall-distance should be reset elsewhere.
    }
    
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final MovingData data = MovingData.getData(player);
		// TODO: on existing set back: detect world changes and loss of world on join (+ set up some paradigm).
		data.clearMorePacketsData();
		final Location loc = player.getLocation();
		if (!data.hasSetBack() || data.hasSetBackWorldChanged(loc)){
			data.setSetBack(loc);
		}
		if (data.fromX == Double.MAX_VALUE && data.toX == Double.MAX_VALUE){
			// TODO: re-think: more fine grained reset?
			data.resetPositions(loc);
		}
	}
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event){
    	onLeave(event.getPlayer());
        
    }
    
    private void onLeave(final Player player) {
    	survivalFly.setReallySneaking(player, false);
        noFall.onLeave(player);
        
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(final PlayerKickEvent event){
        onLeave(event.getPlayer());
    }

	/**
	 * Determine "some jump amplifier": 1 is jump boost, 2 is jump boost II. <br>
	 * NOTE: This is not the original amplifier value (use mcAccess for that).
	 * @param mcPlayer
	 * @return
	 */
	public static final double getJumpAmplifier(final Player player) {
		final double amplifier = NoCheatPlus.getMCAccess().getJumpAmplifier(player);
		if (amplifier == Double.MIN_VALUE) return 0D;
		else return 1D + amplifier;
	}
	
	/**
	 * Heavier check, but convenient for seldom events (not for use in the player-move check).
	 * @param player
	 * @param data
	 * @param cc
	 * @return
	 */
	public final boolean shouldCheckSurvivalFly(final Player player, final MovingData data, final MovingConfig cc){
		if (player.hasPermission(Permissions.MOVING_CREATIVEFLY)) return false;
		else if (!survivalFly.isEnabled(player)) return false;
		else if ((cc.ignoreCreative || player.getGameMode() != GameMode.CREATIVE) && (cc.ignoreAllowFlight || !player.getAllowFlight())){
			return true;
		}
		else return false;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerToggleSneak(final PlayerToggleSneakEvent event){
		survivalFly.setReallySneaking(event.getPlayer(), event.isSneaking());
	}
}
