package cc.co.evenprime.bukkit.nocheat.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;

public abstract class EventManager {

    protected NoCheat plugin;

    private static class BlockL extends BlockListener {

        private final EventManager m;
        private final Priority     priority;
        private final boolean      ignoreCancelledEvents;

        public BlockL(EventManager m, Priority priority, boolean ignoreCancelled) {
            this.m = m;
            this.priority = priority;
            this.ignoreCancelledEvents = ignoreCancelled;
        }

        @Override
        public void onBlockPlace(BlockPlaceEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handleBlockPlaceEvent(event, priority);
        }

        @Override
        public void onBlockBreak(BlockBreakEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handleBlockBreakEvent(event, priority);
        }

        @Override
        public void onBlockDamage(BlockDamageEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handleBlockDamageEvent(event, priority);
        }
    }

    private static class PlayerL extends PlayerListener {

        private final EventManager m;
        private final Priority     priority;
        private final boolean      ignoreCancelledEvents;

        public PlayerL(EventManager m, Priority priority, boolean ignoreCancelled) {
            this.m = m;
            this.priority = priority;
            this.ignoreCancelledEvents = ignoreCancelled;
        }

        @Override
        public void onPlayerChat(PlayerChatEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handlePlayerChatEvent(event, priority);
        }

        @Override
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handlePlayerCommandPreprocessEvent(event, priority);
        }

        @Override
        public void onPlayerMove(PlayerMoveEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handlePlayerMoveEvent(event, priority);
        }

        @Override
        public void onPlayerVelocity(PlayerVelocityEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handlePlayerVelocityEvent(event, priority);
        }

        @Override
        public void onPlayerRespawn(PlayerRespawnEvent event) {
            // if(ignoreCancelledEvents && event.isCancelled()) return;
            m.handlePlayerRespawnEvent(event, priority);
        }

        @Override
        public void onPlayerPortal(PlayerPortalEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handlePlayerPortalEvent(event, priority);
        }

        @Override
        public void onPlayerTeleport(PlayerTeleportEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handlePlayerTeleportEvent(event, priority);
        }

        @Override
        public void onPlayerAnimation(PlayerAnimationEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handlePlayerAnimationEvent(event, priority);
        }
    }

    private static class EntityL extends EntityListener {

        private final EventManager m;
        private final Priority     priority;
        private final boolean      ignoreCancelledEvents;

        public EntityL(EventManager m, Priority priority, boolean ignoreCancelled) {
            this.m = m;
            this.priority = priority;
            this.ignoreCancelledEvents = ignoreCancelled;
        }

        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;
            m.handleEntityDamageEvent(event, priority);
        }
    }

    public EventManager(NoCheat plugin) {
        this.plugin = plugin;
    }

    protected void registerListener(Type type, Priority priority, boolean ignoreCancelled) {
        switch (type.getCategory()) {
        case BLOCK:
            Bukkit.getServer().getPluginManager().registerEvent(type, new BlockL(this, priority, ignoreCancelled), priority, plugin);
            break;
        case PLAYER:
            Bukkit.getServer().getPluginManager().registerEvent(type, new PlayerL(this, priority, ignoreCancelled), priority, plugin);
            break;
        case ENTITY:
            Bukkit.getServer().getPluginManager().registerEvent(type, new EntityL(this, priority, ignoreCancelled), priority, plugin);
            break;
        default:
            System.out.println("Can't register a listener for " + type);
        }
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        return Collections.emptyList();
    }

    protected void handleEvent(Event event, Priority priority) {
        System.out.println("Handling of event " + event.getType() + " not implemented for " + this);
    }

    protected void handleBlockPlaceEvent(BlockPlaceEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handleBlockBreakEvent(BlockBreakEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handleBlockDamageEvent(BlockDamageEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handleEntityDamageEvent(EntityDamageEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerChatEvent(PlayerChatEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerMoveEvent(PlayerMoveEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerVelocityEvent(PlayerVelocityEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerRespawnEvent(PlayerRespawnEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerPortalEvent(PlayerPortalEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerTeleportEvent(PlayerTeleportEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerAnimationEvent(PlayerAnimationEvent event, Priority priority) {
        handleEvent(event, priority);
    }
}
