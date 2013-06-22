package fr.neatmonster.nocheatplus.command.admin;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class NCPVersionCommand extends BaseCommand{

	public NCPVersionCommand(JavaPlugin plugin) {
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
				"Plugin: " + access.getDescription().getVersion(),
				"MCAccess: " + mc.getMCVersion() + " / " + mc.getServerVersionTag(),
				});
		final Collection<NCPHook> hooks = NCPHookManager.getAllHooks();
		if (!hooks.isEmpty()){
			final List<String> fullNames = new LinkedList<String>();
			for (final NCPHook hook : hooks){
				fullNames.add(hook.getHookName() + " " + hook.getHookVersion());
			}
			Collections.sort(fullNames, String.CASE_INSENSITIVE_ORDER);
			sender.sendMessage("Hooks: " + StringUtil.join(fullNames, " | "));
		}
		return true;
	}

}
