package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * The only place that listens to and modifies player_move events if necessary
 * 
 * Get the event, decide which checks should work on it and in what order,
 * evaluate the check results and decide what to
 * 
 */
public class MovingCheckListener implements Listener, EventManager {

    private final List<MovingCheck> checks;
    private final NoCheat           plugin;

    public MovingCheckListener(NoCheat plugin) {

        this.checks = new ArrayList<MovingCheck>(2);

        checks.add(new RunflyCheck(plugin));
        checks.add(new MorePacketsCheck(plugin));

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockPlace(final BlockPlaceEvent event) {
        if(event.isCancelled())
            return;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        // Get the player-specific stored data that applies here
        final MovingData data = MovingCheck.getData(player.getDataStore());

        final Block blockPlaced = event.getBlockPlaced();

        if(blockPlaced == null || !data.runflySetBackPoint.isSet()) {
            return;
        }

        final SimpleLocation lblock = new SimpleLocation();
        lblock.set(blockPlaced);
        final SimpleLocation lplayer = new SimpleLocation();
        lplayer.setLocation(player.getPlayer().getLocation());

        if(Math.abs(lplayer.x - lblock.x) <= 1 && Math.abs(lplayer.z - lblock.z) <= 1 && lplayer.y - lblock.y >= 0 && lplayer.y - lblock.y <= 2) {

            final int type = CheckUtil.getType(blockPlaced.getTypeId());
            if(CheckUtil.isSolid(type) || CheckUtil.isLiquid(type)) {
                if(lblock.y + 1 >= data.runflySetBackPoint.y) {
                    data.runflySetBackPoint.y = (lblock.y + 1);
                    data.jumpPhase = 0;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void teleport(final PlayerTeleportEvent event) {

        NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final MovingData data = MovingCheck.getData(player.getDataStore());

        // If it was a teleport initialized by NoCheat, do it anyway
        if(data.teleportTo.isSet() && data.teleportTo.equals(event.getTo())) {
            event.setCancelled(false);
        } else {
            // Only if it wasn't NoCheat, drop data from morepackets check
            data.clearMorePacketsData();
        }

        // Always forget runfly specific data
        data.teleportTo.reset();
        data.clearRunFlyData();

        return;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void worldChange(final PlayerChangedWorldEvent event) {
        final MovingData data = MovingCheck.getData(plugin.getPlayer(event.getPlayer()).getDataStore());
        data.teleportTo.reset();
        data.clearRunFlyData();
        data.clearMorePacketsData();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void portal(final PlayerPortalEvent event) {
        final MovingData data = MovingCheck.getData(plugin.getPlayer(event.getPlayer()).getDataStore());
        data.clearMorePacketsData();
        data.clearRunFlyData();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void respawn(final PlayerRespawnEvent event) {
        final MovingData data = MovingCheck.getData(plugin.getPlayer(event.getPlayer()).getDataStore());
        data.clearMorePacketsData();
        data.clearRunFlyData();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void move(final PlayerMoveEvent event) {

        if(event.isCancelled())
            return;
        // Get the world-specific configuration that applies here
        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());

        // Not interested at all in players in vehicles or dead
        if(event.getPlayer().isInsideVehicle() || player.isDead()) {
            return;
        }

        final MovingConfig cc = MovingCheck.getConfig(player.getConfigurationStore());
        final MovingData data = MovingCheck.getData(player.getDataStore());

        // Various calculations related to velocity estimates
        updateVelocities(data);

        if(!cc.check || player.hasPermission(Permissions.MOVING)) {
            // Just because he is allowed now, doesn't mean he will always
            // be. So forget data about the player related to moving
            data.clearRunFlyData();
            data.clearMorePacketsData();
            return;
        }

        // Get some data that's needed from this event, to avoid passing the
        // event itself on to the checks (and risk to
        // accidentally modifying the event there)

        final Location to = event.getTo();

        data.from.set(event.getFrom());
        data.to.set(to);

        // This variable will have the modified data of the event (new
        // "to"-location)
        PreciseLocation newTo = null;

        for(MovingCheck check : checks) {
            if(newTo == null && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                newTo = check.check(player, data, cc);
            }
        }

        // Did the check(s) decide we need a new "to"-location?
        if(newTo != null) {
            // Compose a new location based on coordinates of "newTo" and
            // viewing direction of "event.getTo()"
            event.setTo(new Location(player.getPlayer().getWorld(), newTo.x, newTo.y, newTo.z, to.getYaw(), to.getPitch()));

            data.teleportTo.set(newTo);
        }
    }

    private void updateVelocities(MovingData data) {

        /******** DO GENERAL DATA MODIFICATIONS ONCE FOR EACH EVENT *****/
        if(data.horizVelocityCounter > 0) {
            data.horizVelocityCounter--;
        } else if(data.horizFreedom > 0.001) {
            data.horizFreedom *= 0.90;
        }

        if(data.vertVelocity <= 0.1) {
            data.vertVelocityCounter--;
        }
        if(data.vertVelocityCounter > 0) {
            data.vertFreedom += data.vertVelocity;
            data.vertVelocity *= 0.90;
        } else if(data.vertFreedom > 0.001) {
            // Counter has run out, now reduce the vert freedom over time
            data.vertFreedom *= 0.93;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void velocity(final PlayerVelocityEvent event) {
        if(event.isCancelled())
            return;
        final MovingData data = MovingCheck.getData(plugin.getPlayer(event.getPlayer()).getDataStore());

        final Vector v = event.getVelocity();

        double newVal = v.getY();
        if(newVal >= 0.0D) {
            data.vertVelocity += newVal;
            data.vertFreedom += data.vertVelocity;
        }

        data.vertVelocityCounter = 50;

        newVal = Math.sqrt(Math.pow(v.getX(), 2) + Math.pow(v.getZ(), 2));
        if(newVal > 0.0D) {
            data.horizFreedom += newVal;
            data.horizVelocityCounter = 30;
        }
    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        MovingConfig m = MovingCheck.getConfig(cc);

        if(m.check) {
            if(m.runflyCheck) {

                if(!m.allowFlying) {
                    s.add("moving.runfly");
                    if(m.sneakingCheck)
                        s.add("moving.sneaking");
                    if(m.nofallCheck)
                        s.add("moving.nofall");
                } else
                    s.add("moving.flying");

            }
            if(m.morePacketsCheck)
                s.add("moving.morepackets");
        }

        return s;
    }
}
