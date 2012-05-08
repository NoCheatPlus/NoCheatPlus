package fr.neatmonster.nocheatplus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.WorkaroundsListener;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakListener;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceListener;
import fr.neatmonster.nocheatplus.checks.chat.ChatListener;
import fr.neatmonster.nocheatplus.checks.fight.FightListener;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryListener;
import fr.neatmonster.nocheatplus.checks.moving.MovingListener;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;

public class NoCheatPlus extends JavaPlugin implements Listener {
    public static NoCheatPlus instance = null;

    public static boolean skipCheck() {
        return instance.lagMeasureTask == null ? false : instance.lagMeasureTask.skipCheck();
    }

    private CommandHandler commandHandler;
    private LagMeasureTask lagMeasureTask;
    private List<Listener> listeners;

    @Override
    public void onDisable() {

        final PluginDescriptionFile pdfFile = getDescription();

        if (lagMeasureTask != null) {
            lagMeasureTask.cancel();
            lagMeasureTask = null;
        }

        ConfigManager.cleanup();

        // Just to be sure nothing gets left out
        getServer().getScheduler().cancelTasks(this);

        commandHandler = null;

        System.out.println("[NoCheatPlus] version [" + pdfFile.getVersion() + "] is disabled.");
    }

    @Override
    public void onEnable() {
        instance = this;

        commandHandler = new CommandHandler();
        // Then read the configuration files
        ConfigManager.init();

        listeners = new ArrayList<Listener>();
        // Then set up the event listeners
        listeners.add(new WorkaroundsListener());
        listeners.add(new BlockBreakListener());
        listeners.add(new BlockPlaceListener());
        listeners.add(new ChatListener());
        listeners.add(new FightListener());
        listeners.add(new InventoryListener());
        listeners.add(new MovingListener());

        // Then set up a task to monitor server lag
        if (lagMeasureTask == null) {
            lagMeasureTask = new LagMeasureTask();
            lagMeasureTask.start();
        }

        // register all listeners
        for (final Listener listener : listeners)
            Bukkit.getPluginManager().registerEvents(listener, this);

        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("nocheatplus").setExecutor(commandHandler);

        ConfigManager.writeInstructions();

        // Tell the server admin that we finished loading NoCheatPlus now
        System.out.println("[NoCheatPlus] version [" + getDescription().getVersion() + "] is enabled.");
    }

    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (ConfigManager.getConfigFile().getBoolean(ConfPaths.MISCELLANEOUS_ALLOWCLIENTMODS))
            return;
        String message = "";
        // Disable Zombe's fly mod
        if (!NCPPlayer.hasPermission(player, Permissions.ZOMBE_FLY))
            message = message + "§f §f §1 §0 §2 §4";
        // Disable Zombe's noclip
        if (!NCPPlayer.hasPermission(player, Permissions.ZOMBE_NOCLIP))
            message = message + "§f §f §4 §0 §9 §6";
        // Disable Zombe's cheat
        if (!NCPPlayer.hasPermission(player, Permissions.ZOMBE_CHEAT))
            message = message + "§f §f §2 §0 §4 §8";
        // Disable CJB's fly mod
        if (!NCPPlayer.hasPermission(player, Permissions.CJB_FLY))
            message = message + "§3 §9 §2 §0 §0 §1";
        // Disable CJB's xray
        if (!NCPPlayer.hasPermission(player, Permissions.CJB_XRAY))
            message = message + "§3 §9 §2 §0 §0 §2";
        // Disable CJB's radar
        if (!NCPPlayer.hasPermission(player, Permissions.CJB_RADAR))
            message = message + "§3 §9 §2 §0 §0 §3";
        // Disable Rei's Minimap's cave mode
        if (NCPPlayer.hasPermission(player, Permissions.REI_CAVE))
            message = message + "§0§0§1§e§f";
        // Disable Rei's Minimap's radar
        if (NCPPlayer.hasPermission(player, Permissions.REI_RADAR))
            message = message + "§0§0§2§3§4§5§6§7§e§f";
        // Disable Minecraft AutoMap's ores
        if (!NCPPlayer.hasPermission(player, Permissions.MINECRAFTAUTOMAP_ORES))
            message = message + "§0§0§1§f§e";
        // Disable Minecraft AutoMap's cave mode
        if (!NCPPlayer.hasPermission(player, Permissions.MINECRAFTAUTOMAP_CAVE))
            message = message + "§0§0§2§f§e";
        // Disable Minecraft AutoMap's radar
        if (!NCPPlayer.hasPermission(player, Permissions.MINECRAFTAUTOMAP_RADAR))
            message = message + "§0§0§3§4§5§6§7§8§f§e";
        // Disable Smart Moving's climbing
        if (!NCPPlayer.hasPermission(player, Permissions.SMARTMOVING_CLIMBING))
            message = message + "§0§1§0§1§2§f§f";
        // Disable Smart Moving's climbing
        if (!NCPPlayer.hasPermission(player, Permissions.SMARTMOVING_SWIMMING))
            message = message + "§0§1§3§4§f§f";
        // Disable Smart Moving's climbing
        if (!NCPPlayer.hasPermission(player, Permissions.SMARTMOVING_CRAWLING))
            message = message + "§0§1§5§f§f";
        // Disable Smart Moving's climbing
        if (!NCPPlayer.hasPermission(player, Permissions.SMARTMOVING_SLIDING))
            message = message + "§0§1§6§f§f";
        // Disable Smart Moving's climbing
        if (!NCPPlayer.hasPermission(player, Permissions.SMARTMOVING_JUMPING))
            message = message + "§0§1§8§9§a§b§f§f";
        // Disable Smart Moving's climbing
        if (!NCPPlayer.hasPermission(player, Permissions.SMARTMOVING_FLYING))
            message = message + "§0§1§7§f§f";
        player.sendMessage(message);
    }
}
