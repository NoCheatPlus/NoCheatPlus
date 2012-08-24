package fr.neatmonster.nocheatplus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.checks.ViolationHistory.ViolationLevel;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakConfig;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractConfig;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceConfig;
import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.fight.FightConfig;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * MM'""""'YMM                                                        dP 
 * M' .mmm. `M                                                        88 
 * M  MMMMMooM .d8888b. 88d8b.d8b. 88d8b.d8b. .d8888b. 88d888b. .d888b88 
 * M  MMMMMMMM 88'  `88 88'`88'`88 88'`88'`88 88'  `88 88'  `88 88'  `88 
 * M. `MMM' .M 88.  .88 88  88  88 88  88  88 88.  .88 88    88 88.  .88 
 * MM.     .dM `88888P' dP  dP  dP dP  dP  dP `88888P8 dP    dP `88888P8 
 * MMMMMMMMMMM                                                           
 * 
 * M""MMMMM""MM                         dP dP                   
 * M  MMMMM  MM                         88 88                   
 * M         `M .d8888b. 88d888b. .d888b88 88 .d8888b. 88d888b. 
 * M  MMMMM  MM 88'  `88 88'  `88 88'  `88 88 88ooood8 88'  `88 
 * M  MMMMM  MM 88.  .88 88    88 88.  .88 88 88.  ... 88       
 * M  MMMMM  MM `88888P8 dP    dP `88888P8 dP `88888P' dP       
 * MMMMMMMMMMMM                                                 
 */
/**
 * This the class handling all the commands.
 */
public class CommandHandler implements CommandExecutor {

    /**
     * The event triggered when NoCheatPlus configuration is reloaded.
     */
    public static class NCPReloadEvent extends Event {

        /** The handlers list. */
        private static final HandlerList handlers = new HandlerList();

        /**
         * Gets the handler list.
         * 
         * @return the handler list
         */
        public static HandlerList getHandlerList() {
            return handlers;
        }

        /* (non-Javadoc)
         * @see org.bukkit.event.Event#getHandlers()
         */
        @Override
        public HandlerList getHandlers() {
            return handlers;
        }
    }

    /** The prefix of every message sent by NoCheatPlus. */
    private static final String TAG = ChatColor.RED + "NCP: " + ChatColor.WHITE;

    /** The plugin. */
    private final NoCheatPlus   plugin;

    /**
     * Instantiates a new command handler.
     * 
     * @param plugin
     *            the instance of NoCheatPlus
     */
    public CommandHandler(final NoCheatPlus plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle the '/nocheatplus info' command.
     * 
     * @param sender
     *            the sender
     * @param playerName
     *            the player name
     * @return true, if successful
     */
    private void handleInfoCommand(final CommandSender sender, final String playerName) {
        final Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            final TreeMap<Long, ViolationLevel> violations = ViolationHistory.getHistory(player).getViolationLevels();
            if (violations.size() > 0) {
                sender.sendMessage(TAG + "Displaying " + playerName + "'s violations...");
                for (final long time : violations.descendingKeySet()) {
                    final ViolationLevel violationLevel = violations.get(time);
                    final String[] parts = violationLevel.check.split("\\.");
                    final String check = parts[parts.length - 1];
                    final String parent = parts[parts.length - 2];
                    final double VL = Math.round(violationLevel.VL);
                    sender.sendMessage(TAG + "[" + dateFormat.format(new Date(time)) + "] (" + parent + ".)" + check
                            + " VL " + VL);
                }
            } else
                sender.sendMessage(TAG + "Displaying " + playerName + "'s violations... nothing to display.");
        } else {
            sender.sendMessage(TAG + "404 Not Found");
            sender.sendMessage(TAG + "The requested player was not found on this server.");
        }
    }

    /**
     * Handle the '/nocheatplus reload' command.
     * 
     * @param sender
     *            the sender
     * @return true, if successful
     */
    private void handleReloadCommand(final CommandSender sender) {
        sender.sendMessage(TAG + "Reloading configuration...");

        // Do the actual reload.
        ConfigManager.cleanup();
        ConfigManager.init(plugin);
        BlockBreakConfig.clear();
        BlockInteractConfig.clear();
        BlockPlaceConfig.clear();
        ChatConfig.clear();
        FightConfig.clear();
        InventoryConfig.clear();
        MovingConfig.clear();

        // Say to the other plugins that we've reloaded the configuration.
        Bukkit.getPluginManager().callEvent(new NCPReloadEvent());

        sender.sendMessage(TAG + "Configuration reloaded!");
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel,
            final String[] args) {
        /*
         *   ____                                          _ 
         *  / ___|___  _ __ ___  _ __ ___   __ _ _ __   __| |
         * | |   / _ \| '_ ` _ \| '_ ` _ \ / _` | '_ \ / _` |
         * | |__| (_) | | | | | | | | | | | (_| | | | | (_| |
         *  \____\___/|_| |_| |_|_| |_| |_|\__,_|_| |_|\__,_|
         */
        // Not our command, how did it get here?
        if (!command.getName().equalsIgnoreCase("nocheatplus"))
            return false;

        final boolean protectPlugins = ConfigManager.getConfigFile().getBoolean(ConfPaths.MISCELLANEOUS_PROTECTPLUGINS);

        if (args.length == 2 && args[0].equalsIgnoreCase("info")
                && sender.hasPermission(Permissions.ADMINISTRATION_INFO))
            // Info command was used.
            handleInfoCommand(sender, args[1]);
        else if (args.length == 1 && args[0].equalsIgnoreCase("reload")
                && sender.hasPermission(Permissions.ADMINISTRATION_RELOAD))
            // Reload command was used.
            handleReloadCommand(sender);
        else if (protectPlugins && !sender.hasPermission(Permissions.ADMINISTRATION_INFO)
                && !sender.hasPermission(Permissions.ADMINISTRATION_RELOAD))
            sender.sendMessage("Unknown command. Type \"help\" for help.");
        else
            return false;
        return true;
    }
}
