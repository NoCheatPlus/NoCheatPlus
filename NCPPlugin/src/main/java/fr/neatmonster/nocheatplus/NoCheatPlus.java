/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakListener;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractListener;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceListener;
import fr.neatmonster.nocheatplus.checks.chat.ChatListener;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedListener;
import fr.neatmonster.nocheatplus.checks.fight.FightListener;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryListener;
import fr.neatmonster.nocheatplus.checks.moving.MovingListener;
import fr.neatmonster.nocheatplus.checks.moving.location.tracking.LocationTrace.TraceEntryPool;
import fr.neatmonster.nocheatplus.checks.moving.util.AuxMoving;
import fr.neatmonster.nocheatplus.checks.workaround.WRPT;
import fr.neatmonster.nocheatplus.clients.ModUtil;
import fr.neatmonster.nocheatplus.command.NoCheatPlusCommand;
import fr.neatmonster.nocheatplus.command.admin.VersionCommand;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeListener;
import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeTracker;
import fr.neatmonster.nocheatplus.compat.meta.BridgeCrossPlugin;
import fr.neatmonster.nocheatplus.compat.registry.AttributeAccessFactory;
import fr.neatmonster.nocheatplus.compat.registry.DefaultComponentFactory;
import fr.neatmonster.nocheatplus.compat.registry.EntityAccessFactory;
import fr.neatmonster.nocheatplus.compat.registry.MCAccessConfig;
import fr.neatmonster.nocheatplus.compat.registry.MCAccessFactory;
import fr.neatmonster.nocheatplus.compat.versions.Bugs;
import fr.neatmonster.nocheatplus.compat.versions.BukkitVersion;
import fr.neatmonster.nocheatplus.compat.versions.GenericVersion;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.components.registry.ComponentRegistry;
import fr.neatmonster.nocheatplus.components.registry.DefaultGenericInstanceRegistry;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.components.registry.feature.ComponentWithName;
import fr.neatmonster.nocheatplus.components.registry.feature.ConsistencyChecker;
import fr.neatmonster.nocheatplus.components.registry.feature.IDisableListener;
import fr.neatmonster.nocheatplus.components.registry.feature.IHoldSubComponents;
import fr.neatmonster.nocheatplus.components.registry.feature.INeedConfig;
import fr.neatmonster.nocheatplus.components.registry.feature.INotifyReload;
import fr.neatmonster.nocheatplus.components.registry.feature.IPostRegisterRunnable;
import fr.neatmonster.nocheatplus.components.registry.feature.IRegisterAsGenericInstance;
import fr.neatmonster.nocheatplus.components.registry.feature.JoinLeaveListener;
import fr.neatmonster.nocheatplus.components.registry.feature.NCPListener;
import fr.neatmonster.nocheatplus.components.registry.feature.NameSetPermState;
import fr.neatmonster.nocheatplus.components.registry.feature.PermStateReceiver;
import fr.neatmonster.nocheatplus.components.registry.feature.TickListener;
import fr.neatmonster.nocheatplus.components.registry.order.SetupOrder;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.event.IHaveMethodOrder;
import fr.neatmonster.nocheatplus.event.ListenerManager;
import fr.neatmonster.nocheatplus.hooks.ExemptionSettings;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.hooks.allviolations.AllViolationsConfig;
import fr.neatmonster.nocheatplus.hooks.allviolations.AllViolationsHook;
import fr.neatmonster.nocheatplus.logging.BukkitLogManager;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.StreamID;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.logging.details.IGetStreamId;
import fr.neatmonster.nocheatplus.permissions.PermissionUtil;
import fr.neatmonster.nocheatplus.permissions.PermissionUtil.CommandProtectionEntry;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.PlayerData;
import fr.neatmonster.nocheatplus.players.PlayerMessageSender;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.updates.Updates;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import fr.neatmonster.nocheatplus.utilities.OnDemandTickListener;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

/**
 * This is the main class of NoCheatPlus. The commands, events listeners and tasks are registered here.
 */
public class NoCheatPlus extends JavaPlugin implements NoCheatPlusAPI {

    private static final String MSG_NOTIFY_OFF = ChatColor.RED + "NCP: " + ChatColor.WHITE + "Notifications are turned " + ChatColor.RED + "OFF" + ChatColor.WHITE + ".";

    // Static API

    /**
     * Convenience method.
     * @deprecated Use fr.neatmonster.nocheatplus.utilities.NCPAPIProvider.getNoCheatPlusAPI() instead, this method might get removed.
     * @return
     */
    public static NoCheatPlusAPI getAPI() {
        return NCPAPIProvider.getNoCheatPlusAPI();
    }

    // Not static.

    /** Central logging access point. */
    private BukkitLogManager logManager = null; // Not final, but intended to stay, once set [change to init=syso?].

    /** Names of players with a certain permission. */
    protected final NameSetPermState nameSetPerms = new NameSetPermState(Permissions.NOTIFY);

    /** Lower case player name to milliseconds point of time of release */
    private final Map<String, Long> denyLoginNames = Collections.synchronizedMap(new HashMap<String, Long>());

    /** Configuration problems (likely put to ConfigManager later). */
    protected String configProblems = null;

    //    /** Is a new update available? */
    //    private boolean              updateAvailable = false;

    /** Player data future stuff. */
    protected final DataManager dataMan = new DataManager();

    private int dataManTaskId = -1;

    /**
     * Commands that were changed for protecting them against tab complete or
     * use.
     */
    final LinkedList<CommandProtectionEntry> changedCommands = new LinkedList<CommandProtectionEntry>();

    private final ListenerManager listenerManager = new ListenerManager(this, false);

    private boolean manageListeners = true;

    protected boolean lateListenerRegistered = false;

    /** The event listeners. */
    private final List<Listener> listeners       = new ArrayList<Listener>();

    /** Components that need notification on reloading.
     * (Kept here, for if during runtime some might get added.)*/
    private final List<INotifyReload> notifyReload = new LinkedList<INotifyReload>();

    /** If to use subscriptions or not. */
    protected boolean useSubscriptions = false;

    /** Permission states stored on a per-world basis, updated with join/quit/kick.  */
    protected final List<PermStateReceiver> permStateReceivers = new ArrayList<PermStateReceiver>();

    /** Components that check consistency. */
    protected final List<ConsistencyChecker> consistencyCheckers = new ArrayList<ConsistencyChecker>();

    /** Index at which to continue. */
    protected int consistencyCheckerIndex = 0;

    protected int consistencyCheckerTaskId = -1;

    /** Listeners for players joining and leaving (monitor level) */
    protected final List<JoinLeaveListener> joinLeaveListeners = new ArrayList<JoinLeaveListener>();

    /** Sub component registries. */
    protected final List<ComponentRegistry<?>> subRegistries = new ArrayList<ComponentRegistry<?>>();

