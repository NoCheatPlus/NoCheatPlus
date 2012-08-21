package fr.neatmonster.nocheatplus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
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
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.metrics.Metrics;
import fr.neatmonster.nocheatplus.metrics.Metrics.Graph;
import fr.neatmonster.nocheatplus.metrics.Metrics.Plotter;
import fr.neatmonster.nocheatplus.metrics.MetricsData;
import fr.neatmonster.nocheatplus.metrics.MetricsData.TicksPlotter;
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

    /** The time it was when NoCheatPlus has been activated. */
    public static final long     time       = System.currentTimeMillis();

    /** The listeners. */
    private final List<Listener> listeners  = new ArrayList<Listener>();

    /** The new version build number. */
    private int                  newVersion = 0;

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        final PluginDescriptionFile pdfFile = getDescription();

        // Stop the lag measuring task.
        LagMeasureTask.cancel();

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
        // Read the configuration files.
        ConfigManager.init(this);

        // List the events listeners.
        listeners.add(new BlockBreakListener());
        listeners.add(new BlockInteractListener());
        listeners.add(new BlockPlaceListener());
        listeners.add(new ChatListener());
        listeners.add(new FightListener());
        listeners.add(new InventoryListener());
        listeners.add(new MovingListener());
        listeners.add(new Workarounds());

        // Set up a task to monitor server lag.
        LagMeasureTask.start(this);

        // Register all listeners.
        for (final Listener listener : listeners)
            Bukkit.getPluginManager().registerEvents(listener, this);
        Bukkit.getPluginManager().registerEvents(this, this);

        // Register the commands handler.
        getCommand("nocheatplus").setExecutor(new CommandHandler(this));

        // Start Metrics.
        try {
            final Metrics metrics = new Metrics(this);
            final Graph eventsChecked = metrics.createGraph("Events Checked");
            final Graph checksFailed = metrics.createGraph("Checks Failed");
            final Graph violationLevels = metrics.createGraph("Violation Levels");
            for (final CheckType type : CheckType.values())
                if (type == CheckType.ALL || type.getParent() != null) {
                    eventsChecked.addPlotter(new Plotter(type.name()) {

                        @Override
                        public int getValue() {
                            final int checked = MetricsData.getChecked(type);
                            MetricsData.resetChecked(type);
                            return checked;
                        }
                    });
                    checksFailed.addPlotter(new Plotter(type.name()) {

                        @Override
                        public int getValue() {
                            final int failed = MetricsData.getFailed(type);
                            MetricsData.resetFailed(type);
                            return failed;
                        }
                    });
                    violationLevels.addPlotter(new Plotter(type.name()) {

                        @Override
                        public int getValue() {
                            final int violationLevel = (int) MetricsData.getViolationLevel(type);
                            MetricsData.resetViolationLevel(type);
                            return violationLevel;
                        }
                    });
                }
            final Graph serverTicks = metrics.createGraph("Server Ticks");
            for (int ticks = 0; ticks < 21; ticks++)
                serverTicks.addPlotter(new TicksPlotter(ticks));
            metrics.start();
        } catch (final Exception e) {}

        // Tell the server administrator that we finished loading NoCheatPlus now.
        System.out.println("[NoCheatPlus] Version " + getDescription().getVersion() + " is enabled.");

        // Check for updates.
        try {
            final Integer oldVersion = Integer.parseInt(getDescription().getVersion().split("-b")[1]);
            final URL url = new URL("http://nocheatplus.org:8080/job/NoCheatPlus/lastSuccessfulBuild/api/json");
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openConnection()
                    .getInputStream()));
            String content = "", inputLine = "";
            while ((inputLine = bufferedReader.readLine()) != null)
                content += inputLine;
            bufferedReader.close();
            final Integer newVersion = Integer.parseInt(content.split("\"number\":")[1].split(",")[0]);
            if (oldVersion < newVersion)
                this.newVersion = newVersion;
        } catch (final Exception e) {}
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
        event.executeActions();
    }

    /**
     * This event handler is used to send all the disabling messages to the client.
     * 
     * @param event
     *            the event handled
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        String message = "";

        // Display a message about the new version if relevant.
        if (newVersion > 0 && player.hasPermission(Permissions.ADMINISTRATION_NOTIFY))
            message += ChatColor.RED + "NCP: " + ChatColor.WHITE + "A new version is available! (Build #" + newVersion
                    + ".)";

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
