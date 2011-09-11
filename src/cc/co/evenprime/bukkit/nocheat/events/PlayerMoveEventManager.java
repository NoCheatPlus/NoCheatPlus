package cc.co.evenprime.bukkit.nocheat.events;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.Permissions;
import cc.co.evenprime.bukkit.nocheat.checks.moving.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.DataManager;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * The only place that listens to and modifies player_move events if necessary
 * 
 * Get the event, decide which checks should work on it and in what order,
 * evaluate the check results and decide what to
 * 
 * @author Evenprime
 * 
 */
public class PlayerMoveEventManager extends PlayerListener implements EventManager {

    private final MovingCheck          movingCheck;

    private final ConfigurationManager config;
    private final DataManager          data;

    public PlayerMoveEventManager(NoCheat plugin) {
        this.config = plugin.getConfigurationManager();
        this.data = plugin.getDataManager();

        this.movingCheck = new MovingCheck(plugin);

        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Lowest, plugin);
        pm.registerEvent(Event.Type.PLAYER_VELOCITY, this, Priority.Monitor, plugin);
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {

        // Cancelled events are ignored
        if(event.isCancelled())
            return;

        // Get the world-specific configuration that applies here
        final Player player = event.getPlayer();
        final ConfigurationCache cc = config.getConfigurationCacheForWorld(player.getWorld().getName());

        // Find out if checks need to be done for that player
        if(cc.moving.check && !player.hasPermission(Permissions.MOVE)) {

            // Get the player-specific stored data that applies here
            final MovingData data = this.data.getMovingData(player);

            // Get some data that's needed from this event, to avoid passing the
            // event itself on to the checks (and risk to
            // accidentally modifying the event there)
            final Location from = event.getFrom();
            final Location to = event.getTo();

            // This variable will have the modified data of the event (new
            // "to"-location)
            Location newTo = null;

            // Currently only one check here.
            newTo = movingCheck.check(player, from, to, data, cc);

            // Did the check(s) decide we need a new "to"-location?
            if(newTo != null) {
                // Compose a new location based on coordinates of "newTo" and
                // viewing direction of "event.getTo()"
                Location l = new Location(newTo.getWorld(), newTo.getX(), newTo.getY(), newTo.getZ(), to.getYaw(), to.getPitch());
                event.setTo(l);
                data.teleportTo = l;
            }
        }

        // Log some performance data
        // log.logToConsole(LogLevel.LOW, "Time: " + (System.nanoTime() -
        // nanoTime));
    }

    @Override
    public void onPlayerVelocity(PlayerVelocityEvent event) {

        if(!event.isCancelled()) {
            Player player = event.getPlayer();

            MovingData mdata = data.getMovingData(player);

            Vector v = event.getVelocity();

            double newVal = v.getY();
            if(newVal >= 0.0D) {
                mdata.vertVelocity += newVal;
                mdata.vertFreedom += mdata.vertVelocity;
                mdata.vertVelocityCounter = 50;
            }

            newVal = Math.sqrt(Math.pow(v.getX(), 2) + Math.pow(v.getZ(), 2));
            if(newVal > 0.0D) {
                mdata.horizFreedom += newVal;
                mdata.horizVelocityCounter = 30;
            }
        }
    }

    @Override
    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.moving.check && cc.moving.flyingCheck)
            s.add("moving.flying");
        if(cc.moving.check && cc.moving.runningCheck)
            s.add("moving.running");
        if(cc.moving.check && cc.moving.runningCheck && cc.moving.swimmingCheck)
            s.add("moving.swimming");
        if(cc.moving.check && cc.moving.runningCheck && cc.moving.sneakingCheck)
            s.add("moving.sneaking");
        if(cc.moving.check && cc.moving.morePacketsCheck)
            s.add("moving.morepackets");
        if(cc.moving.check && cc.moving.noclipCheck)
            s.add("moving.noclip");

        return s;
    }

    @Override
    public List<String> getInactiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(!(cc.moving.check && cc.moving.flyingCheck))
            s.add("moving.flying");
        if(!(cc.moving.check && cc.moving.runningCheck))
            s.add("moving.running");
        if(!(cc.moving.check && cc.moving.runningCheck && cc.moving.swimmingCheck))
            s.add("moving.running.swimming");
        if(!(cc.moving.check && cc.moving.runningCheck && cc.moving.sneakingCheck))
            s.add("moving.running.sneaking");
        if(!(cc.moving.check && cc.moving.morePacketsCheck))
            s.add("moving.morepackets");
        if(!(cc.moving.check && cc.moving.noclipCheck))
            s.add("moving.noclip");

        return s;
    }
}
