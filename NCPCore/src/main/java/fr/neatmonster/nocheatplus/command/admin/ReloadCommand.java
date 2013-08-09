package fr.neatmonster.nocheatplus.command.admin;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.command.NoCheatPlusCommand.NCPReloadEvent;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.components.order.Order;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.StaticLogFile;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;

public class ReloadCommand extends BaseCommand {
	
	/** Components that need to be notified on reload */
	private final List<INotifyReload> notifyReload;

	public ReloadCommand(JavaPlugin plugin, List<INotifyReload> notifyReload) {
		super(plugin, "reload", Permissions.COMMAND_RELOAD);
		this.notifyReload = notifyReload;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		if (args.length != 1) return false;
        handleReloadCommand(sender);
        return true;
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
        ConfigManager.init(access);
        StaticLogFile.cleanup();
        StaticLogFile.setupLogger(new File(access.getDataFolder(), ConfigManager.getConfigFile().getString(ConfPaths.LOGGING_BACKEND_FILE_FILENAME)));
        // Remove all cached configs.
        DataManager.clearConfigs(); // There you have to add XConfig.clear() form now on.
        // Remove some checks data.
        for (final CheckType checkType : new CheckType[]{
        		CheckType.BLOCKBREAK, CheckType.FIGHT,
        }){
        	DataManager.clearData(checkType);
        }
        
        // Tell the registered listeners to adapt to new config, first sort them (!).
        Collections.sort(notifyReload, Order.cmpSetupOrder);
        for (final INotifyReload component : notifyReload){
        	component.onReload();
        }

        // Say to the other plugins that we've reloaded the configuration.
        Bukkit.getPluginManager().callEvent(new NCPReloadEvent());

        sender.sendMessage(TAG + "Configuration reloaded!");
        final String info = "[NoCheatPlus] Configuration reloaded.";
        if (!(sender instanceof ConsoleCommandSender)) Bukkit.getLogger().info(info);
        final ConfigFile config = ConfigManager.getConfigFile();
        if (config.getBoolean(ConfPaths.LOGGING_ACTIVE) && config.getBoolean(ConfPaths.LOGGING_BACKEND_FILE_ACTIVE)) StaticLogFile.fileLogger.info(info);
    }

}
