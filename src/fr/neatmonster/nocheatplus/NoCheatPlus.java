package fr.neatmonster.nocheatplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
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
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakListener;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractListener;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceListener;
import fr.neatmonster.nocheatplus.checks.chat.ChatListener;
import fr.neatmonster.nocheatplus.checks.combined.CombinedListener;
import fr.neatmonster.nocheatplus.checks.fight.FightListener;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryListener;
import fr.neatmonster.nocheatplus.checks.moving.MovingListener;
import fr.neatmonster.nocheatplus.command.CommandHandler;
import fr.neatmonster.nocheatplus.command.INotifyReload;
import fr.neatmonster.nocheatplus.components.ComponentWithName;
import fr.neatmonster.nocheatplus.components.INeedConfig;
import fr.neatmonster.nocheatplus.components.NCPListener;
import fr.neatmonster.nocheatplus.components.NameSetPermState;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.components.PermStateReceiver;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.config.DefaultConfig;
import fr.neatmonster.nocheatplus.event.IHaveMethodOrder;
import fr.neatmonster.nocheatplus.event.ListenerManager;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.metrics.Metrics;
import fr.neatmonster.nocheatplus.metrics.Metrics.Graph;
import fr.neatmonster.nocheatplus.metrics.Metrics.Plotter;
import fr.neatmonster.nocheatplus.metrics.MetricsData;
import fr.neatmonster.nocheatplus.permissions.PermissionUtil;
import fr.neatmonster.nocheatplus.permissions.PermissionUtil.CommandProtectionEntry;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;
import fr.neatmonster.nocheatplus.utilities.LogUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.Updates;

/*
 * M"""""""`YM          MM'""""'YMM dP                           dP   MM"""""""`YM dP                   
 * M  mmmm.  M          M' .mmm. `M 88                           88   MM  mmmmm  M 88                   
 * M  MMMMM  M .d8888b. M  MMMMMooM 88d888b. .d8888b. .d8888b. d8888P M'        .M 88 dP    dP .d8888b. 
 * M  MMMMM  M 88'  `88 M  MMMMMMMM 88'  `88 88ooood8 88'  `88   88   MM  MMMMMMMM 88 88    88 Y8ooooo. 
 * M  MMMMM  M 88.  .88 M. `MMM' .M 88    88 88.  ... 88.  .88   88   MM  MMMMMMMM 88 88.  .88       88 
 * M  MMMMM  M `88888P' MM.     .dM dP    dP `88888P' `88888P8   dP   MM  MMMMMMMM dP `88888P' `88888P' 
 * MMMMMMMMMMM          MMMMMMMMMMM                                   MMMMMMMMMMMM                      
 */
/**
 * This is the main class of NoCheatPlus. The commands, events listeners and tasks are registered here.
 */
public class NoCheatPlus extends JavaPlugin implements NoCheatPlusAPI {
	
	/** Lower case player name to milliseconds point of time of release */
	private static final Map<String, Long> denyLoginNames = Collections.synchronizedMap(new HashMap<String, Long>());
	
	/** Names of players with a certain permission. */
	protected static final NameSetPermState nameSetPerms = new NameSetPermState(Permissions.ADMINISTRATION_NOTIFY);
	
	/**
	 * Remove expired entries.
	 */
    private static void checkDenyLoginsNames() {
		final long ts = System.currentTimeMillis();
		final List<String> rem = new LinkedList<String>();
		synchronized (denyLoginNames) {
			for (final Entry<String, Long> entry : denyLoginNames.entrySet()){
				if (entry.getValue().longValue() < ts)  rem.add(entry.getKey());
			}
			for (final String name : rem){
				denyLoginNames.remove(name);
			}
		}
	}
    
    /**
     * Allow login (remove from deny login map).
     * @param playerName
     * @return If player was denied to login.
     */
    public static boolean allowLogin(String playerName){
    	playerName = playerName.trim().toLowerCase();
    	final Long time = denyLoginNames.remove(playerName);
    	if (time == null) return false;
    	return System.currentTimeMillis() <= time;
    }
    
