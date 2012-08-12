package fr.neatmonster.nocheatplus.checks.moving;

import net.minecraft.server.Block;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.players.Permissions;
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
public class MovingListener implements Listener {

    /** The no fall check. **/
    public final static NoFall noFall = new NoFall();

    /**
     * Checks if a material is liquid.
     * 
     * @param material
     *            the material
     * @return true, if the material is liquid
     */
    public static boolean isLiquid(final Material material) {
        return material == Material.LAVA || material == Material.STATIONARY_LAVA
                || material == Material.STATIONARY_WATER || material == Material.WATER;
    }

    /** The instance of NoCheatPlus. */
    private final NoCheatPlus        plugin             = (NoCheatPlus) Bukkit.getPluginManager().getPlugin(
                                                                "NoCheatPlus");

    /** The creative fly check. */
    private final CreativeFly        creativeFly        = new CreativeFly();

    /** The more packets check. */
    private final MorePackets        morePackets        = new MorePackets();

    /** The more packets vehicle check. */
    private final MorePacketsVehicle morePacketsVehicle = new MorePacketsVehicle();

    /** The survival fly check. */
    private final SurvivalFly        survivalFly        = new SurvivalFly();

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

        final MovingData data = MovingData.getData(player);

        final int blockY = event.getBlock().getY();
        if (isLiquid(event.getBlockAgainst().getType()) && event.getBlock().getType() != Material.WATER_LILY)
            // The block was placed against a liquid block, cancel its placement.
            event.setCancelled(true);
        else if ((creativeFly.isEnabled(player) || survivalFly.isEnabled(player)) && event.getBlock() != null
                && data.setBack != null && blockY + 1D >= data.setBack.getY()
                && Math.abs(player.getLocation().getX() - event.getBlock().getX()) <= 1D
                && Math.abs(player.getLocation().getZ() - event.getBlock().getZ()) <= 1D
                && player.getLocation().getY() - blockY > 0D && player.getLocation().getY() - blockY < 2D
                && (Block.i(event.getBlock().getTypeId()) || isLiquid(event.getBlock().getType()))) {
            // The creative fly and/or survival fly check is enabled, the block was placed below the player and is
            // solid, so do what we have to do.
            data.setBack.setY(blockY + 1D);
            data.survivalFlyJumpPhase = 0;
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
        MovingData.getData(event.getPlayer()).survivalFlyWasInBed = true;
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
        if (survivalFly.isEnabled(player) && survivalFly.check(player))
            // To cancel the event, we simply teleport the player to his last safe location.
            player.teleport(MovingData.getData(player).lastSafeLocations[1]);
    }

    /**
     * Just for security, if a player switches between worlds, reset the fly and more packets checks dataFactory,
     * because it is definitely invalid now.
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
        final MovingData data = MovingData.getData(event.getPlayer());
        data.teleported = null;
        data.clearFlyData();
        data.clearMorePacketsData();
    }

    /**
     * We listen to this event to cancel the placement of boat the ground. Boats are made to float on water, right?
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
        if (!event.getPlayer().hasPermission(Permissions.MOVING_BOATSANYWHERE)
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getPlayer().getItemInHand().getType() == Material.BOAT
                && event.getClickedBlock().getType() != Material.WATER
                && event.getClickedBlock().getType() != Material.STATIONARY_WATER
                && event.getClickedBlock().getRelative(event.getBlockFace()).getType() != Material.WATER
                && event.getClickedBlock().getRelative(event.getBlockFace()).getType() != Material.STATIONARY_WATER)
            // If the player right clicked on a non-liquid block with a boat in his hands, cancel the event.
            event.setCancelled(true);
    }

    /**
     * We listen to this event to prevent players from leaving while falling, so from avoiding fall damages.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        /*
         *  ____  _                             _       _       
         * |  _ \| | __ _ _   _  ___ _ __      | | ___ (_)_ __  
         * | |_) | |/ _` | | | |/ _ \ '__|  _  | |/ _ \| | '_ \ 
         * |  __/| | (_| | |_| |  __/ |    | |_| | (_) | | | | |
         * |_|   |_|\__,_|\__, |\___|_|     \___/ \___/|_|_| |_|
         *                |___/                                 
         */
        final Player player = event.getPlayer();
        final MovingData data = MovingData.getData(player);

