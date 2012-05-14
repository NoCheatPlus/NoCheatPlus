package fr.neatmonster.nocheatplus;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;

/**
 * Handle all NoCheatPlus related commands in a common place
 */
public class CommandHandler implements CommandExecutor {

    private final List<Permission> perms;

    public CommandHandler() {
        // Make a copy to allow sorting
        perms = new LinkedList<Permission>(NoCheatPlus.instance.getDescription().getPermissions());

        // Sort NoCheats permission by name and parent-child relation with
        // a custom sorting method
        Collections.sort(perms, new Comparator<Permission>() {

            @Override
            public int compare(final Permission o1, final Permission o2) {

                final String name1 = o1.getName();
                final String name2 = o2.getName();

                if (name1.equals(name2))
                    return 0;

                if (name1.startsWith(name2))
                    return 1;

                if (name2.startsWith(name1))
                    return -1;

                return name1.compareTo(name2);
            }
        });
    }

    private boolean handlePermlistCommand(final CommandSender sender, final String[] args) {

        // Get the player by name
        final Player player = Bukkit.getServer().getPlayerExact(args[1]);
        if (player == null) {
            sender.sendMessage("Unknown player: " + args[1]);
            return true;
        }

        // Should permissions be filtered by prefix?
        String prefix = "";
        if (args.length == 3)
            prefix = args[2];

        sender.sendMessage("Player " + player.getName() + " has the permission(s):");

        for (final Permission permission : perms)
            if (permission.getName().startsWith(prefix))
                sender.sendMessage(permission.getName() + ": " + NCPPlayer.hasPermission(player, permission.getName()));
        return true;
    }

    private boolean handlePlayerInfoCommand(final CommandSender sender, final String[] args) {

        final Player player = Bukkit.getPlayer(args[1]);
        final Map<String, Object> map = player == null ? new HashMap<String, Object>() : NCPPlayer.getPlayer(player)
                .collectData();
        String filter = "";

        if (args.length > 2)
            filter = args[2];

        sender.sendMessage("PlayerInfo for " + args[1]);
        for (final Entry<String, Object> entry : map.entrySet())
            if (entry.getKey().contains(filter))
                sender.sendMessage(entry.getKey() + ": " + entry.getValue());
        return true;
    }

    private boolean handleReloadCommand(final CommandSender sender) {

        // Players need a special permission for this
        if (!(sender instanceof Player) || NCPPlayer.hasPermission(sender, Permissions.ADMIN_RELOAD)) {
            sender.sendMessage("[NoCheatPlus] Reloading configuration");
            ConfigManager.cleanup();
            ConfigManager.init();
            NCPPlayer.reloadConfig();
            sender.sendMessage("[NoCheatPlus] Configuration reloaded");
        } else
            sender.sendMessage("You lack the " + Permissions.ADMIN_RELOAD + " permission to use 'reload'");

        return true;
    }

    /**
     * Handle a command that is directed at NoCheatPlus
     */
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel,
            final String[] args) {

        if (sender instanceof Player) {
            final String worldName = ((Player) sender).getWorld().getName();
            final boolean protectPlugins = ConfigManager.getConfigFile(worldName).getBoolean(
                    ConfPaths.MISCELLANEOUS_PROTECTPLUGINS);

            // Hide NoCheatPlus's commands if the player doesn't have the required permission
            if (protectPlugins && !NCPPlayer.hasPermission(sender, Permissions.ADMIN_COMMANDS)) {
                sender.sendMessage("Unknown command. Type \"help\" for help.");
                return true;
            }
        }

        boolean result = false;
        // Not our command, how did it get here?
        if (!command.getName().equalsIgnoreCase("nocheatplus") || args.length == 0)
            result = false;
        else if (args[0].equalsIgnoreCase("permlist") && args.length >= 2)
            // permlist command was used
            result = handlePermlistCommand(sender, args);
        else if (args[0].equalsIgnoreCase("reload"))
            // reload command was used
            result = handleReloadCommand(sender);
        else if (args[0].equalsIgnoreCase("playerinfo") && args.length >= 2)
            // playerinfo command was used
            result = handlePlayerInfoCommand(sender, args);

        return result;
    }
}