    /** Queued sub component holders, emptied on the next tick usually. */
    protected final List<IHoldSubComponents> subComponentholders = new ArrayList<IHoldSubComponents>(20);

    private final List<IDisableListener> disableListeners = new ArrayList<IDisableListener>();

    /** All registered components.  */
    protected Set<Object> allComponents = new LinkedHashSet<Object>(50);

    /** Feature tags by keys, for features that might not be available. */
    private final LinkedHashMap<String, LinkedHashSet<String>> featureTags = new LinkedHashMap<String, LinkedHashSet<String>>();

    /** Hook for logging all violations. */
    protected final AllViolationsHook allViolationsHook = new AllViolationsHook();

    /** Block change tracking (pistons, other). */
    private final BlockChangeTracker blockChangeTracker = new BlockChangeTracker();
    /** Listener for the BlockChangeTracker (register once, lazy). */
    private BlockChangeListener blockChangeListener = null;

    private final DefaultGenericInstanceRegistry genericInstanceRegistry = new DefaultGenericInstanceRegistry();

    /** Self-updating MCAccess reference. */
    protected final IGenericInstanceHandle<MCAccess> mcAccess = genericInstanceRegistry.getGenericInstanceHandle(MCAccess.class);

    /** Tick listener that is only needed sometimes (component registration). */
    protected final OnDemandTickListener onDemandTickListener = new OnDemandTickListener() {
        @Override
        public boolean delegateTick(final int tick, final long timeLast) {
            processQueuedSubComponentHolders();
            return false;
        }
    };

    private class PostEnableTask implements Runnable {

        private final NoCheatPlusCommand commandHandler;
        private final Player[] onlinePlayers;

        protected PostEnableTask(NoCheatPlusCommand commandHandler, Player[] onlinePlayers) {
            this.commandHandler = commandHandler;
            this.onlinePlayers = onlinePlayers;
        }

        @Override
        public void run() {
            postEnable(commandHandler, onlinePlayers);
        }

    }

    /** Access point for thread safe message queuing. */
    private final PlayerMessageSender playerMessageSender  = new PlayerMessageSender();

    private boolean clearExemptionsOnJoin = true;
    private boolean clearExemptionsOnLeave = true;

    private StreamID getRegistryStreamId() {
        // TODO: Select by config, or add Streams.REGISTRY for a new default.
        // For now prefer log file, unless extended status is set.
        return ConfigManager.getConfigFile().getBoolean(ConfPaths.LOGGING_EXTENDED_STATUS) ? Streams.STATUS : Streams.DEFAULT_FILE;
    }

    /**
     * Remove expired entries.
     */
    private void checkDenyLoginsNames() {
        final long ts = System.currentTimeMillis();
        final List<String> rem = new LinkedList<String>();
        synchronized (denyLoginNames) {
            for (final Entry<String, Long> entry : denyLoginNames.entrySet()) {
                if (entry.getValue().longValue() < ts)  rem.add(entry.getKey());
            }
            for (final String name : rem) {
                denyLoginNames.remove(name);
            }
        }
    }

    @Override
    public boolean allowLogin(String playerName) {
        playerName = playerName.trim().toLowerCase();
        final Long time = denyLoginNames.remove(playerName);
        if (time == null) return false;
        return System.currentTimeMillis() <= time;
    }

    @Override
    public int allowLoginAll() {
        int denied = 0;
        final long now = System.currentTimeMillis();
        for (final String playerName : denyLoginNames.keySet()) {
            final Long time = denyLoginNames.get(playerName);
            if (time != null && time > now) denied ++;
        }
        denyLoginNames.clear();
        return denied;
    }

    @Override
    public void denyLogin(String playerName, long duration) {
        final long ts = System.currentTimeMillis() + duration;
        playerName = playerName.trim().toLowerCase();
        synchronized (denyLoginNames) {
            final Long oldTs = denyLoginNames.get(playerName);
            if (oldTs != null && ts < oldTs.longValue()) return;
            denyLoginNames.put(playerName, ts);
            // TODO: later maybe save these ?
        }
        checkDenyLoginsNames();
    }

    @Override
    public boolean isLoginDenied(String playerName) {
        return isLoginDenied(playerName, System.currentTimeMillis());
    }

    @Override
    public String[] getLoginDeniedPlayers() {
        checkDenyLoginsNames();
        String[] kicked = new String[denyLoginNames.size()];
        denyLoginNames.keySet().toArray(kicked);
        return kicked;
    }

    @Override
    public boolean isLoginDenied(String playerName, long time) {
        playerName = playerName.trim().toLowerCase();
        final Long oldTs = denyLoginNames.get(playerName);
        if (oldTs == null) return false; 
        else return time < oldTs.longValue();
    }

    @Override
    public int sendAdminNotifyMessage(final String message) {
        if (useSubscriptions) {
            // TODO: Might respect console settings, or add extra config section (e.g. notifications).
            return sendAdminNotifyMessageSubscriptions(message);
        }
        else {
            return sendAdminNotifyMessageStored(message);
        }
    }

    private boolean hasTurnedOffNotifications(final String playerName) {
        final PlayerData data = DataManager.getPlayerData(playerName, false);
        return data != null && data.getNotifyOff();
    }

    /**
     * Send notification to players with stored notify-permission (world changes, login, permissions are not re-checked here). 
     * @param message
     * @return
     */
    public int sendAdminNotifyMessageStored(final String message) {
        final Set<String> names = nameSetPerms.getPlayers(Permissions.NOTIFY);
        if (names == null) return 0;
        int done = 0;
        for (final String name : names) {
            if (hasTurnedOffNotifications(name)) {
                // Has turned off notifications.
                continue;
            }
            final Player player = DataManager.getPlayerExact(name);
            if (player != null) {
                player.sendMessage(message);
                done ++;
            }
        }
        return done;
    }