	/**
	 * Deny the player to login. This will also remove expired entries.
	 * @param playerName
	 * @param duration Duration from now on, in milliseconds.
	 */
	public static void denyLogin(String playerName, long duration){
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
	
	/**
	 * Check if player is denied to login right now. 
	 * @param playerName
	 * @return
	 */
	public static boolean isLoginDenied(String playerName){
		return isLoginDenied(playerName, System.currentTimeMillis());
	}
	
	public static String[] getLoginDeniedPlayers() {
		checkDenyLoginsNames();
		String[] kicked = new String[denyLoginNames.size()];
		denyLoginNames.keySet().toArray(kicked);
		return kicked;
	}

	/**
	 * Check if a player is denied to login at a certain point of time.
	 * @param playerName
	 * @param currentTimeMillis
	 * @return
	 */
	public static boolean isLoginDenied(String playerName, long time) {
		playerName = playerName.trim().toLowerCase();
		final Long oldTs = denyLoginNames.get(playerName);
		if (oldTs == null) return false; 
		else return time < oldTs.longValue();
	}
	
	/**
	 * Convenience method, delegates to 
	 * @return
	 */
	public static NoCheatPlusAPI getAPI() {
		return (NoCheatPlusAPI) Bukkit.getPluginManager().getPlugin("NoCheatPlus");
	}
	
	/**
	 * Send all players with the nocheatplus.admin.notify permission a message.
	 * This is likely to be more efficient than iterating over all players and
	 * checking their permissions. However this only updates permissions with
	 * login and world changes currently.
	 * 
	 * @param message
	 * @return Number of players messaged.
	 */
	public static int sendAdminNotifyMessage(final String message){
		final Set<String> names = nameSetPerms.getPlayers(Permissions.ADMINISTRATION_NOTIFY);
		if (names == null) return 0;
		int done = 0;
		final Server server = Bukkit.getServer();
		for (final String name : names){
			final Player player = server.getPlayerExact(name);
			if (player != null){
				player.sendMessage(message);
				done ++;
			}
		}
		return done;
	}

	/** The event listeners. */
    private final List<Listener> listeners       = new ArrayList<Listener>();
    
    /** Components that need notification on reloading.
     * (Kept here, for if during runtime some might get added.)*/
    private final List<INotifyReload> notifyReload = new LinkedList<INotifyReload>();

    /** Is the configuration outdated? */
    private boolean              configOutdated  = false;

    /** Is a new update available? */
    private boolean              updateAvailable = false;
    
    /** Player data future stuff. */
    protected final DataManager dataMan = new DataManager();
    
	/**
	 * Commands that were changed for protecting them against tab complete or
	 * use.
	 */
	protected List<CommandProtectionEntry> changedCommands = null;
	
	
	private final ListenerManager listenerManager = new ListenerManager(this, false);
	
	private boolean manageListeners = true;
	
	protected final List<PermStateReceiver> permStateReceivers = new ArrayList<PermStateReceiver>();

	protected Metrics metrics = null;
	
	/**
	 * Interfaces checked for managed listeners: IHaveMethodOrder (method), ComponentWithName (tag)<br>
	 */
	@Override
	public void addComponent(final Object obj) {
		if (obj instanceof Listener) {
			addListener((Listener) obj);
		}
		if (obj instanceof INotifyReload) {
			notifyReload.add((INotifyReload) obj);
			if (obj instanceof INeedConfig) {
				((INeedConfig) obj).onReload();
			}
		}
		if (obj instanceof PermStateReceiver){
			// No immediate update done.
			permStateReceivers.add((PermStateReceiver) obj);
		}
		dataMan.addComponent(obj);
	}
	
	/**
	 * Interfaces checked for managed listeners: IHaveMethodOrder (method), ComponentWithName (tag)<br>
	 * @param listener
	 */
	protected void addListener(final Listener listener) {
		if (manageListeners){
			String tag = "NoCheatPlus";
			if (listener instanceof ComponentWithName){
				tag = ((ComponentWithName) listener).getComponentName();
			}
			listenerManager.registerAllEventHandlers(listener, tag);
			listeners.add(listener);
		}
		else{
			Bukkit.getPluginManager().registerEvents(listener, this);
			if (listener instanceof IHaveMethodOrder){
				// TODO: Might log the order too, might prevent registration ?
				// TODO: Alternative: queue listeners and register after startup (!)
				LogUtil.logWarning("[NoCheatPlus] Listener demands registration order, but listeners are not managed: " + listener.getClass().getName());
			}
		}
	}
	
	/**
	 * Test if NCP uses the ListenerManager at all.
	 * @return If so.
	 */
	public boolean doesManageListeners(){
		return manageListeners;
	}

	@Override
	public void removeComponent(final Object obj) {
		if (obj instanceof Listener){
			listeners.remove(obj);
			listenerManager.remove((Listener) obj);
		}
		if (obj instanceof PermStateReceiver){
			permStateReceivers.remove((PermStateReceiver) obj);
		}
		if (obj instanceof INotifyReload) {
			notifyReload.remove(obj);
		}
		dataMan.removeComponent(obj);
	}
    
    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        /*
         *  ____  _           _     _      
         * |  _ \(_)___  __ _| |__ | | ___ 
         * | | | | / __|/ _` | '_ \| |/ _ \
         * | |_| | \__ \ (_| | |_) | |  __/
         * |____/|_|___/\__,_|_.__/|_|\___|
         */
        
    	final boolean verbose = ConfigManager.getConfigFile().getBoolean(ConfPaths.LOGGING_DEBUG);
    	
		// Remove listener references.
    	if (verbose){
    		if (listenerManager.hasListenerMethods()) LogUtil.logInfo("[NoCheatPlus] Cleanup ListenerManager...");
    		else LogUtil.logInfo("[NoCheatPlus] (ListenerManager empty...)");
    	}
		listenerManager.setRegisterDirectly(false);
		listenerManager.clear();
        
        // Stop the tickTask.
		if (verbose) LogUtil.logInfo("[NoCheatPlus] Stop TickTask...");
        TickTask.setLocked(true);
        TickTask.purge();
        TickTask.cancel();
        
		// Stop metrics task.
		if (metrics != null){
			if (verbose) LogUtil.logInfo("[NoCheatPlus] Stop Metrics...");
			metrics.cancel();
			metrics = null;
		}
        
        // Stop the lag measuring task.
		if (verbose) LogUtil.logInfo("[NoCheatPlus] Stop LagMeasureTask...");
        LagMeasureTask.cancel();
        
        // Just to be sure nothing gets left out.
        if (verbose) LogUtil.logInfo("[NoCheatPlus] Stop all remaining tasks...");
        getServer().getScheduler().cancelTasks(this);

        if (verbose) LogUtil.logInfo("[NoCheatPlus] Cleanup some mappings...");
        // Remove listeners.
        listeners.clear();
        // Remove config listeners.
        notifyReload.clear();
        // World specific permissions.
		permStateReceivers.clear();

		// More cleanup.
		if (verbose) LogUtil.logInfo("[NoCheatPlus] Cleanup DataManager...");
		dataMan.onDisable();

		// Clear command changes list (compatibility issues with NPCs, leads to rrecalculation of perms).
		changedCommands.clear();
		changedCommands = null;
//		// Restore changed commands.
//		if (verbose) LogUtil.logInfo("[NoCheatPlus] Undo command changes...");
//		undoCommandChanges();
		
		// Cleanup the configuration manager.
		if (verbose) LogUtil.logInfo("[NoCheatPlus] Cleanup ConfigManager...");
		ConfigManager.cleanup();

		// Tell the server administrator the we finished unloading NoCheatPlus.
		if (verbose) LogUtil.logInfo("[NoCheatPlus] All cleanup done.");
		final PluginDescriptionFile pdfFile = getDescription();
		LogUtil.logInfo("[NoCheatPlus] Version " + pdfFile.getVersion() + " is disabled.");
	}

