package fr.neatmonster.nocheatplus.command.admin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.PermissionCache;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class PermissionCacheCommand extends BaseCommand {

    public PermissionCacheCommand(JavaPlugin plugin) {
        super(plugin, "permission-cache", Permissions.COMMAND + ".permission-cache");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        try {
            boolean value = Boolean.parseBoolean(args[1]);
            if (value) {
                if (!PermissionCache.INITIALIZED) {
                    PermissionCache.init();
                    sender.sendMessage("Permission cache is on");
                } else {
                    sender.sendMessage("Permission cache wasn't off?");
                }
            } else {
                if (PermissionCache.INITIALIZED) {
                    PermissionCache.close();
                    sender.sendMessage("Permission cache is off");
                } else {
                    sender.sendMessage("Permission cache wasn't on?");
                }
            }
        } catch (Exception e) {
            sender.sendMessage("There was an error: " + e.getMessage());
        }
        return true;
    }
}
