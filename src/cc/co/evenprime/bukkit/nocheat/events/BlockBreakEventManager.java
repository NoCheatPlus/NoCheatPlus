package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.BlockBreakCheck;
import cc.co.evenprime.bukkit.nocheat.checks.blockbreak.DirectionCheck;
import cc.co.evenprime.bukkit.nocheat.checks.blockbreak.NoswingCheck;
import cc.co.evenprime.bukkit.nocheat.checks.blockbreak.ReachCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCBlockBreak;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

/**
 * Central location to listen to player-interact events and dispatch them to
 * relevant checks
 * 
 */
public class BlockBreakEventManager extends EventManager {

    private final List<BlockBreakCheck> checks;

    public BlockBreakEventManager(NoCheat plugin) {

        super(plugin);

        // Three checks exist for this event type
        this.checks = new ArrayList<BlockBreakCheck>(3);
        this.checks.add(new NoswingCheck(plugin));
        this.checks.add(new ReachCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));

        registerListener(Event.Type.BLOCK_BREAK, Priority.Lowest, true, plugin.getPerformance(Type.BLOCKBREAK));
        registerListener(Event.Type.BLOCK_DAMAGE, Priority.Monitor, true, plugin.getPerformance(Type.BLOCKDAMAGE));
        registerListener(Event.Type.PLAYER_ANIMATION, Priority.Monitor, false, null);
    }

    @Override
    protected void handleBlockBreakEvent(final BlockBreakEvent event, final Priority priority) {

        boolean cancelled = false;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final CCBlockBreak cc = player.getConfiguration().blockbreak;

        if(!cc.check || player.hasPermission(Permissions.BLOCKBREAK)) {
            return;
        }

        final BlockBreakData data = player.getData().blockbreak;

        data.brokenBlockLocation.set(event.getBlock());

        for(BlockBreakCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                cancelled = check.check(player, data, cc);
            }
        }

        if(cancelled)
            event.setCancelled(cancelled);
    }

    @Override
    protected void handleBlockDamageEvent(final BlockDamageEvent event, final Priority priority) {

        // Only interested in insta-break events here
        if(!event.getInstaBreak())
            return;

        // Get the player-specific stored data that applies here
        final BlockBreakData data = plugin.getPlayer(event.getPlayer()).getData().blockbreak;

        // Remember this location. We ignore block breaks in the block-break
        // direction check that are insta-breaks
        data.instaBrokenBlockLocation.set(event.getBlock());
    }

    @Override
    protected void handlePlayerAnimationEvent(final PlayerAnimationEvent event, final Priority priority) {
        plugin.getPlayer(event.getPlayer()).getData().blockbreak.armswung = true;
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.blockbreak.check && cc.blockbreak.directionCheck)
            s.add("blockbreak.direction");
        if(cc.blockbreak.check && cc.blockbreak.reachCheck)
            s.add("blockbreak.reach");
        if(cc.blockbreak.check && cc.blockbreak.noswingCheck)
            s.add("blockbreak.noswing");

        return s;
    }
}
