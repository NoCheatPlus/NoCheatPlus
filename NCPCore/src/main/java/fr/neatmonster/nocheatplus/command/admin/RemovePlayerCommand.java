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
package fr.neatmonster.nocheatplus.command.admin;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class RemovePlayerCommand extends BaseCommand {

    public RemovePlayerCommand(JavaPlugin plugin) {
        super(plugin, "removeplayer", Permissions.COMMAND_REMOVEPLAYER, new String[]{
                "remove",	
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {
        if (args.length < 2 || args.length > 3) return false;
        String playerName = args[1];
        final CheckType checkType;
        if (args.length == 3){
            try{
                checkType = CheckType.valueOf(args[2].toUpperCase().replace('-', '_').replace('.', '_'));
            } catch (Exception e){
                sender.sendMessage(TAG + "Could not interpret: " + args[2]);
                sender.sendMessage(TAG + "Check type should be one of: " + StringUtil.join(Arrays.asList(CheckType.values()), " | "));
                return true;
            }
        }
        else checkType = CheckType.ALL;

        if (playerName.equals("*")){
            DataManager.clearData(checkType);
            sender.sendMessage(TAG + "Removed all data and history: " + checkType);
            return true;
        }

        final Player player = DataManager.getPlayer(playerName);
        if (player != null) playerName = player.getName();

        ViolationHistory hist = ViolationHistory.getHistory(playerName, false);
        boolean histRemoved = false;
        if (hist != null){
            histRemoved = hist.remove(checkType);
            if (checkType == CheckType.ALL){
                histRemoved = true;
                ViolationHistory.removeHistory(playerName);
            }
        }

        if (DataManager.removeExecutionHistory(checkType, playerName)) histRemoved = true;

        final boolean dataRemoved = DataManager.removeData(playerName, checkType);

        if (dataRemoved || histRemoved){
            String which;
            if (dataRemoved && histRemoved) which = "data and history";
            else if (dataRemoved) which = "data";
            else which = "history";
            sender.sendMessage(TAG + "Removed " + which + " (" + checkType + "): " + playerName);
        }
        else
            sender.sendMessage(TAG + "Nothing found (" + checkType + ", exact spelling): " + playerName);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        // At least complete CheckType
        if (args.length == 3) return CommandUtil.getCheckTypeTabMatches(args[2]);
        return null;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.command.AbstractCommand#testPermission(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean testPermission(CommandSender sender, Command command, String alias, String[] args) {
        return super.testPermission(sender, command, alias, args) 
                || args.length >= 2 && args[1].trim().equalsIgnoreCase(sender.getName()) 
                && sender.hasPermission(Permissions.COMMAND_REMOVEPLAYER_SELF.getBukkitPermission());
    }

}
