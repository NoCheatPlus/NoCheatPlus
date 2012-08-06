package fr.neatmonster.nocheatplus;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    private final NoCheatPlus plugin;

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
     * Handle the '/nocheatplus reload' command.
     * 
     * @param sender
     *            the sender
     * @return true, if successful
     */
    private boolean handleReloadCommand(final CommandSender sender) {
        // Players need a special permission for this.
        if (!(sender instanceof Player) || sender.hasPermission(Permissions.ADMINISTRATION_RELOAD)) {
            sender.sendMessage(ChatColor.RED + "NCP: " + ChatColor.WHITE + "Reloading configuration...");

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

            sender.sendMessage(ChatColor.RED + "NCP: " + ChatColor.WHITE + "Configuration reloaded!");
        } else
            sender.sendMessage(ChatColor.RED + "You lack the " + Permissions.ADMINISTRATION_RELOAD
                    + " permission to use 'reload'!");
        return true;
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel,
            final String[] args) {
        if (sender instanceof Player) {
            final boolean protectPlugins = ConfigManager.getConfigFile(((Player) sender).getWorld().getName())
                    .getBoolean(ConfPaths.MISCELLANEOUS_PROTECTPLUGINS);

            // Hide NoCheatPlus's commands if the player doesn't have the required permission.
            if (protectPlugins && !sender.hasPermission(Permissions.ADMINISTRATION_RELOAD)) {
                sender.sendMessage("Unknown command. Type \"help\" for help.");
                return true;
            }
        }

        boolean result = false;

        // Not our command, how did it get here?
        if (!command.getName().equalsIgnoreCase("nocheatplus") || args.length == 0)
            result = false;
        else if (args[0].equalsIgnoreCase("reload"))
            // Reload command was used.
            result = handleReloadCommand(sender);
        return result;
    }
}
