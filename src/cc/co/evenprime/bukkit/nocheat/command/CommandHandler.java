package cc.co.evenprime.bukkit.nocheat.command;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.EventType;

public class CommandHandler {

    private CommandHandler() {}

    public static boolean handleCommand(NoCheat plugin, CommandSender sender, Command command, String label, String[] args) {

        // Not our command
        if(!command.getName().equalsIgnoreCase("nocheat") || args.length == 0)
            return false;

        if(args[0].equalsIgnoreCase("permlist") && args.length >= 2) {
            // permlist command was used
            return handlePermlistCommand(plugin, sender, args);

        } else if(args[0].equalsIgnoreCase("reload")) {
            // reload command was used
            return handleReloadCommand(plugin, sender);
        }

        else if(args[0].equalsIgnoreCase("performance")) {
            // performance command was used
            return handlePerformanceCommand(plugin, sender);
        }

        else if(args[0].equalsIgnoreCase("playerinfo") && args.length >= 2) {
            // performance command was used
            return handlePlayerInfoCommand(plugin, sender, args);
        }

        return false;
    }

    private static boolean handlePlayerInfoCommand(NoCheat plugin, CommandSender sender, String[] args) {
        // Does the sender have permission?
        if(sender instanceof Player && !sender.hasPermission(Permissions.ADMIN_PLAYERINFO)) {
            return false;
        }

        Map<String, Object> map = plugin.getPlayerData(args[1]);
        String filter = "";

        if(args.length > 2) {
            filter = args[2];
        }

        sender.sendMessage("PlayerInfo for " + args[1]);
        for(Entry<String, Object> entry : map.entrySet()) {
            if(entry.getKey().contains(filter)) {
                sender.sendMessage(entry.getKey() + ": " + entry.getValue());
            }
        }
        return true;
    }

    private static boolean handlePermlistCommand(NoCheat plugin, CommandSender sender, String[] args) {
        // Does the sender have permission to use it?
        if(sender instanceof Player && !sender.hasPermission(Permissions.ADMIN_PERMLIST)) {
            return false;
        }

        // Get the player by name
        Player player = plugin.getServer().getPlayerExact(args[1]);
        if(player == null) {
            sender.sendMessage("Unknown player: " + args[1]);
            return true;
        }

        // Should permissions be filtered by prefix?
        String prefix = "";
        if(args.length == 3) {
            prefix = args[2];
        }

        // Make a copy to allow sorting
        List<Permission> perms = new LinkedList<Permission>(plugin.getDescription().getPermissions());

        Collections.sort(perms, new Comparator<Permission>() {

            public int compare(Permission o1, Permission o2) {

                String name1 = o1.getName();
                String name2 = o2.getName();

                if(name1.equals(name2))
                    return 0;

                if(name1.startsWith(name2)) {
                    return 1;
                }

                if(name2.startsWith(name1)) {
                    return -1;
                }

                return name1.compareTo(name2);
            }

        });

        sender.sendMessage("Player " + player.getName() + " has the permission(s):");

        for(Permission permission : perms) {
            if(permission.getName().startsWith(prefix)) {
                sender.sendMessage(permission.getName() + ": " + player.hasPermission(permission));
            }
        }
        return true;
    }

    private static boolean handleReloadCommand(NoCheat plugin, CommandSender sender) {
        // Does the sender have permission?
        if(sender instanceof Player && !sender.hasPermission(Permissions.ADMIN_RELOAD)) {
            return false;
        }

        sender.sendMessage("[NoCheat] Reloading configuration");
        plugin.reloadConfiguration();
        sender.sendMessage("[NoCheat] Configuration reloaded");

        return true;
    }

    private static boolean handlePerformanceCommand(NoCheat plugin, CommandSender sender) {
        // Does the sender have permission?
        if(sender instanceof Player && !sender.hasPermission(Permissions.ADMIN_PERFORMANCE)) {
            return false;
        }

        sender.sendMessage("[NoCheat] Retrieving performance statistics");

        long totalTime = 0;

        for(EventType type : EventType.values()) {
            Performance p = plugin.getPerformance(type);

            long total = p.getTotalTime();
            totalTime += total;

            StringBuilder string = new StringBuilder("").append(type.toString());
            string.append(": total ").append(Performance.toString(total));
            string.append(", relative ").append(Performance.toString(p.getRelativeTime()));
            string.append(" over ").append(p.getCounter()).append(" events.");

            sender.sendMessage(string.toString());
        }

        sender.sendMessage("Total time spent: " + Performance.toString(totalTime));

        return true;
    }
}
