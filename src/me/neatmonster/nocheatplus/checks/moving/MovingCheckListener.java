package me.neatmonster.nocheatplus.checks.moving;

import java.util.LinkedList;
import java.util.List;

import me.neatmonster.nocheatplus.EventManager;
import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.checks.CheckUtil;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.config.Permissions;
import me.neatmonster.nocheatplus.data.PreciseLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Central location to listen to events that are
 * relevant for the moving checks
 * 
 */
public class MovingCheckListener implements Listener, EventManager {

    private final MorePacketsCheck        morePacketsCheck;
    private final MorePacketsVehicleCheck morePacketsVehicleCheck;
    private final FlyingCheck             flyingCheck;
    private final RunningCheck            runningCheck;
    private final TrackerCheck            trackerCheck;
    private final WaterWalkCheck          waterWalkCheck;

    private final NoCheatPlus             plugin;

    public MovingCheckListener(final NoCheatPlus plugin) {

        flyingCheck = new FlyingCheck(plugin);
        runningCheck = new RunningCheck(plugin);
        trackerCheck = new TrackerCheck(plugin);
        morePacketsCheck = new MorePacketsCheck(plugin);
        morePacketsVehicleCheck = new MorePacketsVehicleCheck(plugin);
        waterWalkCheck = new WaterWalkCheck(plugin);

        // Schedule a new synchronized repeating task repeated 20 times/s.
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {

                // Loop through all players
                for (final Player bukkitPlayer : Bukkit.getOnlinePlayers()) {

                    // Get some data about the player/config
                    final NoCheatPlusPlayer player = plugin.getPlayer(bukkitPlayer);
                    final MovingConfig cc = MovingCheck.getConfig(player);
                    final MovingData data = MovingCheck.getData(player);

                    // Execute the check
                    trackerCheck.check(player, data, cc);
                }
            }
        }, 1L, 1L);

        this.plugin = plugin;
    }

    /**
     * A workaround for players placing blocks below them getting pushed
     * off the block by NoCheatPlus.
     * 
     * It essentially moves the "setbackpoint" to the top of the newly
     * placed block, therefore tricking NoCheatPlus into thinking the player
     * was already on top of that block and should be allowed to stay
     * there
     * 
     * @param event
     *            The BlockPlaceEvent
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void blockPlace(final BlockPlaceEvent event) {

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final MovingConfig config = MovingCheck.getConfig(player);

        // If the player is allowed to fly anyway, the workaround is not needed
        // It's kind of expensive (looking up block types) therefore it makes
        // sense to avoid it
        if (config.allowFlying || !config.runflyCheck || player.hasPermission(Permissions.MOVING_FLYING)
                || player.hasPermission(Permissions.MOVING_RUNFLY))
            return;

        // Get the player-specific stored data that applies here
        final MovingData data = MovingCheck.getData(player);

        final Block block = event.getBlockPlaced();

        if (block == null || !data.runflySetBackPoint.isSet())
            return;

        // Keep some results of "expensive calls
        final Location l = player.getPlayer().getLocation();
        final int playerX = l.getBlockX();
        final int playerY = l.getBlockY();
        final int playerZ = l.getBlockZ();
        final int blockY = block.getY();

        // Was the block below the player?
        if (Math.abs(playerX - block.getX()) <= 1 && Math.abs(playerZ - block.getZ()) <= 1 && playerY - blockY >= 0
                && playerY - blockY <= 2) {
            // yes
            final int type = CheckUtil.getType(block.getTypeId());
            if (CheckUtil.isSolid(type) || CheckUtil.isLiquid(type))
                if (blockY + 1 >= data.runflySetBackPoint.y) {
                    data.runflySetBackPoint.y = blockY + 1;
                    data.jumpPhase = 0;
                }
        }
    }

    /**
     * If a player tries to place a boat on the ground, the event
     * will be cancelled.
     * 
     * @param event
     *            The PlayerInteractEvent
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void boat(final PlayerInteractEvent event) {
        if (!event.getPlayer().hasPermission(Permissions.MOVING_BOATONGROUND)
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getPlayer().getItemInHand().getType() == Material.BOAT
                && event.getClickedBlock().getType() != Material.WATER
                && event.getClickedBlock().getType() != Material.STATIONARY_WATER
                && event.getClickedBlock().getRelative(event.getBlockFace()).getType() != Material.WATER
                && event.getClickedBlock().getRelative(event.getBlockFace()).getType() != Material.STATIONARY_WATER)
            event.setCancelled(true);
    }

    @Override
    public List<String> getActiveChecks(final ConfigurationCacheStore cc) {
        final LinkedList<String> s = new LinkedList<String>();

        final MovingConfig m = MovingCheck.getConfig(cc);

        if (m.runflyCheck)
            if (!m.allowFlying) {
                s.add("moving.runfly");
                if (m.sneakingCheck)
                    s.add("moving.sneaking");
                if (m.nofallCheck)
                    s.add("moving.nofall");
                if (m.waterWalkCheck)
                    s.add("moving.waterwalk");
            } else
                s.add("moving.flying");
        if (m.morePacketsCheck)
            s.add("moving.morepackets");
        if (m.morePacketsVehicleCheck)
            s.add("moving.morepacketsvehicle");

        return s;
    }

    /**
     * This event handler is used to prevent the player from quickly
     * disconnecting/reconnecting in order to cancel his fall damages.
     * 
     * @param event
     *            The PlayerJoinEvent
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void join(final PlayerJoinEvent event) {

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final MovingData data = MovingCheck.getData(player);

        // If the player has joined in the air and a safe location is defined...
        if (player.getPlayer().getLocation().add(0, -1, 0).getBlock().getType() == Material.AIR
                && data.lastSafeLocations[0] != null)
            // ...then teleport him to this location
            event.getPlayer().teleport(data.lastSafeLocations[0]);
    }

    /**
     * When a player moves, he will be checked for various
     * suspicious behaviour.
     * 
     * @param event
     *            The PlayerMoveEvent
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void move(final PlayerMoveEvent event) {

        // Don't care for vehicles
        if (event.getPlayer().isInsideVehicle())
            return;

        // Don't care for movements that are very high distance or to another
        // world (such that it is very likely the event data was modified by
        // another plugin before we got it)
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())
                || event.getFrom().distanceSquared(event.getTo()) > 400)
            return;

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());

        final MovingConfig cc = MovingCheck.getConfig(player);
        final MovingData data = MovingCheck.getData(player);

        // Advance various counters and values that change per movement
        // tick. They are needed to decide on how fast a player may
        // move.
        tickVelocities(data);

        // Remember locations
        data.from.set(event.getFrom());
        final Location to = event.getTo();
        data.to.set(to);

        // Remember safe locations
        if (Math.abs(event.getPlayer().getVelocity().getY()) < 0.0785D) {
            data.lastSafeLocations[0] = data.lastSafeLocations[1];
            data.lastSafeLocations[1] = event.getFrom();
        }

        PreciseLocation newTo = null;

        /** RUNFLY CHECK SECTION **/
        // If the player isn't handled by runfly checks
        if (!cc.runflyCheck || player.hasPermission(Permissions.MOVING_RUNFLY))
            // Just because he is allowed now, doesn't mean he will always
            // be. So forget data about the player related to moving
            data.clearRunFlyData();
        else if (cc.allowFlying || player.isCreative() && cc.identifyCreativeMode
                || player.hasPermission(Permissions.MOVING_FLYING))
            // Only do the limited flying check
            newTo = flyingCheck.check(player, data, cc);
        else
            // Go for the full treatment
            newTo = runningCheck.check(player, data, cc);

        /** WATERWALK CHECK SECTION **/
        if (newTo == null && cc.waterWalkCheck && !cc.allowFlying && (!player.isCreative() || !cc.identifyCreativeMode)
                && (!cc.runflyCheck || !player.hasPermission(Permissions.MOVING_FLYING))
                && !player.hasPermission(Permissions.MOVING_WATERWALK))
            newTo = waterWalkCheck.check(player, data, cc);

        /** MOREPACKETS CHECK SECTION **/
        if (!cc.morePacketsCheck || player.hasPermission(Permissions.MOVING_MOREPACKETS))
            data.clearMorePacketsData();
        else if (newTo == null)
            newTo = morePacketsCheck.check(player, data, cc);

        // Did one of the check(s) decide we need a new "to"-location?
        if (newTo != null) {
            // Compose a new location based on coordinates of "newTo" and
            // viewing direction of "event.getTo()" to allow the player to
            // look somewhere else despite getting pulled back by NoCheatPlus
            event.setTo(new Location(player.getPlayer().getWorld(), newTo.x, newTo.y, newTo.z, to.getYaw(), to
                    .getPitch()));

            // remember where we send the player to
            data.teleportTo.set(newTo);
        }
    }

    /**
     * When a player uses a portal, all information related to the
     * moving checks becomes invalid.
     * 
     * @param event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void portal(final PlayerPortalEvent event) {
        final MovingData data = MovingCheck.getData(plugin.getPlayer(event.getPlayer()));
        data.clearMorePacketsData();
        data.clearRunFlyData();
    }

    /**
     * This events listener fixes the exploitation of the safe
     * respawn location (usually exploited with gravel or sand).
     * 
     * @param event
     */
    @EventHandler
    public void quit(final PlayerQuitEvent event) {

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final MovingData data = MovingCheck.getData(player);

        // Reset the variable
        data.fallingSince = 0L;

        if (!event.getPlayer().hasPermission(Permissions.MOVING_RESPAWNTRICK)
                && (event.getPlayer().getLocation().getBlock().getType() == Material.GRAVEL || event.getPlayer()
                        .getLocation().getBlock().getType() == Material.SAND)) {
            event.getPlayer().getLocation().getBlock().setType(Material.AIR);
            event.getPlayer().getLocation().add(0, 1, 0).getBlock().setType(Material.AIR);
        }
    }

    /**
     * When a player respawns, all information related to the
     * moving checks becomes invalid.
     * 
     * @param event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void respawn(final PlayerRespawnEvent event) {
        final MovingData data = MovingCheck.getData(plugin.getPlayer(event.getPlayer()));
        data.clearMorePacketsData();
        data.clearRunFlyData();
    }

    /**
     * If a player gets teleported, it may have two reasons. Either
     * it was NoCheatPlus or another plugin. If it was NoCheatPlus, the target
     * location should match the "data.teleportTo" value.
     * 
     * On teleports, reset some movement related data that gets invalid
     * 
     * @param event
     *            The PlayerTeleportEvent
     */
    @EventHandler(
            priority = EventPriority.HIGHEST)
    public void teleport(final PlayerTeleportEvent event) {

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final MovingData data = MovingCheck.getData(player);

        // If it was a teleport initialized by NoCheatPlus, do it anyway
        // even if another plugin said "no"
        if (data.teleportTo.isSet() && data.teleportTo.equals(event.getTo()))
            event.setCancelled(false);
        else
            // Only if it wasn't NoCheatPlus, drop data from morepackets check.
            // If it was NoCheatPlus, we don't want players to exploit the
            // runfly check teleporting to get rid of the "morepackets"
            // data.
            data.clearMorePacketsData();

        // Always drop data from runfly check, as it always loses its validity
        // after teleports. Always!
        data.teleportTo.reset();
        data.clearRunFlyData();
    }

    /**
     * Just try to estimate velocities over time
     * Not very precise, but works good enough most
     * of the time.
     * 
     * @param data
     */
    private void tickVelocities(final MovingData data) {

        /******** DO GENERAL DATA MODIFICATIONS ONCE FOR EACH EVENT *****/
        if (data.horizVelocityCounter > 0)
            data.horizVelocityCounter--;
        else if (data.horizFreedom > 0.001)
            data.horizFreedom *= 0.90;

        if (data.vertVelocity <= 0.1)
            data.vertVelocityCounter--;
        if (data.vertVelocityCounter > 0) {
            data.vertFreedom += data.vertVelocity;
            data.vertVelocity *= 0.90;
        } else if (data.vertFreedom > 0.001)
            // Counter has run out, now reduce the vert freedom over time
            data.vertFreedom *= 0.93;
    }

    /**
     * When an vehicle moves, it will be checked for various
     * suspicious behaviour.
     * 
     * @param event
     *            The VehicleMoveEvent
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void vehicleMove(final VehicleMoveEvent event) {

        // Don't care for vehicles without passenger
        if (event.getVehicle().getPassenger() == null || !(event.getVehicle().getPassenger() instanceof Player))
            return;

        // Don't care for movements that are very high distance or to another
        // world (such that it is very likely the event data was modified by
        // another plugin before we got it)
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())
                || event.getFrom().distanceSquared(event.getTo()) > 400)
            return;

        final NoCheatPlusPlayer player = plugin.getPlayer((Player) event.getVehicle().getPassenger());

        final MovingConfig cc = MovingCheck.getConfig(player);
        final MovingData data = MovingCheck.getData(player);

        // Remember locations
        data.fromVehicle.set(event.getFrom());
        final Location to = event.getTo();
        data.toVehicle.set(to);

        if (cc.morePacketsVehicleCheck && !player.hasPermission(Permissions.MOVING_MOREPACKETSVEHICLE)
                && morePacketsVehicleCheck.check(player, data, cc)) {
            // Drop the usual items
            event.getVehicle().getWorld()
                    .dropItemNaturally(event.getVehicle().getLocation(), new ItemStack(Material.WOOD, 3));
            event.getVehicle().getWorld()
                    .dropItemNaturally(event.getVehicle().getLocation(), new ItemStack(Material.STICK, 2));
            // Remove the passenger
            if (event.getVehicle().getPassenger() != null)
                event.getVehicle().setPassenger(null);
            // Destroy the vehicle
            event.getVehicle().remove();
        }
    }

    /**
     * Player got a velocity packet. The server can't keep track
     * of actual velocity values (by design), so we have to try
     * and do that ourselves. Very rough estimates.
     * 
     * @param event
     *            The PlayerVelocityEvent
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void velocity(final PlayerVelocityEvent event) {

        final MovingData data = MovingCheck.getData(plugin.getPlayer(event.getPlayer()));

        // Reset the tracker's data
        data.fallingSince = 0L;

        final Vector v = event.getVelocity();

        double newVal = v.getY();
        if (newVal >= 0.0D) {
            data.vertVelocity += newVal;
            data.vertFreedom += data.vertVelocity;
        }

        data.vertVelocityCounter = 50;

        newVal = Math.sqrt(Math.pow(v.getX(), 2) + Math.pow(v.getZ(), 2));
        if (newVal > 0.0D) {
            data.horizFreedom += newVal;
            data.horizVelocityCounter = 30;
        }
    }

    /**
     * Just for security, if a player switches between worlds, reset the
     * runfly and morepackets checks data, because it is definitely invalid
     * now
     * 
     * @param event
     *            The PlayerChangedWorldEvent
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void worldChange(final PlayerChangedWorldEvent event) {
        // Maybe this helps with people teleporting through multiverse portals having problems?
        final MovingData data = MovingCheck.getData(plugin.getPlayer(event.getPlayer()));
        data.teleportTo.reset();
        data.clearRunFlyData();
        data.clearMorePacketsData();
    }
}