	/**
	 * Does not undo 100%, but restore old permission, permission-message, label (unlikely to be changed), permission default.
	 * @deprecated Leads to compatibility issues with NPC plugins such as Citizens 2, due to recalculation of permissions (specifically during disabling).
	 */
	public void undoCommandChanges() {
		if (changedCommands != null){
			while (!changedCommands.isEmpty()){
				final CommandProtectionEntry entry = changedCommands.remove(changedCommands.size() - 1);
				entry.restore();
			}
			changedCommands = null;
		}
	}
	
	protected void setupCommandProtection() {
		final List<CommandProtectionEntry> changedCommands = PermissionUtil.protectCommands(
				Arrays.asList("plugins", "version", "icanhasbukkit"), "nocheatplus.feature.command", false);
		if (this.changedCommands == null) this.changedCommands = changedCommands;
		else this.changedCommands.addAll(changedCommands);
	}

	/* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        /*
         *  _____             _     _      
         * | ____|_ __   __ _| |__ | | ___ 
         * |  _| | '_ \ / _` | '_ \| |/ _ \
         * | |___| | | | (_| | |_) | |  __/
         * |_____|_| |_|\__,_|_.__/|_|\___|
         */
    	
        // Read the configuration files.
        ConfigManager.init(this);
        
        // Allow entries to TickTask (just in case).
        TickTask.setLocked(false);
        