        // If the player has joined in the air and a safe location is defined...
        if (player.getLocation().add(0D, -1D, 0D).getBlock().getType() == Material.AIR
                && MovingData.getData(player).lastSafeLocations[0] != null)
            // ...teleport him to this location.
            player.teleport(data.lastSafeLocations[0]);
    }

    /**
     * When a player moves, he will be checked for various suspicious behaviors.
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

        // Don't care for movements that are very high distance, to another world (such that it is very likely the event
        // dataFactory was modified by another plugin before we got it) or if the player is inside a vehicle.
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())
                || event.getFrom().distanceSquared(event.getTo()) > 400D || player.isInsideVehicle())
            return;

        final MovingData data = MovingData.getData(player);

        // Just try to estimate velocities over time. Not very precise, but works good enough most of the time. Do
        // general dataFactory modifications one for each event.
        if (data.horizontalVelocityCounter > 0D)
            data.horizontalVelocityCounter--;
        else if (data.horizontalFreedom > 0.001D)
            data.horizontalFreedom *= 0.90D;

        if (data.verticalVelocity <= 0.1D)
            data.verticalVelocityCounter--;
        if (data.verticalVelocityCounter > 0D) {
            data.verticalFreedom += data.verticalVelocity;
            data.verticalVelocity *= 0.90D;
        } else if (data.verticalFreedom > 0.001D)
            // Counter has run out, now reduce the vertical freedom over time.
            data.verticalFreedom *= 0.93D;

        // Remember the location if it's safe.
        if (Math.abs(event.getPlayer().getVelocity().getY()) < 0.0785D) {
            data.lastSafeLocations[0] = data.lastSafeLocations[1];
            data.lastSafeLocations[1] = event.getFrom();
        }

        final PlayerLocation from = new PlayerLocation(event.getFrom(), player);
        data.from = from.getLocation();
        final PlayerLocation to = new PlayerLocation(event.getTo(), player);
        data.to = to.getLocation();

        Location newTo = null;

        if ((player.getGameMode() == GameMode.CREATIVE || player.getAllowFlight()) && creativeFly.isEnabled(player))
            // If the player is handled by the creative fly check, execute it.
            newTo = creativeFly.check(player, from, to);
        else if (survivalFly.isEnabled(player)) {
            // If he is handled by the survival fly check, execute it.
            newTo = survivalFly.check(player, from, to);
            // If don't have a new location and if he is handled by the no fall check, execute it.
            if (newTo == null && noFall.isEnabled(player))
                noFall.check(player, from, to);
        } else
            // He isn't handled by any fly check, clear his dataFactory.
            data.clearFlyData();

        if (newTo == null && morePackets.isEnabled(player))
            // If he hasn't been stopped by any other check and is handled by the more packets check, execute it.
            newTo = morePackets.check(player, from, to);
        else
            // Otherwise we need to clear his dataFactory.
            data.clearMorePacketsData();

        // Did one of the checks decide we need a new "to"-location?
        if (newTo != null) {
            // Yes, so set it.
            event.setTo(newTo);
            // Remember where we send the player to.
            data.teleported = newTo;
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
     * When a player respawns, all information related to the moving checks becomes invalid.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        /*
         *  ____  _                         ____                                      
         * |  _ \| | __ _ _   _  ___ _ __  |  _ \ ___  ___ _ __   __ ___      ___ __  
         * | |_) | |/ _` | | | |/ _ \ '__| | |_) / _ \/ __| '_ \ / _` \ \ /\ / / '_ \ 
         * |  __/| | (_| | |_| |  __/ |    |  _ <  __/\__ \ |_) | (_| |\ V  V /| | | |
         * |_|   |_|\__,_|\__, |\___|_|    |_| \_\___||___/ .__/ \__,_| \_/\_/ |_| |_|
         *                |___/                           |_|                         
         */
        final MovingData data = MovingData.getData(event.getPlayer());
        data.clearFlyData();
        data.clearMorePacketsData();
    }

    /**
     * If a player gets teleported, it may have two reasons. Either it was NoCheat or another plugin. If it was
     * NoCheatPlus, the target location should match the "dataFactory.teleportedTo" value.
     * 
     * On teleports, reset some movement related dataFactory that gets invalid.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.HIGHEST)
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

        // If it was a teleport initialized by NoCheatPlus, do it anyway even if another plugin said "no".
        if (data.teleported != null && data.teleported.equals(event.getTo()))
            event.setCancelled(false);
        else
            // Only if it wasn't NoCheatPlus, drop dataFactory from more packets check. If it was NoCheatPlus, we don't
            // want
            // players to exploit the fly check teleporting to get rid of the "morepackets" dataFactory.
            data.clearMorePacketsData();

        // Always drop dataFactory from fly checks, as it always loses its validity after teleports. Always!
        data.teleported = null;
        data.clearFlyData();
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
        final MovingData data = MovingData.getData(event.getPlayer());

        final Vector velocity = event.getVelocity();

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
        // Don't care if a player isn't inside the vehicle, for movements that are very high distance or to another
        // world (such that it is very likely the event dataFactory was modified by another plugin before we got it).
    	final Location to = event.getTo();
        if (event.getVehicle().getPassenger() == null || !(event.getVehicle().getPassenger() instanceof Player)
                || !event.getFrom().getWorld().equals(to.getWorld())
                || event.getFrom().distanceSquared(to) > 400D)
            return;

        final Player player = (Player) event.getVehicle().getPassenger();

        Location newTo = null;
        
        // Emergency fix attempt:
        final MovingData data = MovingData.getData(player);
        data.clearFlyData();
        player.setFallDistance(0.0f);
        data.noFallY = to.getY();
        
        if (morePacketsVehicle.isEnabled(player))
            // If the player is handled by the more packets vehicle check, execute it.
            newTo = morePacketsVehicle.check(player, event.getFrom(), to);
        else
            // Otherwise we need to clear his dataFactory.
            data.clearMorePacketsData();
        
        

        // Did one of the checks decide we need a new "to"-location?
        if (newTo != null)
            // Yes, so schedule a delayed task to teleport back the vehicle (this event isn't cancellable and we can't
            // teleport the vehicle within the event).
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                private Vehicle  vehicle;
                private Location location;

                @Override
                public void run() {
                    vehicle.teleport(location);
                }

                public Runnable set(final Vehicle vehicle, final Location location) {
                    this.vehicle = vehicle;
                    this.location = location;
                    return this;
                }
            }.set(event.getVehicle(), newTo), 1L);
    }
}
