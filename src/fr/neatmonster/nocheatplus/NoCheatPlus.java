package fr.neatmonster.nocheatplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.Workarounds;
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
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.metrics.Metrics;
import fr.neatmonster.nocheatplus.metrics.Metrics.Graph;
import fr.neatmonster.nocheatplus.metrics.Metrics.Plotter;
import fr.neatmonster.nocheatplus.metrics.MetricsData;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.BlockUtils;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;
import fr.neatmonster.nocheatplus.utilities.TickTask;

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
public class NoCheatPlus extends JavaPlugin implements Listener {
	
	/** Lower case player name to milliseconds point of time of release */
	private static final Map<String, Long> denyLoginNames = Collections.synchronizedMap(new HashMap<String, Long>());
	
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

	/**
	 * Check if a player is denied to login at a certain point of time.
	 * @param playerName
	 * @param currentTimeMillis
	 * @return
	 */
	public static boolean isLoginDenied(String playerName, long currentTimeMillis) {
		playerName = playerName.trim().toLowerCase();
		final Long oldTs = denyLoginNames.get(playerName);
		if (oldTs == null) return false; 
		else return System.currentTimeMillis() < oldTs.longValue();
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
    
    /**
     * Convenience method to add to listeners and notifyReload lists.
     * @param listener
     */
    private void addListener(final Listener listener){
    	Bukkit.getPluginManager().registerEvents(listener, this);
    	listeners.add(listener);
    	if (listener instanceof INotifyReload){
    		notifyReload.add((INotifyReload) listener);
    	}
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
        final PluginDescriptionFile pdfFile = getDescription();
        
        // Stop the tickTask.
        TickTask.cancel();
        
        // Stop the lag measuring task.
        LagMeasureTask.cancel();
        
        // Just to be sure nothing gets left out.
        getServer().getScheduler().cancelTasks(this);

        // Remove listeners.
        listeners.clear();
        
        // Remove config listeners.
        notifyReload.clear();
        
        // Cleanup the configuration manager.
        ConfigManager.cleanup();

        // Tell the server administrator the we finished unloading NoCheatPlus.
        System.out.println("[NoCheatPlus] Version " + pdfFile.getVersion() + " is disabled.");
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

        // List the events listeners and register.
        Bukkit.getPluginManager().registerEvents(this, this);
        for (final Listener listener : new Listener[]{
        	new BlockBreakListener(),
        	new BlockInteractListener(),
        	new BlockPlaceListener(),
        	new ChatListener(),
        	new CombinedListener(),
        	new FightListener(),
        	new InventoryListener(),
        	new MovingListener(),
        	new Workarounds(),
        }){
        	addListener(listener);
        }
        
        // Register the commands handler.
        getCommand("nocheatplus").setExecutor(new CommandHandler(this, notifyReload));

        // Set up a task to monitor server lag.
        LagMeasureTask.start(this);
        
        // Set up the tick task.
        TickTask.start(this);

        ConfigFile config = ConfigManager.getConfigFile();
        
        // Setup the graphs, plotters and start Metrics.
        if (config.getBoolean(ConfPaths.MISCELLANEOUS_REPORTTOMETRICS)) {
            MetricsData.initialize();
            try {
                final Metrics metrics = new Metrics(this);
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
            } catch (final Exception e) {}
        }

        if (config.getBoolean(ConfPaths.MISCELLANEOUS_CHECKFORUPDATES)){
            // Is a new update available?
        	BufferedReader bufferedReader = null;
        	updateAvailable = false;
            try {
                final int currentVersion = Integer.parseInt(getDescription().getVersion().split("-b")[1]);
                final URL url = new URL("http://nocheatplus.org:8080/job/NoCheatPlus/lastSuccessfulBuild/api/json");
                final URLConnection connection = url.openConnection();
                connection.setReadTimeout(config.getInt(ConfPaths.MISCELLANEOUS_READTIMEOUT, 4) * 1000);
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line, content = "";
                while ((line = bufferedReader.readLine()) != null)
                    content += line;
                final int jenkinsVersion = Integer.parseInt(content.split("\"number\":")[1].split(",")[0]);
                updateAvailable = currentVersion < jenkinsVersion;
            } catch (final Exception e) {}
            finally{
            	if (bufferedReader != null) try{bufferedReader.close();}catch (IOException e){};
            }
        }

        // Is the configuration outdated?
        try {
            final int currentVersion = Integer.parseInt(getDescription().getVersion().split("-b")[1]);
            final int configurationVersion = Integer.parseInt(
            		config.options().header().split("-b")[1].split("\\.")[0]);
            if (currentVersion > configurationVersion)
                configOutdated = true;
        } catch (final Exception e) {}
        
        // Debug information about unknown blocks.
        // (Probably removed later.)
        BlockUtils.dumpBlocks(config.getBoolean(ConfPaths.BLOCKBREAK_FASTBREAK_DEBUG, false));

        // Tell the server administrator that we finished loading NoCheatPlus now.
        System.out.println("[NoCheatPlus] Version " + getDescription().getVersion() + " is enabled.");
    }

    public void onPlayerJoinLow(final PlayerJoinEvent event) {
        /*
         *  ____  _                             _       _       
         * |  _ \| | __ _ _   _  ___ _ __      | | ___ (_)_ __  
         * | |_) | |/ _` | | | |/ _ \ '__|  _  | |/ _ \| | '_ \ 
         * |  __/| | (_| | |_| |  __/ |    | |_| | (_) | | | | |
         * |_|   |_|\__,_|\__, |\___|_|     \___/ \___/|_|_| |_|
         *                |___/                                 
         */
        // Change the NetServerHandler of the player if requested in the configuration.
        final ConfigFile configFile = ConfigManager.getConfigFile();
        if (configFile.getBoolean(ConfPaths.MISCELLANEOUS_NOMOVEDTOOQUICKLY_ENABLED, false))
            NCPNetServerHandler.changeNetServerHandler(event.getPlayer(),
                    configFile.getBoolean(ConfPaths.MISCELLANEOUS_NOMOVEDTOOQUICKLY_USEPROXY, false));
    }

    /**
     * This event handler is used to send all the disabling messages to the client.
     * 
     * @param event
     *            the event handled
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerJoinMonitor(final PlayerJoinEvent event) {
        /*
         *  ____  _                             _       _       
         * |  _ \| | __ _ _   _  ___ _ __      | | ___ (_)_ __  
         * | |_) | |/ _` | | | |/ _ \ '__|  _  | |/ _ \| | '_ \ 
         * |  __/| | (_| | |_| |  __/ |    | |_| | (_) | | | | |
         * |_|   |_|\__,_|\__, |\___|_|     \___/ \___/|_|_| |_|
         *                |___/                                 
         */
        final Player player = event.getPlayer();

        // Send a message to the player if a new update is available.
        if (updateAvailable && player.hasPermission(Permissions.ADMINISTRATION_NOTIFY))
            player.sendMessage(ChatColor.RED + "NCP: " + ChatColor.WHITE
                    + "A new update of NoCheatPlus is available.\n" + "Download it at http://nocheatplus.org/update");

        // Send a message to the player if the configuration is outdated.
        if (configOutdated && player.hasPermission(Permissions.ADMINISTRATION_NOTIFY))
            player.sendMessage(ChatColor.RED + "NCP: " + ChatColor.WHITE + "Your configuration file is outdated.\n"
                    + "Some settings might have changed, you should regenerate it!");

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
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerLogin(final PlayerLoginEvent event){
    	// (HGHEST to give other plugins the possibility to add permissions or allow the player).
    	if (event.getResult() != Result.ALLOWED) return;
    	final Player player = event.getPlayer();
    	// Check if login is denied:
    	checkDenyLoginsNames();
    	if (player.hasPermission(Permissions.BYPASS_DENY_LOGIN)) return;
    	if (isLoginDenied(player.getName())){
    		// TODO: display time for which the player is banned.
    		event.setResult(Result.KICK_OTHER);
    		// TODO: Make message configurable.
    		event.setKickMessage("You are temporarily denied to join server.");
    	}
    }
    
}