        // 
        final ConfigFile config = ConfigManager.getConfigFile();
        BlockProperties.applyConfig(config, ConfPaths.COMPATIBILITY_BLOCKS); // Temp probably,

		// List the events listeners and register.
		manageListeners = config.getBoolean(ConfPaths.MISCELLANEOUS_MANAGELISTENERS);
		if (manageListeners) {
			listenerManager.setRegisterDirectly(true);
			listenerManager.registerAllWithBukkit();
		}
		else{
			// Just for safety.
			listenerManager.setRegisterDirectly(false);
			listenerManager.clear();
		}
		addComponent(nameSetPerms);
		addListener(getCoreListener());
        for (final Object obj : new Object[]{
        	NCPExemptionManager.getListener(),
        	dataMan,
        	new BlockBreakListener(),
        	new BlockInteractListener(),
        	new BlockPlaceListener(),
        	new ChatListener(),
        	new CombinedListener(),
        	// Do mind registration order: Combined must come before Fight.
        	new FightListener(),
        	new InventoryListener(),
        	new MovingListener(),
        	new INotifyReload() {
			@Override
			public void onReload() {
				// Only for reloading, not INeedConfig.
				BlockProperties.init();
				final ConfigFile config = ConfigManager.getConfigFile();
				BlockProperties.applyConfig(config, ConfPaths.COMPATIBILITY_BLOCKS);
				undoCommandChanges();
				if (config.getBoolean(ConfPaths.MISCELLANEOUS_PROTECTPLUGINS)) setupCommandProtection();
			}
		},
        }){
        	addComponent(obj);
        }
        
        // Register the commands handler.
        PluginCommand command = getCommand("nocheatplus");
        CommandHandler commandHandler = new CommandHandler(this, notifyReload);
        command.setExecutor(commandHandler);
        // (CommandHandler is TabExecutor.)

        // Set up a task to monitor server lag.
        LagMeasureTask.start(this);
        
        // Set up the tick task.
        TickTask.start(this);
        
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				dataMan.checkExpiration();
			}
		}, 1207, 1207);

        
        // Setup the graphs, plotters and start Metrics.
        if (config.getBoolean(ConfPaths.MISCELLANEOUS_REPORTTOMETRICS)) {
            MetricsData.initialize();
            try {
                this.metrics = new Metrics(this);
                final Graph checksFailed = metrics.createGraph("Checks Failed");
                for (final CheckType type : CheckType.values())
                    if (type.getParent() != null)
                        checksFailed.addPlotter(new Plotter(type.name()) {

                            @Override
                            public int getValue() {
                                return MetricsData.getFailed(type);
                            }
                        });
                final Graph serverTicks = metrics.createGraph("Server Ticks");
                final int[] ticksArray = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18,
                        19, 20};
                for (final int ticks : ticksArray)
                    serverTicks.addPlotter(new Plotter(ticks + " tick(s)") {

                        @Override
                        public int getValue() {
                            return MetricsData.getTicks(ticks);
                        }
                    });
                metrics.start();
            } catch (final Exception e) {
            	LogUtil.logWarning("[NoCheatPlus] Failed to initialize metrics:");
            	LogUtil.logWarning(e);
            	if (metrics != null){
            		metrics.cancel();
            		metrics = null;
            	}
            }
        }

