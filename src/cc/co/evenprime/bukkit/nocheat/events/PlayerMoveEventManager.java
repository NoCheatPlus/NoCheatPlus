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
import cc.co.evenprime.bukkit.nocheat.checks.moving.RunFlyCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

/**
 * The only place that listens to and modifies player_move events if necessary
 * 
 * Get the event, decide which checks should work on it and in what order,
 * evaluate the check results and decide what to
 * 
 */
public class PlayerMoveEventManager extends PlayerListener implements EventManager {

    private final NoCheat     plugin;
    private final RunFlyCheck movingCheck;

    private final Performance movePerformance;
    private final Performance velocityPerformance;

    public PlayerMoveEventManager(NoCheat plugin) {

        this.plugin = plugin;
        this.movingCheck = new RunFlyCheck(plugin);

        this.movePerformance = plugin.getPerformance(Type.MOVING);
        this.velocityPerformance = plugin.getPerformance(Type.VELOCITY);

        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Lowest, plugin);
        pm.registerEvent(Event.Type.PLAYER_VELOCITY, this, Priority.Monitor, plugin);
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {

        // Cancelled events are ignored
        if(event.isCancelled())
            return;

        // Performance counter setup
        long nanoTimeStart = 0;
        final boolean performanceCheck = movePerformance.isEnabled();

        if(performanceCheck)
            nanoTimeStart = System.nanoTime();

        // Get the world-specific configuration that applies here
        final Player player = event.getPlayer();
        final ConfigurationCache cc = plugin.getConfig(player);

        // Find out if checks need to be done for that player
        if(cc.moving.check && !player.hasPermission(Permissions.MOVE)) {

            // Get some data that's needed from this event, to avoid passing the
            // event itself on to the checks (and risk to
            // accidentally modifying the event there)
            final Location from = event.getFrom();
            final Location to = event.getTo();

            // This variable will have the modified data of the event (new
            // "to"-location)
            Location newTo = null;

            // Currently only one check here.
            newTo = movingCheck.check(player, from, to, cc);

            // Did the check(s) decide we need a new "to"-location?
            if(newTo != null) {
                // Compose a new location based on coordinates of "newTo" and
                // viewing direction of "event.getTo()"
                Location l = new Location(newTo.getWorld(), newTo.getX(), newTo.getY(), newTo.getZ(), to.getYaw(), to.getPitch());
                event.setTo(l);

                // Get the player-specific stored data that applies here
                final BaseData data = plugin.getData(player);

                data.moving.teleportTo = l;
            }
        }

        // store performance time
        if(performanceCheck)
            movePerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    @Override
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        if(event.isCancelled())
            return;

        // Performance counter setup
        long nanoTimeStart = 0;
        final boolean performanceCheck = velocityPerformance.isEnabled();

        if(performanceCheck)
            nanoTimeStart = System.nanoTime();

        Player player = event.getPlayer();

        BaseData data = plugin.getData(player);

        Vector v = event.getVelocity();

        double newVal = v.getY();
        if(newVal >= 0.0D) {
            data.moving.vertVelocity += newVal;
            data.moving.vertFreedom += data.moving.vertVelocity;
        }

        data.moving.vertVelocityCounter = 50;

        newVal = Math.sqrt(Math.pow(v.getX(), 2) + Math.pow(v.getZ(), 2));
        if(newVal > 0.0D) {
            data.moving.horizFreedom += newVal;
            data.moving.horizVelocityCounter = 30;
        }

        // store performance time
        if(performanceCheck)
            velocityPerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.moving.check && cc.moving.runflyCheck && !cc.moving.allowFlying)
            s.add("moving.runfly");
        if(cc.moving.check && cc.moving.runflyCheck && cc.moving.allowFlying)
            s.add("moving.flying");
        if(cc.moving.check && cc.moving.runflyCheck && !cc.moving.allowFlying && cc.moving.swimmingCheck)
            s.add("moving.swimming");
        if(cc.moving.check && cc.moving.runflyCheck && !cc.moving.allowFlying && cc.moving.sneakingCheck)
            s.add("moving.sneaking");
        if(cc.moving.check && cc.moving.runflyCheck && !cc.moving.allowFlying && cc.moving.nofallCheck)
            s.add("moving.nofall");
        if(cc.moving.check && cc.moving.morePacketsCheck)
            s.add("moving.morepackets");

        return s;
    }
}
