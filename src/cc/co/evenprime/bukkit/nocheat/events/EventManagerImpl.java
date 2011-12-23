package cc.co.evenprime.bukkit.nocheat.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;

/**
 * To make the actual event management easier, this class will handle
 * most of the repetitive tasks, allow to know the priority of events,
 * and already filter out events that are cancelled and measure the
 * time it takes to handle the event.
 * 
 */
public abstract class EventManagerImpl implements EventManager {

    protected final NoCheat plugin;

    private static class BlockL extends BlockListener {

        private final EventManagerImpl m;
        private final Priority         priority;
        private final boolean          ignoreCancelledEvents;
        private final Performance      measureTime;

        public BlockL(EventManagerImpl m, Priority priority, boolean ignoreCancelled, Performance measureTime) {
            this.m = m;
            this.priority = priority;
            this.ignoreCancelledEvents = ignoreCancelled;
            this.measureTime = measureTime;
        }

        @Override
        public void onBlockPlace(final BlockPlaceEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handleBlockPlaceEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handleBlockPlaceEvent(event, priority);
            }
        }

        @Override
        public void onBlockBreak(final BlockBreakEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handleBlockBreakEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handleBlockBreakEvent(event, priority);
            }
        }

        @Override
        public void onBlockDamage(final BlockDamageEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handleBlockDamageEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handleBlockDamageEvent(event, priority);
            }
        }
    }

    private static class PlayerL extends PlayerListener {

        private final EventManagerImpl m;
        private final Priority         priority;
        private final boolean          ignoreCancelledEvents;
        private final Performance      measureTime;

        public PlayerL(EventManagerImpl m, Priority priority, boolean ignoreCancelled, Performance measureTime) {
            this.m = m;
            this.priority = priority;
            this.ignoreCancelledEvents = ignoreCancelled;
            this.measureTime = measureTime;
        }

        @Override
        public void onPlayerChat(final PlayerChatEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handlePlayerChatEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handlePlayerChatEvent(event, priority);
            }
        }

        @Override
        public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handlePlayerCommandPreprocessEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handlePlayerCommandPreprocessEvent(event, priority);
            }
        }

        @Override
        public void onPlayerMove(final PlayerMoveEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handlePlayerMoveEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handlePlayerMoveEvent(event, priority);
            }
        }

        @Override
        public void onPlayerDropItem(final PlayerDropItemEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handlePlayerDropItemEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handlePlayerDropItemEvent(event, priority);
            }
        }

        @Override
        public void onPlayerVelocity(final PlayerVelocityEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handlePlayerVelocityEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handlePlayerVelocityEvent(event, priority);
            }
        }

        @Override
        public void onPlayerRespawn(final PlayerRespawnEvent event) {
            // Can't be cancelled
            // if(ignoreCancelledEvents && event.isCancelled())
            // return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handlePlayerRespawnEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handlePlayerRespawnEvent(event, priority);
            }
        }

        @Override
        public void onPlayerPortal(final PlayerPortalEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handlePlayerPortalEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handlePlayerPortalEvent(event, priority);
            }
        }

        @Override
        public void onPlayerTeleport(final PlayerTeleportEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handlePlayerTeleportEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handlePlayerTeleportEvent(event, priority);
            }
        }

        @Override
        public void onPlayerAnimation(final PlayerAnimationEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handlePlayerAnimationEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handlePlayerAnimationEvent(event, priority);
            }
        }

        @Override
        public void onPlayerToggleSprint(final PlayerToggleSprintEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            if(measureTime != null && measureTime.isEnabled()) {
                final long startTime = System.nanoTime();
                m.handlePlayerToggleSprintEvent(event, priority);
                measureTime.addTime(System.nanoTime() - startTime);
            } else {
                m.handlePlayerToggleSprintEvent(event, priority);
            }
        }
    }

    private static class EntityL extends EntityListener {

        private final EventManagerImpl m;
        private final Priority         priority;
        private final boolean          ignoreCancelledEvents;
        private final Performance      measureTime;

        public EntityL(EventManagerImpl m, Priority priority, boolean ignoreCancelled, Performance measureTime) {
            this.m = m;
            this.priority = priority;
            this.ignoreCancelledEvents = ignoreCancelled;
            this.measureTime = measureTime;
        }

        @Override
        public void onEntityDamage(final EntityDamageEvent event) {
            if(ignoreCancelledEvents && event.isCancelled())
                return;

            /**
             * Some additional limitations - only interested in
             * EntityDamageByEntity
             * 
             */
            if(!(event instanceof EntityDamageByEntityEvent))
                return;

            // Only handle if attack done by a projectile
            if(event.getCause() == DamageCause.PROJECTILE) {

                final EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;

                // And only if the shooter was a player
                if(!((event2.getDamager() instanceof Projectile) && ((Projectile) event2.getDamager()).getShooter() instanceof Player)) {
                    return;
                }

                /** Only now measure time and dispatch event */
                if(measureTime != null && measureTime.isEnabled()) {
                    final long startTime = System.nanoTime();
                    m.handleProjectileDamageByPlayerEvent(event2, priority);
                    measureTime.addTime(System.nanoTime() - startTime);
                } else {
                    m.handleProjectileDamageByPlayerEvent(event2, priority);
                }
            }
            // Only handle if attack done directly
            else if(event.getCause() == DamageCause.ENTITY_ATTACK) {

                final EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;

                // And only if done by a player
                if(!(event2.getDamager() instanceof Player)) {
                    return;
                }

                /** Only now measure time and dispatch event */
                if(measureTime != null && measureTime.isEnabled()) {
                    final long startTime = System.nanoTime();
                    m.handleEntityAttackDamageByPlayerEvent(event2, priority);
                    measureTime.addTime(System.nanoTime() - startTime);
                } else {
                    m.handleEntityAttackDamageByPlayerEvent(event2, priority);
                }
            }
            // Only handle if attack done by "custom"
            else if(event.getCause() == DamageCause.CUSTOM) {

                final EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;

                // And only if done by a player
                if(!(event2.getDamager() instanceof Player)) {
                    return;
                }

                /** Only now measure time and dispatch event */
                if(measureTime != null && measureTime.isEnabled()) {
                    final long startTime = System.nanoTime();
                    m.handleCustomDamageByPlayerEvent(event2, priority);
                    measureTime.addTime(System.nanoTime() - startTime);
                } else {
                    m.handleCustomDamageByPlayerEvent(event2, priority);
                }
            }
        }
    }

    public EventManagerImpl(NoCheat plugin) {
        this.plugin = plugin;
    }

    /**
     * Use this to register listeners with CraftBukkit
     * 
     * @param type
     * @param priority
     * @param ignoreCancelled
     * @param performance
     */
    protected void registerListener(Type type, Priority priority, boolean ignoreCancelled, Performance performance) {
        switch (type.getCategory()) {
        case BLOCK:
            Bukkit.getServer().getPluginManager().registerEvent(type, new BlockL(this, priority, ignoreCancelled, performance), priority, plugin);
            break;
        case PLAYER:
            Bukkit.getServer().getPluginManager().registerEvent(type, new PlayerL(this, priority, ignoreCancelled, performance), priority, plugin);
            break;
        case LIVING_ENTITY:
        case ENTITY:
            Bukkit.getServer().getPluginManager().registerEvent(type, new EntityL(this, priority, ignoreCancelled, performance), priority, plugin);
            break;
        default:
            System.out.println("Can't register a listener for " + type);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cc.co.evenprime.bukkit.nocheat.events.EventManager#getActiveChecks(cc
     * .co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache)
     */
    @Override
    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        return Collections.emptyList();
    }

    protected void handleEvent(final Event event, final Priority priority) {
        System.out.println("Handling of event " + event.getType() + " not implemented for " + this);
    }

    /***
     * OVERRIDE THESE IN THE SUBCLASSES, IF YOU LISTEN TO THE RELEVANT EVENT(S)
     */

    protected void handleBlockPlaceEvent(final BlockPlaceEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handleBlockBreakEvent(final BlockBreakEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handleBlockDamageEvent(final BlockDamageEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerCommandPreprocessEvent(final PlayerCommandPreprocessEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerChatEvent(final PlayerChatEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerMoveEvent(final PlayerMoveEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerVelocityEvent(final PlayerVelocityEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerRespawnEvent(final PlayerRespawnEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerPortalEvent(final PlayerPortalEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerTeleportEvent(final PlayerTeleportEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerAnimationEvent(final PlayerAnimationEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handleProjectileDamageByPlayerEvent(final EntityDamageByEntityEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handleEntityAttackDamageByPlayerEvent(final EntityDamageByEntityEvent event, final Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerToggleSprintEvent(PlayerToggleSprintEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handleCustomDamageByPlayerEvent(EntityDamageByEntityEvent event, Priority priority) {
        handleEvent(event, priority);
    }

    protected void handlePlayerDropItemEvent(PlayerDropItemEvent event, Priority priority) {
        handleEvent(event, priority);
    }
}