//        if (config.getBoolean(ConfPaths.MISCELLANEOUS_CHECKFORUPDATES)){
//            // Is a new update available?
//        	final int timeout = config.getInt(ConfPaths.MISCELLANEOUS_UPDATETIMEOUT, 4) * 1000;
//        	getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
//				@Override
//				public void run() {
//					updateAvailable = Updates.checkForUpdates(getDescription().getVersion(), timeout);
//				}
//			});
//        }

        // Is the configuration outdated?
        configOutdated = Updates.isConfigOutdated(DefaultConfig.buildNumber, config);
        
        // Debug information about unknown blocks.
        // (Probably removed later.)
        BlockProperties.dumpBlocks(config.getBoolean(ConfPaths.BLOCKBREAK_FASTBREAK_DEBUG, false) || config.getBoolean(ConfPaths.BLOCKBREAK, false));
        
		if (config.getBoolean(ConfPaths.MISCELLANEOUS_PROTECTPLUGINS)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					setupCommandProtection();
				}
			}); 
		}

		// Tell the server administrator that we finished loading NoCheatPlus now.
		LogUtil.logInfo("[NoCheatPlus] Version " + getDescription().getVersion() + " is enabled.");
	}

//    public void onPlayerJoinLow(final PlayerJoinEvent event) {
//        /*
//         *  ____  _                             _       _       
//         * |  _ \| | __ _ _   _  ___ _ __      | | ___ (_)_ __  
//         * | |_) | |/ _` | | | |/ _ \ '__|  _  | |/ _ \| | '_ \ 
//         * |  __/| | (_| | |_| |  __/ |    | |_| | (_) | | | | |
//         * |_|   |_|\__,_|\__, |\___|_|     \___/ \___/|_|_| |_|
//         *                |___/                                 
//         */
//        // Change the NetServerHandler of the player if requested in the configuration.
//        final ConfigFile configFile = ConfigManager.getConfigFile();
//        if (configFile.getBoolean(ConfPaths.MISCELLANEOUS_NOMOVEDTOOQUICKLY_ENABLED, false))
//            NCPNetServerHandler.changeNetServerHandler(event.getPlayer(),
//                    configFile.getBoolean(ConfPaths.MISCELLANEOUS_NOMOVEDTOOQUICKLY_USEPROXY, false));
//    }

    
    /**
     * Send block codes to the player according to allowed or disallowed client-mods or client-mod features.
     * @param player
     */
    protected void checkModsMessage(Player player) {
        String message = "";

        // Check if we allow all the client mods.
        final boolean allowAll = ConfigManager.getConfigFile().getBoolean(ConfPaths.MISCELLANEOUS_ALLOWCLIENTMODS);

        // Allow Rei's Minimap's cave mode.
        if (allowAll || player.hasPermission(Permissions.REI_CAVE))
            message += "§0§0§1§e§f";

        // Allow Rei's Minimap's radar.
        if (allowAll || player.hasPermission(Permissions.REI_RADAR))
            message += "§0§0§2§3§4§5§6§7§e§f";

        // If all the client mods are allowed, no need to go any further.
        if (allowAll) {
            if (!message.equals(""))
                player.sendMessage(message);
            return;
        }

        // Disable Zombe's fly mod.
        if (!player.hasPermission(Permissions.ZOMBE_FLY))
            message += "§f §f §1 §0 §2 §4";

        // Disable Zombe's noclip.
        if (!player.hasPermission(Permissions.ZOMBE_NOCLIP))
            message += "§f §f §4 §0 §9 §6";

        // Disable Zombe's cheat.
        if (!player.hasPermission(Permissions.ZOMBE_CHEAT))
            message += "§f §f §2 §0 §4 §8";

        // Disable CJB's fly mod.
        if (!player.hasPermission(Permissions.CJB_FLY))
            message += "§3 §9 §2 §0 §0 §1";

        // Disable CJB's xray.
        if (!player.hasPermission(Permissions.CJB_XRAY))
            message += "§3 §9 §2 §0 §0 §2";

        // Disable CJB's radar.
        if (!player.hasPermission(Permissions.CJB_RADAR))
            message += "§3 §9 §2 §0 §0 §3";

        // Disable Minecraft AutoMap's ores.
        if (!player.hasPermission(Permissions.MINECRAFTAUTOMAP_ORES))
            message += "§0§0§1§f§e";

        // Disable Minecraft AutoMap's cave mode.
        if (!player.hasPermission(Permissions.MINECRAFTAUTOMAP_CAVE))
            message += "§0§0§2§f§e";

        // Disable Minecraft AutoMap's radar.
        if (!player.hasPermission(Permissions.MINECRAFTAUTOMAP_RADAR))
            message += "§0§0§3§4§5§6§7§8§f§e";

        // Disable Smart Moving's climbing.
        if (!player.hasPermission(Permissions.SMARTMOVING_CLIMBING))
            message += "§0§1§0§1§2§f§f";

        // Disable Smart Moving's climbing.
        if (!player.hasPermission(Permissions.SMARTMOVING_SWIMMING))
            message += "§0§1§3§4§f§f";

        // Disable Smart Moving's climbing.
        if (!player.hasPermission(Permissions.SMARTMOVING_CRAWLING))
            message += "§0§1§5§f§f";

        // Disable Smart Moving's climbing.
        if (!player.hasPermission(Permissions.SMARTMOVING_SLIDING))
            message += "§0§1§6§f§f";

        // Disable Smart Moving's climbing.
        if (!player.hasPermission(Permissions.SMARTMOVING_JUMPING))
            message += "§0§1§8§9§a§b§f§f";

        // Disable Smart Moving's climbing.
        if (!player.hasPermission(Permissions.SMARTMOVING_FLYING))
            message += "§0§1§7§f§f";

        if (!message.equals(""))
            player.sendMessage(message);
    }

	/**
	 * Quick solution to hide the listener methods, expect refactoring.
	 * @return
	 */
	private Listener getCoreListener() {
		return new NCPListener() {
			@SuppressWarnings("unused")
			@EventHandler(priority = EventPriority.HIGHEST)
			public void onPlayerLogin(final PlayerLoginEvent event) {
				// (HGHEST to give other plugins the possibility to add
				// permissions or allow the player).
				if (event.getResult() != Result.ALLOWED) return;
				final Player player = event.getPlayer();
				// Check if login is denied:
				checkDenyLoginsNames();
				if (player.hasPermission(Permissions.BYPASS_DENY_LOGIN)) return;
				if (isLoginDenied(player.getName())) {
					// TODO: display time for which the player is banned.
					event.setResult(Result.KICK_OTHER);
					// TODO: Make message configurable.
					event.setKickMessage("You are temporarily denied to join this server.");
				}
			}

			@SuppressWarnings("unused")
			@EventHandler(priority = EventPriority.MONITOR)
			public void onPlayerJoin(final PlayerJoinEvent event) {
				final Player player = event.getPlayer();

				updatePermStateReceivers(player);
				
				if (nameSetPerms.hasPermission(player.getName(), Permissions.ADMINISTRATION_NOTIFY)){
					// Login notifications...
					
					// Update available.
					if (updateAvailable) player.sendMessage(ChatColor.RED + "NCP: " + ChatColor.WHITE + "A new update of NoCheatPlus is available.\n" + "Download it at http://nocheatplus.org/update");
					
					// Outdated config.
					if (configOutdated) player.sendMessage(ChatColor.RED + "NCP: " + ChatColor.WHITE + "Your configuration might be outdated.\n" + "Some settings could have changed, you should regenerate it!");

				}
				checkModsMessage(player);
			}

			@SuppressWarnings("unused")
			@EventHandler(priority = EventPriority.MONITOR)
			public void onPlayerchangedWorld(final PlayerChangedWorldEvent event)
			{
				final Player player = event.getPlayer();
				updatePermStateReceivers(player);
			}

			@SuppressWarnings("unused")
			@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
			public void onPlayerKick(final PlayerKickEvent event) {
				onLeave(event.getPlayer());
			}

			@SuppressWarnings("unused")
			@EventHandler(priority = EventPriority.MONITOR)
			public void onPlayerQuitMonitor(final PlayerQuitEvent event) {
				onLeave(event.getPlayer());
			}
		};
	}
	
	protected void onLeave(final Player player) {
		for (final PermStateReceiver pr : permStateReceivers) {
			pr.removePlayer(player.getName());
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

}
