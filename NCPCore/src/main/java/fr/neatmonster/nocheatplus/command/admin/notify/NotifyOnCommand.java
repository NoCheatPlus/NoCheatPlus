package fr.neatmonster.nocheatplus.command.admin.notify;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.players.DataManager;

public class NotifyOnCommand extends BaseCommand {

    public NotifyOnCommand(JavaPlugin plugin) {
        super(plugin, "on", null, new String[]{"1", "+"});
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.command.AbstractCommand#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 2){
            return false;
        }
        if (!(sender instanceof Player)){
            // TODO: Might implement if upvoted a lot.
            sender.sendMessage(TAG + "Toggling notifications is only available for online players.");
            return true;
        }
        DataManager.getPlayerData(sender.getName(), true).setNotifyOff(false);
        sender.sendMessage(TAG + "Notifications are now turned " + ChatColor.YELLOW + "on" + ChatColor.WHITE + ".");
        return true;
    }

}
