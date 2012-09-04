package fr.neatmonster.nocheatplus.command;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakConfig;
import fr.neatmonster.nocheatplus.checks.blockinteract.BlockInteractConfig;
import fr.neatmonster.nocheatplus.checks.blockplace.BlockPlaceConfig;
import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.fight.FightConfig;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.command.CommandHandler.NCPReloadEvent;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.Permissions;

public class ReloadCommand extends NCPCommand {
	
	/** Components that need to be notified on reload */
	private final Collection<INotifyReload> notifyReload;

	public ReloadCommand(NoCheatPlus plugin, Collection<INotifyReload> notifyReload) {
		super(plugin, "reload", Permissions.ADMINISTRATION_RELOAD);
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
        ConfigManager.init(plugin);
        BlockBreakConfig.clear();
        BlockInteractConfig.clear();
        BlockPlaceConfig.clear();
        ChatConfig.clear();
        FightConfig.clear();
        InventoryConfig.clear();
        MovingConfig.clear();
        
        // Tell the plugin to adapt to new config.
        for (INotifyReload component : notifyReload){
        	component.onReload();
        }

        // Say to the other plugins that we've reloaded the configuration.
        Bukkit.getPluginManager().callEvent(new NCPReloadEvent());

        sender.sendMessage(TAG + "Configuration reloaded!");
    }

}
