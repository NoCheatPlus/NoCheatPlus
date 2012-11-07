package fr.neatmonster.nocheatplus.permissions;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import fr.neatmonster.nocheatplus.command.CommandUtil;

public class PermissionUtil {
	
	/**
	 * Entry for what the old state of a command was.
	 * @author mc_dev
	 *
	 */
	public static class CommandProtectionEntry{
		public final  Command command;
		public final String label;
		public final String permission;
		public final PermissionDefault permissionDefault;
		public final  String permissionMessage;
		/**
		 * 
		 * @param command
		 * @param label trim + lower case.
		 * @param permission
		 * @param permissionDefault
		 * @param permissionMessage
		 */
		public CommandProtectionEntry(Command command, String label, String permission, PermissionDefault permissionDefault, String permissionMessage){
			this.command = command;
			this.label = label;
			this.permission = permission;
			this.permissionDefault = permissionDefault;
			this.permissionMessage = permissionMessage;
		}
		
		public void restore(){
			Command registered = CommandUtil.getCommand(label);
			if (registered == null || registered != command) return;
			if (!label.equalsIgnoreCase(command.getLabel().trim().toLowerCase())) command.setLabel(label);
			command.setPermission(permission);
			if (permission != null && permissionDefault != null){
				Permission perm = Bukkit.getPluginManager().getPermission(permission);
				if (perm != null) perm.setDefault(permissionDefault);
			}
			command.setPermissionMessage(permissionMessage);
		}
	}
	
	/**
	 * 
	 * @param commands
	 * @param permissionBase
	 * @param ops
	 * @return
	 */
	public static List<CommandProtectionEntry> protectCommands(Collection<String> commands, String permissionBase, boolean ops){
		return protectCommands(permissionBase, commands, true, ops);
	}
	
	/**
	 * 
	 * @param permissionBase
	 * @param ignoredCommands
	 * @param invertIgnored
	 * @param ops
	 * @return
	 */
	public static List<CommandProtectionEntry> protectCommands(String permissionBase, Collection<String> ignoredCommands, boolean invertIgnored, boolean ops){
		Set<String> checked = new HashSet<String>();
		for (String label : ignoredCommands){
			checked.add(CommandUtil.getCommandLabel(label, false));
		}
		PluginManager pm = Bukkit.getPluginManager();
		Permission rootPerm = pm.getPermission(permissionBase);
		if (rootPerm == null){
			rootPerm = new Permission(permissionBase);
			pm.addPermission(rootPerm);
		}
		List<CommandProtectionEntry> changed = new LinkedList<CommandProtectionEntry>();
		SimpleCommandMap map = CommandUtil.getCommandMap();
		for (Command command : map.getCommands()){
			String lcLabel = command.getLabel().trim().toLowerCase();
			if (checked != null){
				if (checked.contains(lcLabel)){
					if (!invertIgnored) continue;
				}
				else if (invertIgnored) continue;
			}
			// Set the permission for the command.
			String cmdPermName = command.getPermission();
			boolean cmdHadPerm;
			if (cmdPermName == null){
				// Set a permission.
				cmdPermName = permissionBase + "." + lcLabel;
				command.setPermission(cmdPermName);
				cmdHadPerm = false;
			}
			else{
				cmdHadPerm = true;
			}
			// Set permission default behavior.
			Permission cmdPerm = pm.getPermission(cmdPermName);
			if (cmdPerm == null){
				if (!cmdHadPerm){
					cmdPerm = new Permission(cmdPermName);
					cmdPerm.addParent(rootPerm, true);
					pm.addPermission(cmdPerm);
				}
			}
			// Create change history entry.
			if (cmdHadPerm) changed.add(new CommandProtectionEntry(command, lcLabel, cmdPermName, cmdPerm.getDefault(), command.getPermissionMessage()));
			else changed.add(new CommandProtectionEntry(command, lcLabel, null, null, command.getPermissionMessage()));
			// Change 
			cmdPerm.setDefault(ops ? PermissionDefault.OP : PermissionDefault.FALSE);
			command.setPermissionMessage("Unknown command. Type \"help\" for help.");
		}
		return changed;
	}
}