    /**
     * Send notification to all CommandSenders found in permission subscriptions for the notify-permission as well as players that have stored permissions (those get re-checked here).
     * @param message
     * @return
     */
    public int sendAdminNotifyMessageSubscriptions(final String message) {
        final Set<Permissible> permissibles = Bukkit.getPluginManager().getPermissionSubscriptions(Permissions.NOTIFY);
        final Set<String> names = nameSetPerms.getPlayers(Permissions.NOTIFY);
        final Set<String> done = new HashSet<String>(permissibles.size() + (names == null ? 0 : names.size()));
        for (final Permissible permissible : permissibles) {
            if (permissible instanceof CommandSender && permissible.hasPermission(Permissions.NOTIFY)) {
                final CommandSender sender = (CommandSender) permissible;
                if ((sender instanceof Player) && hasTurnedOffNotifications(((Player) sender).getName())) {
                    continue;
                }

                sender.sendMessage(message);
                done.add(sender.getName());
            }
        }
        // Fall-back checking for players.
        if (names != null) {
            for (final String name : names) {
                if (!done.contains(name)) {
                    final Player player = DataManager.getPlayerExact(name);
                    if (player != null && player.hasPermission(Permissions.NOTIFY)) {
                        if (hasTurnedOffNotifications(player.getName())) {
                            continue;
                        }
                        player.sendMessage(message); 
                        done.add(name);
                    }
                }
            }
        }
        return done.size();
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.components.NoCheatPlusAPI#sendMessageDelayed(java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessageOnTick(final String playerName, final String message) {
        playerMessageSender.sendMessageThreadSafe(playerName, message);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Collection<ComponentRegistry<T>> getComponentRegistries(final Class<ComponentRegistry<T>> clazz) {
        final List<ComponentRegistry<T>> result = new LinkedList<ComponentRegistry<T>>();
        for (final ComponentRegistry<?> registry : subRegistries) {
            if (clazz.isAssignableFrom(registry.getClass())) {
                try{
                    result.add((ComponentRegistry<T>) registry);
                }
                catch(Throwable t) {
                    // Ignore.
                }
            }
        }
        return result;
    }

    /**
     * Convenience method to add components according to implemented interfaces,
     * like Listener, INotifyReload, INeedConfig.<br>
     * For the NoCheatPlus instance this must be done after the configuration
     * has been initialized. This will also register ComponentRegistry instances
     * if given.
     */
    @Override
    public boolean addComponent(final Object obj) {
        return addComponent(obj, true);
    }

    /**
     * Convenience method to add components according to implemented interfaces,
     * like Listener, INotifyReload, INeedConfig.<br>
     * For the NoCheatPlus instance this must be done after the configuration
     * has been initialized.
     * 
     * @param allowComponentRegistry
     *            Only registers ComponentRegistry instances if this is set to
     *            true.
     */
    @Override
    public boolean addComponent(final Object obj, final boolean allowComponentRegistry) {

        // TODO: Allow to add ComponentFactory + contract (renew with reload etc.)?
        if (obj == this) {
            throw new IllegalArgumentException("Can not register NoCheatPlus with itself.");
        }

        // Don't register twice.
        if (allComponents.contains(obj)) {
            // All added components are in here.
            return false;
        }

        boolean added = false;

        // First register generic instances.
        if (obj instanceof IRegisterAsGenericInstance) {
            registerGenericInstance(obj);
        }

        // Other types.
        if (obj instanceof Listener) {
            addListener((Listener) obj);
            added = true;
        }
        if (obj instanceof INotifyReload) {
            notifyReload.add((INotifyReload) obj);
            if (obj instanceof INeedConfig) {
                ((INeedConfig) obj).onReload();
            }
            added = true;
        }
        if (obj instanceof TickListener) {
            TickTask.addTickListener((TickListener) obj);
            added = true;
        }
        if (obj instanceof PermStateReceiver) {
            // No immediate update done.
            permStateReceivers.add((PermStateReceiver) obj);
            added = true;
        }
        if (obj instanceof ConsistencyChecker) {
            consistencyCheckers.add((ConsistencyChecker) obj);
            added = true;
        }
        if (obj instanceof JoinLeaveListener) {
            joinLeaveListeners.add((JoinLeaveListener) obj);
            added = true;
        }
        if (obj instanceof IDisableListener) {
            disableListeners.add((IDisableListener) obj);
            added = true;
        }

        // Add to sub registries.
        for (final ComponentRegistry<?> registry : subRegistries) {
            final Object res = ReflectionUtil.invokeGenericMethodOneArg(registry, "addComponent", obj);
            if (res != null && (res instanceof Boolean) && ((Boolean) res).booleanValue()) {
                added = true;
            }
        }

        // Add ComponentRegistry instances after adding to sub registries to prevent adding it to itself.
        if (allowComponentRegistry && (obj instanceof ComponentRegistry<?>)) {
            subRegistries.add((ComponentRegistry<?>) obj);
            added = true;
        }

        // Components holding more components to register later.
        if (obj instanceof IHoldSubComponents) {
            subComponentholders.add((IHoldSubComponents) obj);
            onDemandTickListener.register();
            added = true; // Convention.
        }

        // Add to allComponents if in fact added.
        if (added) {
            allComponents.add(obj);
        }

        // Post register hooks.
        if (obj instanceof IPostRegisterRunnable) {
            ((IPostRegisterRunnable) obj).runPostRegister();
        }

        return added;
    }

    /**
     * Interfaces checked for managed listeners: IHaveMethodOrder (method), ComponentWithName (tag)<br>
     * @param listener
     */
    private void addListener(final Listener listener) {
        // private: Use addComponent.
        if (manageListeners) {
            String tag = "NoCheatPlus";
            if (listener instanceof ComponentWithName) {
                tag = ((ComponentWithName) listener).getComponentName();
            }
            listenerManager.registerAllEventHandlers(listener, tag);
            listeners.add(listener);
        }
        else {
            Bukkit.getPluginManager().registerEvents(listener, this);
            if (listener instanceof IHaveMethodOrder) {
                // TODO: Might log the order too, might prevent registration ?
                // TODO: Alternative: queue listeners and register after startup (!)
                logManager.warning(Streams.INIT, "Listener demands registration order, but listeners are not managed: " + listener.getClass().getName());
            }
        }
    }

    /**
     * Test if NCP uses the ListenerManager at all.
     * @return If so.
     */
    public boolean doesManageListeners() {
        return manageListeners;
    }

    @Override
    public void removeComponent(final Object obj) {
        if (obj instanceof Listener) {
            listeners.remove((Listener) obj);
            listenerManager.remove((Listener) obj);
        }
        if (obj instanceof PermStateReceiver) {
            permStateReceivers.remove((PermStateReceiver) obj);
        }
        if (obj instanceof TickListener) {
            TickTask.removeTickListener((TickListener) obj);
        }
        if (obj instanceof INotifyReload) {
            notifyReload.remove((INotifyReload) obj);
        }
        if (obj instanceof ConsistencyChecker) {
            consistencyCheckers.remove((ConsistencyChecker) obj);
        }
        if (obj instanceof JoinLeaveListener) {
            joinLeaveListeners.remove((JoinLeaveListener) obj);
        }
        if (obj instanceof IDisableListener) {
            disableListeners.remove((IDisableListener) obj);
        }

        // Remove sub registries.
        if (obj instanceof ComponentRegistry<?>) {
            subRegistries.remove((ComponentRegistry<?>) obj);
        }
        // Remove from present registries, order prevents to remove from itself.
        for (final ComponentRegistry<?> registry : subRegistries) {
            ReflectionUtil.invokeGenericMethodOneArg(registry, "removeComponent", obj);
        }

        allComponents.remove(obj);
    }

    /**
     * (Called on the plugin getting disabled.)
     * <hr>
     * Rough order of disabling:<br>
     * <ul>
     * <li>Prevent further registration. For now also disable listeners, though
     * this might get shifted still.</li>
     * <li><b>Call onDisable for IDisableListener instances, in reversed
     * order of registration.</b> This includes clearing all data (Needs extensions for sorting
     * by priority for IDisableListener instances.).</li>
     * <li>Random sequence of cleanup calls for other registries and logging
     * statistics.</li>
     * <li>Call removeComponent for all registered components.</li>
     * <li>Cleanup BlockProperties and clear most internal mappings for
     * components (needs a more clean registry approach).</li>
     * <li>(Command changes cleanup: currently disabled due to compatibility
     * issues, could cause other issues with dynamic plugin managers.)</li>
     * <li>Cleanup ConfigManager.</li>
     * <li>Shutdown LogManager.</li>
     * </ul>
     */
    @Override
    public void onDisable() {

        final boolean verbose = ConfigManager.getConfigFile().getBoolean(ConfPaths.LOGGING_EXTENDED_STATUS);

        // Remove listener references.
        if (verbose) {
            if (listenerManager.hasListenerMethods()) {
                logManager.info(Streams.INIT, "Cleanup ListenerManager...");
            }
            else {
                logManager.info(Streams.INIT, "(ListenerManager not in use, prevent registering...)");
            }
        }
        listenerManager.setRegisterDirectly(false);
        listenerManager.clear();
        lateListenerRegistered = false;

        BukkitScheduler sched = getServer().getScheduler();

        // Stop data-man task.
        if (dataManTaskId != -1) {
            sched.cancelTask(dataManTaskId);
            dataManTaskId = -1;
        }

        // Stop the tickTask.
        if (verbose) {
            logManager.info(Streams.INIT, "Stop TickTask...");
        }
        TickTask.setLocked(true);
        TickTask.purge();
        TickTask.cancel();
        TickTask.removeAllTickListeners();
        // (Keep the tick task locked!)

        // Stop consistency checking task.
        if (consistencyCheckerTaskId != -1) {
            sched.cancelTask(consistencyCheckerTaskId);
            consistencyCheckerTaskId = -1;
        }

        // Just to be sure nothing gets left out.
        if (verbose) {
            logManager.info(Streams.INIT, "Stop all remaining tasks...");
        }
        sched.cancelTasks(this);

        // DisableListener.onDisable (includes DataManager cleanup.)
        if (verbose) {
            logManager.info(Streams.INIT, "onDisable calls (include DataManager cleanup)...");
        }
        // TODO: Reliable sorting order + sort now (checks and data cleanup late, data manager last, allow plugins to access stuff before data is reset).
        final ArrayList<IDisableListener> disableListeners = new ArrayList<IDisableListener>(this.disableListeners);
        Collections.reverse(disableListeners);
        for (final IDisableListener dl : disableListeners) {
            try {
                dl.onDisable();
            } catch (Throwable t) {
                logManager.severe(Streams.INIT, "IDisableListener (" + dl.getClass().getName() + "): " + t.getClass().getSimpleName() + " / " + t.getMessage());
                logManager.severe(Streams.INIT, t);
            }
        }
        // (Component removal will clear the list, rather.)

        // ExemptionManager cleanup.
        if (verbose) {
            logManager.info(Streams.INIT, "Reset ExemptionManager...");
        }
        NCPExemptionManager.clear();

        // Remove hooks.
        allViolationsHook.unregister();
        NCPHookManager.removeAllHooks();

        // Write some debug/statistics.
        final Counters counters = getGenericInstance(Counters.class);
        if (counters != null) {
            // Ensure we get this kind of information for the time being.
            if (verbose) {
                logManager.info(Streams.INIT, counters.getMergedCountsString(true)); // Server logger needs info level.
            } else {
                logManager.debug(Streams.TRACE_FILE, counters.getMergedCountsString(true));
            }
        }

        // Hooks:
        // (Expect external plugins to unregister their hooks on their own.)
        // (No native hooks present, yet.)

        // Unregister all added components explicitly (reverse order).
        if (verbose) {
            logManager.info(Streams.INIT, "Unregister all registered components...");
        }
        final ArrayList<Object> components = new ArrayList<Object>(this.allComponents);
        for (int i = components.size() - 1; i >= 0; i--) {
            removeComponent(components.get(i));
        }

        // Cleanup BlockProperties.
        if (verbose) {
            logManager.info(Streams.INIT, "Cleanup BlockProperties...");
        }
        BlockProperties.cleanup();

        if (verbose) {
            logManager.info(Streams.INIT, "Cleanup some mappings...");
        }
        // Remove IDisableListener instances.
        disableListeners.clear(); // Just in case.
        // Remove listeners.
        listeners.clear();
        // Remove config listeners.
        notifyReload.clear();
        // World specific permissions.
        permStateReceivers.clear();
        // Sub registries.
        subRegistries.clear();
        // Just in case: clear the subComponentHolders.
        subComponentholders.clear();
        // Generic instances registry.
        genericInstanceRegistry.clear();
        // Feature tags.
        featureTags.clear();
        // BlockChangeTracker.
        if (blockChangeListener != null) {
            blockChangeListener.setEnabled(false);
            blockChangeListener = null; // Only on disable.
        }
        blockChangeTracker.clear();

        // Restore changed commands.
        //		if (verbose) LogUtil.logInfo("Undo command changes...");
        //		undoCommandChanges();
        // Clear command changes list (compatibility issues with NPCs, leads to recalculation of perms).
        changedCommands.clear();

        // Cleanup the configuration manager.
        if (verbose) {
            logManager.info(Streams.INIT, "Cleanup ConfigManager...");
        }
        ConfigManager.cleanup();

        // Cleanup file logger.
        if (verbose) {
            logManager.info(Streams.INIT, "Shutdown LogManager...");
        }
        StaticLog.setUseLogManager(false);
        StaticLog.setStreamID(Streams.INIT);
        logManager.shutdown();

        // Tell the server administrator that we finished unloading NoCheatPlus.
        if (verbose) {
            Bukkit.getLogger().info("[NoCheatPlus] All cleanup done."); // Bukkit logger.
        }
        final PluginDescriptionFile pdfFile = getDescription();
        Bukkit.getLogger().info("[NoCheatPlus] Version " + pdfFile.getVersion() + " is disabled."); // Bukkit logger.
    }

    /**
     * Does not undo 100%, but restores
     * <ul>
     * <li>old permission,</li>
     * <li>permission-message,</li>
     * <li>label (unlikely to be changed),</li>
     * <li>permission default.</li>
     * </ul>
     * 
     * @deprecated Leads to compatibility issues with NPC plugins such as
     *             Citizens 2, due to recalculation of permissions (specifically
     *             during disabling).
     */
    public void undoCommandChanges() {
        if (!changedCommands.isEmpty()) {
            final Iterator<CommandProtectionEntry> it = changedCommands.descendingIterator();
            while (it.hasNext()) {
                it.next().restore();
            }
            changedCommands.clear();
        }
    }

    protected void setupCommandProtection() {
        // TODO: Might re-check with plugins enabling during runtime (!).
        // Read lists and messages from config.
        final ConfigFile config = ConfigManager.getConfigFile();
        // (Might add options to invert selection.)
        // "No permission".
        // TODO: Could/should set permission message to null here (server default), might use keyword "default".
        final List<String> noPerm = config.getStringList(ConfPaths.PROTECT_PLUGINS_HIDE_NOPERMISSION_CMDS);
        if (noPerm != null && !noPerm.isEmpty()) {
            final String noPermMsg = ColorUtil.replaceColors(ConfigManager.getConfigFile().getString(ConfPaths.PROTECT_PLUGINS_HIDE_NOPERMISSION_MSG));
            // Setup and add changes to history for undoing.
            changedCommands.addAll(PermissionUtil.protectCommands(Permissions.FILTER_COMMAND, noPerm,  true, false, noPermMsg));
        }
        // "Unknown command", override the other option.
        final List<String> noCommand = config.getStringList(ConfPaths.PROTECT_PLUGINS_HIDE_NOCOMMAND_CMDS);
        if (noCommand != null && !noCommand.isEmpty()) {
            final String noCommandMsg = ColorUtil.replaceColors(ConfigManager.getConfigFile().getString(ConfPaths.PROTECT_PLUGINS_HIDE_NOCOMMAND_MSG));
            // Setup and add changes to history for undoing.
            changedCommands.addAll(PermissionUtil.protectCommands(Permissions.FILTER_COMMAND, noCommand,  true, false, noCommandMsg));
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onLoad()
     */
    @Override
    public void onLoad() {
        Bukkit.getLogger().info("[NoCheatPlus] onLoad: Early set up of static API, configuration, logging."); // Bukkit logger.
        setupBasics();
    }

    /**
     * Lazy initialization of basics (static API, configuration, logging).
     */
    private void setupBasics() {
        if (NCPAPIProvider.getNoCheatPlusAPI() == null) {
            NCPAPIProvider.setNoCheatPlusAPI(this);
        }
        if (ServerVersion.getMinecraftVersion() == GenericVersion.UNKNOWN_VERSION) {
            BukkitVersion.init();
        }
        if (!ConfigManager.isInitialized()) {
            ConfigManager.init(this);
            // Basic setup for exemption (uses CheckType). This is redundant, but should not hurt.
            NCPExemptionManager.setExemptionSettings(new ExemptionSettings(ConfigManager.getConfigFile()));
        }
        if (logManager == null || logManager.getStreamID(Streams.STATUS.name) != Streams.STATUS) {
            logManager = new BukkitLogManager(this);
            StaticLog.setStreamID(Streams.INIT);
            StaticLog.setUseLogManager(true);
            logManager.info(Streams.INIT, "Logging system initialized.");
            logManager.info(Streams.INIT, "Detected Minecraft version: " + ServerVersion.getMinecraftVersion());
            genericInstanceRegistry.setLogger(
                    logManager, new IGetStreamId() {
                        @Override
                        public StreamID getStreamId() {
                            // TODO Auto-generated method stub
                            return NoCheatPlus.this.getRegistryStreamId();
                        }
                    }, "[GenericInstanceRegistry] ");
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // Reset TickTask (just in case).
        TickTask.setLocked(true);
        TickTask.purge();
        TickTask.cancel();
        TickTask.reset();

        // Allow entries to TickTask.
        TickTask.setLocked(false);

        // Re-check basic setup (if onLoad gets skipped by some custom thing).
        setupBasics();
        if (Bugs.shouldEnforceLocation()) {
            addFeatureTags("defaults", Arrays.asList("enforceLocation"));
        }
        if (Bugs.shouldPvpKnockBackVelocity()) {
            addFeatureTags("defaults", Arrays.asList("pvpKnockBackVelocity"));
        }
        if (dataMan.storesPlayerInstances()) {
            addFeatureTags("defaults", Arrays.asList("storePlayers"));
        }

        // Start logger task(s).
        logManager.startTasks();

        final ConfigFile config = ConfigManager.getConfigFile();

        // Set some instance members.
        setInstanceMembers(config);

        // Listener manager.
        manageListeners = config.getBoolean(ConfPaths.COMPATIBILITY_MANAGELISTENERS);
        if (manageListeners) {
            listenerManager.setRegisterDirectly(true);
            listenerManager.registerAllWithBukkit();
        }
        else {
            // Just for safety.
            listenerManager.setRegisterDirectly(false);
            listenerManager.clear();
        }

        // Register some generic stuff.
        // (Deny change some.)
        registerGenericInstance(new Counters());
        genericInstanceRegistry.denyChangeExistingRegistration(Counters.class);
        registerGenericInstance(new WRPT());
        genericInstanceRegistry.denyChangeExistingRegistration(WRPT.class);
        registerGenericInstance(new TraceEntryPool(1000)); // Random number.
        genericInstanceRegistry.denyChangeExistingRegistration(TraceEntryPool.class);
        // (Allow override others.)
        registerGenericInstance(new Random(System.currentTimeMillis() ^ ((long) this.hashCode() * (long) listenerManager.hashCode() * (long) logManager.hashCode())));
        addComponent(new BridgeCrossPlugin());

        // Initialize MCAccess.
        initMCAccess(config);

        // Initialize BlockProperties.
        initBlockProperties(config);

        // Initialize data manager.
        dataMan.onEnable();

        // Register components. 
        @SetupOrder(priority = - 100)
        class ReloadHook implements INotifyReload{
            @Override
            public void onReload() {
                // Only for reloading, not INeedConfig.
                processReload();
            }
        }

        // Add the "low level" system components first.
        for (final Object obj : new Object[]{
                nameSetPerms,
                getCoreListener(),
                // Put ReloadListener first, because Checks could also listen to it.
                new ReloadHook(),
                dataMan,
                new AuxMoving(),
        }) {
            addComponent(obj);
            // Register sub-components (allow later added to use registries, if any).
            processQueuedSubComponentHolders();
        }
        updateBlockChangeTracker(config);

        // Register "higher level" components (check listeners).
        for (final Object obj : new Object[]{
                new BlockInteractListener(),
                new BlockBreakListener(),
                new BlockPlaceListener(),
                new ChatListener(),
                new CombinedListener(),
                // Do mind registration order: Combined must come before Fight.
                new FightListener(),
                new InventoryListener(),
                new MovingListener(),
        }) {
            addComponent(obj);
            // Register sub-components (allow later added to use registries, if any).
            processQueuedSubComponentHolders();
        }

        // Register optional default components.
        final DefaultComponentFactory dcf = new DefaultComponentFactory();
        for (final Object obj : dcf.getAvailableComponentsOnEnable(this)) {
            addComponent(obj);
            // Register sub-components to enable registries for optional components.
            processQueuedSubComponentHolders();
        }

        // Register the commands handler.
        final PluginCommand command = getCommand("nocheatplus");
        final NoCheatPlusCommand commandHandler = new NoCheatPlusCommand(this, notifyReload);
        command.setExecutor(commandHandler);
        // (CommandHandler is TabExecutor.)

        // Set up the tick task.
        TickTask.start(this);

        // dataMan expiration checking.
        this.dataManTaskId  = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                dataMan.checkExpiration();
            }
        }, 1207, 1207);

        // Ensure dataMan is first on disableListeners.
        disableListeners.remove(dataMan);
        disableListeners.add(0, dataMan);

        // Set up consistency checking.
        scheduleConsistencyCheckers();

        // Setup allViolationsHook
        allViolationsHook.setConfig(new AllViolationsConfig(config));

        //        if (config.getBoolean(ConfPaths.MISCELLANEOUS_CHECKFORUPDATES)) {
        //            // Is a new update available?
        //        	final int timeout = config.getInt(ConfPaths.MISCELLANEOUS_UPDATETIMEOUT, 4) * 1000;
        //        	getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
        //				@Override
        //				public void run() {
        //					updateAvailable = Updates.checkForUpdates(getDescription().getVersion(), timeout);
        //				}
        //			});
        //        }

        // Log other notes.
        logOtherNotes(config);

        // Is the version the configuration was created with consistent with the current one?
        if (configProblems != null && config.getBoolean(ConfPaths.CONFIGVERSION_NOTIFY)) {
            // Could use custom prefix from logging, however ncp should be mentioned then.
            logManager.warning(Streams.INIT, "" + configProblems);
        }

        // Care for already online players.
        final Player[] onlinePlayers = BridgeMisc.getOnlinePlayers();
        // TODO: re-map ExemptionManager !
        // TODO: Disable all checks for these players for one tick ?
        // TODO: Prepare check data for players [problem: permissions]?
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new PostEnableTask(commandHandler, onlinePlayers));

        // Mid-term cleanup (seconds range).
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                midTermCleanup();
            }
        }, 83, 83);

        // Set StaticLog to more efficient output.
        StaticLog.setStreamID(Streams.STATUS);
        // Tell the server administrator that we finished loading NoCheatPlus now.
        logManager.info(Streams.INIT, "Version " + getDescription().getVersion() + " is enabled.");
    }

    /**
     * Log other notes once on enabling.
     * 
     * @param config
     */
    private void logOtherNotes(ConfigFile config) {
        if (ServerVersion.compareMinecraftVersion("1.9") >= 0) {
            logManager.info(Streams.INIT, "Force disable FastHeal on Minecraft 1.9 and later.");
        }
    }

    /**
     * Actions to be done after enable of  all plugins. This aims at reloading mainly.
     */
    protected void postEnable(final NoCheatPlusCommand commandHandler, final Player[] onlinePlayers) {
        logManager.info(Streams.INIT, "Post-enable running...");
        try {
            // Set child permissions for commands for faster checking.
            PermissionUtil.addChildPermission(commandHandler.getAllSubCommandPermissions(), Permissions.FILTER_COMMAND_NOCHEATPLUS, PermissionDefault.OP);
        } catch (Throwable t) {
            logManager.severe(Streams.INIT, "Failed to complement permissions: " + t.getClass().getSimpleName());
            logManager.severe(Streams.INIT, t);
        }
        try {
            // Command protection feature.
            if (ConfigManager.getConfigFile().getBoolean(ConfPaths.PROTECT_PLUGINS_HIDE_ACTIVE)) {
                setupCommandProtection();
            }
        } catch (Throwable t) {
            logManager.severe(Streams.INIT, "Failed to apply command protection: " + t.getClass().getSimpleName());
            logManager.severe(Streams.INIT, t);
        }
        for (final Player player : onlinePlayers) {
            updatePermStateReceivers(player);
            if (player.isSleeping()) {
                CombinedData.getData(player).wasInBed = true;
            }
        }
        if (onlinePlayers.length > 0) {
            logManager.info(Streams.INIT, "Updated data for " + onlinePlayers.length + " players (post-enable).");
        }
        // Register late listener.
        Bukkit.getPluginManager().registerEvents(getLateListener(), this);
        lateListenerRegistered = true;
        // Finished.
        logManager.info(Streams.INIT, "Post-enable finished.");
        // Log version to file (queued).
        logManager.info(Streams.DEFAULT_FILE, StringUtil.join(VersionCommand.getVersionInfo(), "\n"));
    }

    /**
     * Empties and registers the subComponentHolders list.
     */
    protected void processQueuedSubComponentHolders() {
        if (subComponentholders.isEmpty()) return;
        final List<IHoldSubComponents> copied = new ArrayList<IHoldSubComponents>(subComponentholders);
        subComponentholders.clear();
        for (final IHoldSubComponents holder : copied) {
            for (final Object component : holder.getSubComponents()) {
                addComponent(component);
            }
        }
    }

    /**
     * All action done on reload.
     */
    protected void processReload() {
        final ConfigFile config = ConfigManager.getConfigFile();
        setInstanceMembers(config);
        // TODO: Process registered ComponentFactory instances.
        // Set up MCAccess.
        initMCAccess(config);
        // Initialize BlockProperties
        initBlockProperties(config);
        // Reset Command protection.
        undoCommandChanges();
        if (config.getBoolean(ConfPaths.PROTECT_PLUGINS_HIDE_ACTIVE)) {
            setupCommandProtection();
        }
        // (Re-) schedule consistency checking.
        scheduleConsistencyCheckers();
        // Cache some things. TODO: Where is this comment from !?
        // Re-setup allViolationsHook.
        allViolationsHook.setConfig(new AllViolationsConfig(config));
        // Set block change tracker.
        updateBlockChangeTracker(config);
    }

    /**
     * Set instance members based on the given configuration. This is meant to
     * work after reloading the configuration too.
     * 
     * @param config
     */
    private void setInstanceMembers(final ConfigFile config) {
        configProblems = Updates.isConfigUpToDate(config);
        useSubscriptions = config.getBoolean(ConfPaths.LOGGING_BACKEND_INGAMECHAT_SUBSCRIPTIONS);
        clearExemptionsOnJoin = config.getBoolean(ConfPaths.COMPATIBILITY_EXEMPTIONS_REMOVE_JOIN);
        clearExemptionsOnLeave = config.getBoolean(ConfPaths.COMPATIBILITY_EXEMPTIONS_REMOVE_LEAVE);
        NCPExemptionManager.setExemptionSettings(new ExemptionSettings(config));
    }

    private void updateBlockChangeTracker(final ConfigFile config) {
        // Activation / listener.
        if (config.getBoolean(ConfPaths.COMPATIBILITY_BLOCKS_CHANGETRACKER_ACTIVE) 
                && config.getBoolean(ConfPaths.COMPATIBILITY_BLOCKS_CHANGETRACKER_PISTONS)) {
            if (blockChangeListener == null) {
                blockChangeListener = new BlockChangeListener(blockChangeTracker);
                this.addComponent(blockChangeListener);
            }
            blockChangeListener.setEnabled(true);
        }
        else if (blockChangeListener != null) {
            blockChangeListener.setEnabled(false);
            blockChangeTracker.clear();
        }
        // Configuration.
        blockChangeTracker.setExpirationAgeTicks(config.getInt(ConfPaths.COMPATIBILITY_BLOCKS_CHANGETRACKER_MAXAGETICKS));
        blockChangeTracker.setWorldNodeSkipSize(config.getInt(ConfPaths.COMPATIBILITY_BLOCKS_CHANGETRACKER_PERWORLD_MAXENTRIES));
        blockChangeTracker.updateBlockCacheHandle();
    }

    @Override
    public LogManager getLogManager() {
        return logManager;
    }

    /**
     * (Re-) Setup MCAccess and other access providers from the internal
     * factories. Only call from the primary thread.
     * 
     * @param config
     */
    private MCAccess initMCAccess(final ConfigFile config) {
        // TODO: Auto registry with unregister on reload hooks (more clean reload).
        // Reset MCAccess.
        // TODO: Might fire a NCPSetMCAccessFromFactoryEvent (include getting and setting)!
        final MCAccessConfig mcaC = new MCAccessConfig(config);
        final MCAccess mcAccess = new MCAccessFactory().getMCAccess(mcaC);
        /*
         * NOTE: previously registration was done last, to allow fetching the
         * previous registration. That has been discarded, due to the number of
         * related objects registering anyway.
         */
        // Set in registry.
        genericInstanceRegistry.registerGenericInstance(MCAccess.class, mcAccess);
        // Register a BlockCache instance for temporary use (enables using handle thing).
        genericInstanceRegistry.registerGenericInstance(BlockCache.class, mcAccess.getBlockCache());
        // Spin-off.
        new AttributeAccessFactory().setupAttributeAccess(mcAccess, mcaC);
        new EntityAccessFactory().setupEntityAccess(mcAccess, mcaC);

        // TODO: Summary event or listener call-back (possibly in another place.).

        // Log.
        logManager.info(Streams.INIT, "McAccess set to: " + mcAccess.getMCVersion() + " / " + mcAccess.getServerVersionTag());

        return mcAccess;
    }

    /**
     * Initialize BlockProperties, including config. Needs initMCAccess to be
     * called before.
     */
    protected void initBlockProperties(ConfigFile config) {
        // Set up BlockProperties.
        BlockProperties.init(mcAccess, ConfigManager.getWorldConfigProvider());
        BlockProperties.applyConfig(config, ConfPaths.COMPATIBILITY_BLOCKS);
        // Schedule dumping the blocks properties (to let other plugins override).
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                // Debug information about unknown blocks.
                // (Probably removed later.)
                ConfigFile config = ConfigManager.getConfigFile();
                BlockProperties.dumpBlocks(config.getBoolean(ConfPaths.BLOCKBREAK_DEBUG, config.getBoolean(ConfPaths.CHECKS_DEBUG, false)));
            }
        });
    }

    /**
     * Listener to be registered in postEnable. Flag must be set elsewhere.
     * 
     * @return
     */
    private Listener getLateListener() {
        return new NCPListener() {
            @EventHandler(priority = EventPriority.LOWEST) // Do update comment in NoCheatPlusAPI with changing.
            public void onPlayerJoinLowestLate(final PlayerJoinEvent event) {
                updatePermStateReceivers(event.getPlayer());
            }
        };
    }

    /**
     * Quick solution to hide the listener methods, expect refactoring.
     * @return
     */
    private Listener getCoreListener() {
        return new NCPListener() {
            @EventHandler(priority = EventPriority.NORMAL)
            public void onPlayerLogin(final PlayerLoginEvent event) {
                // (NORMAL to have chat checks come after this.)
                if (event.getResult() != Result.ALLOWED) {
                    return;
                }
                final Player player = event.getPlayer();
                // Check if login is denied:
                checkDenyLoginsNames();
                if (player.hasPermission(Permissions.BYPASS_DENY_LOGIN)) {
                    return;
                }
                if (isLoginDenied(player.getName())) {
                    // TODO: Consider using the vanilla temporary ban feature instead (for an alternative?).
                    // TODO: Display time for which the player is banned.
                    event.setResult(Result.KICK_OTHER);
                    // TODO: Some basic/language configuration object, possibly independent of checks.
                    event.setKickMessage(ColorUtil.replaceColors(ConfigManager.getConfigFile(player.getWorld().getName()).getString(ConfPaths.STRINGS + ".msgtempdenylogin")));
                }
            }

            @EventHandler(priority = EventPriority.LOWEST) // Do update comment in NoCheatPlusAPI with changing.
            public void onPlayerJoinLowest(final PlayerJoinEvent event) {
                final Player player = event.getPlayer();
                if (!lateListenerRegistered) {
                    // Let's see if this gets logged with big servers :).
                    NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, "Player " + player.getName() + " joins before the post-enable task has run.");
                    // (Assume postEnable will run and call updatePermStateReceivers(player).)
                } else {
                    updatePermStateReceivers(player);
                }
                if (clearExemptionsOnJoin) {
                    NCPExemptionManager.unexempt(player);
                }
            }

            @EventHandler(priority = EventPriority.LOW)
            public void onPlayerJoinLow(final PlayerJoinEvent event) {
                // LOWEST is for DataMan and CombinedListener.
                onJoinLow(event.getPlayer());
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onPlayerchangedWorld(final PlayerChangedWorldEvent event) {
                updatePermStateReceivers(event.getPlayer());
            }

            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void onPlayerKick(final PlayerKickEvent event) {
                onLeave(event.getPlayer());
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onPlayerQuit(final PlayerQuitEvent event) {
                onLeave(event.getPlayer());
            }
        };
    }

    protected void onJoinLow(final Player player) {
        final String playerName = player.getName();
        if (nameSetPerms.hasPermission(playerName, Permissions.NOTIFY)) {
            // Login notifications...
            final PlayerData data = DataManager.getPlayerData(playerName, true);
            //			// Update available.
            //			if (updateAvailable) player.sendMessage(ChatColor.RED + "NCP: " + ChatColor.WHITE + "A new update of NoCheatPlus is available.\n" + "Download it at http://nocheatplus.org/update");

            // Inconsistent config version.
            if (configProblems != null && ConfigManager.getConfigFile().getBoolean(ConfPaths.CONFIGVERSION_NOTIFY)) {
                // Could use custom prefix from logging, however ncp should be mentioned then.
                sendMessageOnTick(playerName, ChatColor.RED + "NCP: " + ChatColor.WHITE + configProblems);
            }
            // Message if notify is turned off.
            if (data.getNotifyOff()) {
                sendMessageOnTick(playerName, MSG_NOTIFY_OFF);
            }
        }
        // JoinLeaveListenerS: Do update comment in NoCheatPlusAPI with changing event priority.
        for (final JoinLeaveListener jlListener : joinLeaveListeners) {
            try{
                jlListener.playerJoins(player);
            }
            catch(Throwable t) {
                logManager.severe(Streams.INIT, "JoinLeaveListener(" + jlListener.getClass().getName() + ") generated an exception (join): " + t.getClass().getSimpleName());
                logManager.severe(Streams.INIT, t);
            }
        }
        // Mod message (left on low instead of lowest to allow some permissions plugins compatibility).
        ModUtil.motdOnJoin(player);
    }

    protected void onLeave(final Player player) {
        for (final PermStateReceiver pr : permStateReceivers) {
            pr.removePlayer(player.getName());
        }
        for (final JoinLeaveListener jlListener : joinLeaveListeners) {
            try{
                jlListener.playerLeaves(player);
            }
            catch(Throwable t) {
                logManager.severe(Streams.INIT, "JoinLeaveListener(" + jlListener.getClass().getName() + ") generated an exception (leave): " + t.getClass().getSimpleName());
                logManager.severe(Streams.INIT, t);
            }
        }
        if (clearExemptionsOnLeave) {
            NCPExemptionManager.unexempt(player);
        }
    }

    protected void updatePermStateReceivers(final Player player) {
        final Map<String, Boolean> checked = new HashMap<String, Boolean>(20);
        final String name = player.getName();
        for (final PermStateReceiver pr : permStateReceivers) {
            for (final String permission : pr.getDefaultPermissions()) {
                Boolean state = checked.get(permission);
                if (state == null) {
                    state = player.hasPermission(permission);
                    checked.put(permission, state);
                }
                pr.setPermission(name, permission, state);
            }
        }
    }

    protected void scheduleConsistencyCheckers() {
        BukkitScheduler sched = getServer().getScheduler();
        if (consistencyCheckerTaskId != -1) {
            sched.cancelTask(consistencyCheckerTaskId);
        }
        ConfigFile config = ConfigManager.getConfigFile();
        if (!config.getBoolean(ConfPaths.DATA_CONSISTENCYCHECKS_CHECK, true)) {
            return;
        }
        // Schedule task in seconds.
        final long delay = 20L * config.getInt(ConfPaths.DATA_CONSISTENCYCHECKS_INTERVAL, 1, 3600, 10);
        consistencyCheckerTaskId = sched.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                runConsistencyChecks();
            }
        }, delay, delay );
    }

    /**
     * Several seconds, repeating.
     */
    protected void midTermCleanup() {
        if (blockChangeListener != null && blockChangeListener.isEnabled()) {
            blockChangeTracker.checkExpiration(TickTask.getTick());
        }
    }

    /**
     * Run consistency checks for at most the configured duration. If not finished, a task will be scheduled to continue.
     */
    protected void runConsistencyChecks() {
        final long tStart = System.currentTimeMillis();
        final ConfigFile config = ConfigManager.getConfigFile();
        if (!config.getBoolean(ConfPaths.DATA_CONSISTENCYCHECKS_CHECK) || consistencyCheckers.isEmpty()) {
            consistencyCheckerIndex = 0;
            return;
        }
        final long tEnd = tStart + config.getLong(ConfPaths.DATA_CONSISTENCYCHECKS_MAXTIME, 1, 50, 2);
        if (consistencyCheckerIndex >= consistencyCheckers.size()) consistencyCheckerIndex = 0;
        final Player[] onlinePlayers = BridgeMisc.getOnlinePlayers();
        // Loop
        while (consistencyCheckerIndex < consistencyCheckers.size()) {
            final ConsistencyChecker checker = consistencyCheckers.get(consistencyCheckerIndex);
            try{
                checker.checkConsistency(onlinePlayers);
            }
            catch (Throwable t) {
                logManager.severe(Streams.INIT, "ConsistencyChecker(" + checker.getClass().getName() + ") encountered an exception:");
                logManager.severe(Streams.INIT, t);
            }
            consistencyCheckerIndex ++; // Do not remove :).
            final long now = System.currentTimeMillis();
            if (now < tStart || now >= tEnd) {
                break;
            }
        }
        // (The index might be bigger than size by now.)

        // If not finished, schedule further checks.
        if (consistencyCheckerIndex < consistencyCheckers.size()) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    runConsistencyChecks();
                }
            });
            if (config.getBoolean(ConfPaths.LOGGING_EXTENDED_STATUS)) {
                logManager.info(Streams.STATUS, "Interrupted consistency checking until next tick.");
            }
        }
    }

    @Override
    public <T> T getGenericInstance(Class<T> registeredFor) {
        return genericInstanceRegistry.getGenericInstance(registeredFor);
    }

    @Override
    public <T> T registerGenericInstance(T instance) {
        return genericInstanceRegistry.registerGenericInstance(instance);
    }

    @Override
    public <T, TI extends T> T registerGenericInstance(Class<T> registerFor, TI instance) {
        return genericInstanceRegistry.registerGenericInstance(registerFor, instance);
    }

    @Override
    public <T> T unregisterGenericInstance(Class<T> registeredFor) {
        return genericInstanceRegistry.unregisterGenericInstance(registeredFor);
    }

    @Override
    public <T> IGenericInstanceHandle<T> getGenericInstanceHandle(Class<T> registeredFor) {
        return genericInstanceRegistry.getGenericInstanceHandle(registeredFor);
    }

    @Override
    public void addFeatureTags(String key, Collection<String> featureTags) {
        LinkedHashSet<String> present = this.featureTags.get(key);
        if (present == null) {
            present = new LinkedHashSet<String>();
            this.featureTags.put(key, present);
        }
        present.addAll(featureTags);
    }

    @Override
    public void setFeatureTags(String key, Collection<String> featureTags) {
        LinkedHashSet<String> present = new LinkedHashSet<String>();
        this.featureTags.put(key, present);
        present.addAll(featureTags);
    }

    @Override
    public boolean hasFeatureTag(final String key, final String feature) {
        final Collection<String>  features = this.featureTags.get(key);
        return features == null ? false : features.contains(feature);
    }

    @Override
    public Map<String, Set<String>> getAllFeatureTags() {
        final LinkedHashMap<String, Set<String>> allTags = new LinkedHashMap<String, Set<String>>();
        for (final Entry<String, LinkedHashSet<String>> entry : this.featureTags.entrySet()) {
            allTags.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        return Collections.unmodifiableMap(allTags);
    }

    @Override
    public BlockChangeTracker getBlockChangeTracker() {
        return blockChangeTracker;
    }

}
