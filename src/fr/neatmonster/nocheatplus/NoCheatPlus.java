package fr.neatmonster.nocheatplus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ExecuteActionsEvent;
import fr.neatmonster.nocheatplus.checks.Workarounds;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakListener;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractListener;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceListener;
import fr.neatmonster.nocheatplus.checks.chat.ChatListener;
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
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;

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

        // Stop the lag measuring task.
        LagMeasureTask.cancel();

        // Remove listeners.
        listeners.clear();
        
        // Remove config listeners.
        notifyReload.clear();
        
        // Cleanup the configuration manager.
        ConfigManager.cleanup();

        // Just to be sure nothing gets left out.
        getServer().getScheduler().cancelTasks(this);

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

        // List the events listeners.
        listeners.clear();
        for (final Listener listener : new Listener[]{
        	new BlockBreakListener(),
        	new BlockInteractListener(),
        	new BlockPlaceListener(),
        	new ChatListener(),
        	new FightListener(),
        	new InventoryListener(),
        	new MovingListener(),
        	new Workarounds(),
        }){
        	addListener(listener);
        }

        // Set up a task to monitor server lag.
        LagMeasureTask.start(this);

        // Register all listeners.
        for (final Listener listener : listeners)
            Bukkit.getPluginManager().registerEvents(listener, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        // Register the commands handler.
        getCommand("nocheatplus").setExecutor(new CommandHandler(this, notifyReload));

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
            try {
                final int currentVersion = Integer.parseInt(getDescription().getVersion().split("-b")[1]);
                final URL url = new URL("http://nocheatplus.org:8080/job/NoCheatPlus/lastSuccessfulBuild/api/json");
                final URLConnection connection = url.openConnection();
                connection.setReadTimeout(config.getInt(ConfPaths.MISCELLANEOUS_READTIMEOUT, 4) * 1000);
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String content = "", line = "";
                while ((line = bufferedReader.readLine()) != null)
                    content += line;
                bufferedReader.close();
                final int jenkinsVersion = Integer.parseInt(content.split("\"number\":")[1].split(",")[0]);
                updateAvailable = currentVersion < jenkinsVersion;
            } catch (final Exception e) {}
        }

        // Is the configuration outdated?
        try {
            final int currentVersion = Integer.parseInt(getDescription().getVersion().split("-b")[1]);
            final int configurationVersion = Integer.parseInt(config.options().header()
                    .split("-b")[1].split("\\.")[0]);
            if (currentVersion > configurationVersion)
                configOutdated = true;
        } catch (final Exception e) {}

        // Tell the server administrator that we finished loading NoCheatPlus now.
        System.out.println("[NoCheatPlus] Version " + getDescription().getVersion() + " is enabled.");
    }

    /**
     * This event handler is used to execute the actions when a violation is detected.
     * 
     * @param event
     *            the event handled
     */
    @EventHandler(
            priority = EventPriority.LOWEST)
    final void onExecuteActions(final ExecuteActionsEvent event) {
        /*
         *  _____                     _            _        _   _                 
         * | ____|_  _____  ___ _   _| |_ ___     / \   ___| |_(_) ___  _ __  ___ 
         * |  _| \ \/ / _ \/ __| | | | __/ _ \   / _ \ / __| __| |/ _ \| '_ \/ __|
         * | |___ >  <  __/ (__| |_| | ||  __/  / ___ \ (__| |_| | (_) | | | \__ \
         * |_____/_/\_\___|\___|\__,_|\__\___| /_/   \_\___|\__|_|\___/|_| |_|___/
         */
        event.executeActions();
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
        if (configFile.getBoolean(ConfPaths.MISCELLANEOUS_NOMOVEDTOOQUICKLY_ENABLED))
            NCPNetServerHandler.changeNetServerHandler(event.getPlayer(),
                    configFile.getBoolean(ConfPaths.MISCELLANEOUS_NOMOVEDTOOQUICKLY_USEPROXY));
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
                    + "A new update of NoCheatPlus is available.\n" + "Download it at http://nocheatplus.org/update.");

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
    
}
