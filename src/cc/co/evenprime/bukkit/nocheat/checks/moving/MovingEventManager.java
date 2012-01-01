package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.EventType;
import cc.co.evenprime.bukkit.nocheat.events.EventManagerImpl;

/**
 * The only place that listens to and modifies player_move events if necessary
 * 
 * Get the event, decide which checks should work on it and in what order,
 * evaluate the check results and decide what to
 * 
 */
public class MovingEventManager extends EventManagerImpl {

    private final List<MovingCheck> checks;

    public MovingEventManager(NoCheat plugin) {

        super(plugin);

        this.checks = new ArrayList<MovingCheck>(2);

        checks.add(new RunflyCheck(plugin));
        checks.add(new MorePacketsCheck(plugin));

        registerListener(Event.Type.PLAYER_MOVE, Priority.Lowest, true, plugin.getPerformance(EventType.MOVING));
        registerListener(Event.Type.PLAYER_VELOCITY, Priority.Monitor, true, plugin.getPerformance(EventType.VELOCITY));
        registerListener(Event.Type.BLOCK_PLACE, Priority.Monitor, true, plugin.getPerformance(EventType.BLOCKPLACE));
        registerListener(Event.Type.PLAYER_TELEPORT, Priority.Highest, false, null);
    }

    @Override
    protected void handleBlockPlaceEvent(final BlockPlaceEvent event, final Priority priority) {

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

    @Override
    protected void handlePlayerTeleportEvent(final PlayerTeleportEvent event, final Priority priority) {

        // No typo here, I really want to only handle cancelled events
        if(!event.isCancelled())
            return;

        NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final MovingData data = MovingCheck.getData(player.getDataStore());

        if(data.teleportTo.isSet() && data.teleportTo.equals(event.getTo())) {
            event.setCancelled(false);
        }
        return;

    }

    @Override
    protected void handlePlayerMoveEvent(final PlayerMoveEvent event, final Priority priority) {

        // Get the world-specific configuration that applies here
        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());

        // Not interested at all in players in vehicles or dead
        if(event.getPlayer().isInsideVehicle() || player.isDead()) {
            return;
        }

        final CCMoving cc = MovingCheck.getConfig(player.getConfigurationStore());

        if(!cc.check || player.hasPermission(Permissions.MOVING)) {
            return;
        }

        final MovingData data = MovingCheck.getData(player.getDataStore());

        /******** DO GENERAL DATA MODIFICATIONS ONCE FOR EACH EVENT *****/
        if(data.horizVelocityCounter > 0) {
            data.horizVelocityCounter--;
        } else {
            data.horizFreedom *= 0.90;
        }

        if(data.vertVelocity <= 0.1) {
            data.vertVelocityCounter--;
        }
        if(data.vertVelocityCounter > 0) {
            
            data.vertFreedom += data.vertVelocity;
            data.vertVelocity *= 0.90;
        } else {
            // Counter has run out, now reduce the vert freedom over time
            data.vertFreedom *= 0.90;
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

    @Override
    protected void handlePlayerVelocityEvent(final PlayerVelocityEvent event, final Priority priority) {

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

        CCMoving m = MovingCheck.getConfig(cc);

        if(m.check) {
            if(m.runflyCheck) {

                if(!m.allowFlying) {
                    s.add("moving.runfly");
                    if(m.swimmingCheck)
                        s.add("moving.swimming");
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
