package fr.neatmonster.nocheatplus.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.command.NCPCommand;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.permissions.Permissions;

public class NCPVersionCommand extends NCPCommand{

	public NCPVersionCommand(NoCheatPlus plugin) {
		super(plugin, "version", Permissions.ADMINISTRATION_VERSION, new String[]{"versions", "ver"});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		final MCAccess mc = NCPAPIProvider.getNoCheatPlusAPI().getMCAccess();
		sender.sendMessage(new String[]{
				"---- Version information ----",
				"#### Server ####",
				Bukkit.getServer().getVersion(),
				"#### NoCheatPlus ####",
				"Plugin: " + plugin.getDescription().getVersion(),
				"MCAccess: " + mc.getMCVersion() + " / " + mc.getServerVersionTag(),

				});
		return true;
	}

}
