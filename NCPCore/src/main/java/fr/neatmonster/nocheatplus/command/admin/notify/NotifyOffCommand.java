/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.command.admin.notify;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.players.DataManager;

public class NotifyOffCommand extends BaseCommand {

    public NotifyOffCommand(JavaPlugin plugin) {
        super(plugin, "off", null, new String[]{"0", "-"});
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
        DataManager.getPlayerData(sender.getName(), true).setNotifyOff(true);
        sender.sendMessage(TAG + "Notifications are now turned " + ChatColor.RED + "off" + ChatColor.WHITE + ".");
        return true;
    }

}
