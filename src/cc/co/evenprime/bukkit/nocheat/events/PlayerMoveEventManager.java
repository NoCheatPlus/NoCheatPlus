package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.checks.moving.MorePacketsCheck;
import cc.co.evenprime.bukkit.nocheat.checks.moving.RunflyCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCMoving;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;
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

    private final NoCheat           plugin;
    private final List<MovingCheck> checks;

    private final Performance       movePerformance;
    private final Performance       velocityPerformance;

    public PlayerMoveEventManager(final NoCheat plugin) {

        this.plugin = plugin;
        this.checks = new ArrayList<MovingCheck>(5);

        checks.add(new RunflyCheck(plugin));
        checks.add(new MorePacketsCheck(plugin));

        this.movePerformance = plugin.getPerformance(Type.MOVING);
        this.velocityPerformance = plugin.getPerformance(Type.VELOCITY);

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Lowest, plugin);
        pm.registerEvent(Event.Type.PLAYER_VELOCITY, this, Priority.Monitor, plugin);

        // This is part of a workaround for the moving check
        pm.registerEvent(Event.Type.BLOCK_PLACE, new BlockListener() {

            @Override
            public void onBlockPlace(BlockPlaceEvent event) {
                if(event.isCancelled())
                    return;

                final NoCheatPlayer player = plugin.getPlayer(event.getPlayer().getName());
                // Get the player-specific stored data that applies here
                blockPlaced(player, event.getBlockPlaced());

            }
        }, Priority.Monitor, plugin);
    }

    @Override
    public void onPlayerMove(final PlayerMoveEvent event) {

        // Cancelled events are ignored
        if(event.isCancelled())
            return;

        // Performance counter setup
        long nanoTimeStart = 0;
        final boolean performanceCheck = movePerformance.isEnabled();

        if(performanceCheck)
            nanoTimeStart = System.nanoTime();

        handleEvent(event);

        // store performance time
        if(performanceCheck)
            movePerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    public void handleEvent(PlayerMoveEvent event) {

        // Get the world-specific configuration that applies here
        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer().getName());
        final CCMoving cc = player.getConfiguration().moving;

        if(!cc.check || player.hasPermission(Permissions.MOVING)) {
            return;
        }

        final MovingData data = player.getData().moving;

        /******** DO GENERAL DATA MODIFICATIONS ONCE FOR EACH EVENT *****/
        if(data.horizVelocityCounter > 0) {
            data.horizVelocityCounter--;
        } else {
            data.horizFreedom *= 0.90;
        }

        if(data.vertVelocityCounter > 0) {
            data.vertVelocityCounter--;
            data.vertFreedom += data.vertVelocity;
            data.vertVelocity *= 0.90;
        } else {
            data.vertFreedom = 0;
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
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        if(event.isCancelled())
            return;

        // Performance counter setup
        long nanoTimeStart = 0;
        final boolean performanceCheck = velocityPerformance.isEnabled();

        if(performanceCheck)
            nanoTimeStart = System.nanoTime();

        MovingData data = plugin.getPlayer(event.getPlayer().getName()).getData().moving;

        Vector v = event.getVelocity();

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

        // store performance time
        if(performanceCheck)
            velocityPerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    /**
     * This is a workaround for people placing blocks below them causing false
     * positives with the move check(s).
     */
    public void blockPlaced(final NoCheatPlayer player, Block blockPlaced) {

        BaseData data = player.getData();

        if(blockPlaced == null || !data.moving.runflySetBackPoint.isSet()) {
            return;
        }

        SimpleLocation lblock = new SimpleLocation();
        lblock.set(blockPlaced);
        SimpleLocation lplayer = new SimpleLocation();
        lplayer.setLocation(player.getPlayer().getLocation());

        if(Math.abs(lplayer.x - lblock.x) <= 1 && Math.abs(lplayer.z - lblock.z) <= 1 && lplayer.y - lblock.y >= 0 && lplayer.y - lblock.y <= 2) {

            int type = CheckUtil.getType(blockPlaced.getTypeId());
            if(CheckUtil.isSolid(type) || CheckUtil.isLiquid(type)) {
                if(lblock.y + 1 >= data.moving.runflySetBackPoint.y) {
                    data.moving.runflySetBackPoint.y = (lblock.y + 1);
                    data.moving.jumpPhase = 0;
                }
            }
        }
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.moving.check) {
            if(cc.moving.runflyCheck) {

                if(!cc.moving.allowFlying) {
                    s.add("moving.runfly");
                    if(cc.moving.swimmingCheck)
                        s.add("moving.swimming");
                    if(cc.moving.sneakingCheck)
                        s.add("moving.sneaking");
                    if(cc.moving.nofallCheck)
                        s.add("moving.nofall");
                } else
                    s.add("moving.flying");

            }
            if(cc.moving.morePacketsCheck)
                s.add("moving.morepackets");
        }

        return s;
    }
}
