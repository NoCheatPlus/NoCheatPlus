package fr.neatmonster.nocheatplus.permissions;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

public class PermissionUtil {
	
	public static SimpleCommandMap getCommandMap(){
		return (((CraftServer) Bukkit.getServer()).getCommandMap());
	}
	
	/**
	 * TODO: Return undo info.
	 * @param permissionBase
	 * @param ignoredCommands
	 * @param ops
	 */
	public static void alterCommandPermissions(String permissionBase, Set<String> ignoredCommands, boolean invertIgnored, boolean ops){
		PluginManager pm = Bukkit.getPluginManager();
		Permission rootPerm = pm.getPermission(permissionBase);
		if (rootPerm == null){
			rootPerm = new Permission(permissionBase);
			pm.addPermission(rootPerm);
		}
		SimpleCommandMap map = getCommandMap();
		for (Command command : map.getCommands()){
			String lcLabel = command.getLabel().trim().toLowerCase();
			if (ignoredCommands != null){
				if (ignoredCommands.contains(lcLabel)){
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
			else cmdHadPerm = true;
			// Set permission default behavior.
			Permission cmdPerm = pm.getPermission(cmdPermName);
			if (cmdPerm == null){
				if (!cmdHadPerm){
					cmdPerm = new Permission(cmdPermName);
					cmdPerm.addParent(rootPerm, true);
					cmdPerm.setDefault(ops ? PermissionDefault.OP : PermissionDefault.FALSE);
					pm.addPermission(cmdPerm);
				}
			}
			else{
				// Change default of the permission.
				cmdPerm.setDefault(ops ? PermissionDefault.OP : PermissionDefault.FALSE);
			}	
		}
	}
}
