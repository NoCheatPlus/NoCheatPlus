package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

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
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.EventType;
import cc.co.evenprime.bukkit.nocheat.events.EventManagerImpl;

/**
 * Central location to listen to player-interact events and dispatch them to
 * relevant checks
 * 
 */
public class BlockBreakEventManager extends EventManagerImpl {

    private final List<BlockBreakCheck> checks;

    public BlockBreakEventManager(NoCheat plugin) {

        super(plugin);

        // Three checks exist for this event type
        this.checks = new ArrayList<BlockBreakCheck>(3);
        this.checks.add(new NoswingCheck(plugin));
        this.checks.add(new ReachCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));

        registerListener(Event.Type.BLOCK_BREAK, Priority.Lowest, true, plugin.getPerformance(EventType.BLOCKBREAK));
        registerListener(Event.Type.BLOCK_DAMAGE, Priority.Monitor, true, plugin.getPerformance(EventType.BLOCKDAMAGE));
        registerListener(Event.Type.PLAYER_ANIMATION, Priority.Monitor, false, null);
    }

    @Override
    protected void handleBlockBreakEvent(final BlockBreakEvent event, final Priority priority) {

        boolean cancelled = false;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final CCBlockBreak cc = BlockBreakCheck.getConfig(player.getConfigurationStore());

        if(!cc.check || player.hasPermission(Permissions.BLOCKBREAK)) {
            return;
        }

        final BlockBreakData data = BlockBreakCheck.getData(player.getDataStore());

        data.brokenBlockLocation.set(event.getBlock());

        // Only if the block got damaged before, do the check(s)
        if(!data.brokenBlockLocation.equals(data.lastDamagedBlock)) {
            // Something caused a blockbreak event that's not from the player
            // Don't check it at all
            data.lastDamagedBlock.reset();
            return;
        }

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

        NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        BlockBreakData data = BlockBreakCheck.getData(player.getDataStore());

        // Only interested in insta-break events here
        if(event.getInstaBreak()) {
            // Remember this location. We ignore block breaks in the block-break
            // direction check that are insta-breaks
            data.instaBrokenBlockLocation.set(event.getBlock());
        }

        // Remember this location. Only blockbreakevents for this specific block
        // will be handled at all
        data.lastDamagedBlock.set(event.getBlock());

    }

    @Override
    protected void handlePlayerAnimationEvent(final PlayerAnimationEvent event, final Priority priority) {
        BlockBreakCheck.getData(plugin.getPlayer(event.getPlayer()).getDataStore()).armswung = true;
    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        CCBlockBreak bb = BlockBreakCheck.getConfig(cc);

        if(bb.check && bb.directionCheck)
            s.add("blockbreak.direction");
        if(bb.check && bb.reachCheck)
            s.add("blockbreak.reach");
        if(bb.check && bb.noswingCheck)
            s.add("blockbreak.noswing");

        return s;
    }
}
