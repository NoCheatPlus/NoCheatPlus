package fr.neatmonster.nocheatplus.command.admin.exemption;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class UnexemptCommand extends BaseCommand {

    public UnexemptCommand(JavaPlugin plugin) {
        super(plugin, "unexempt", Permissions.COMMAND_UNEXEMPT);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {
        // TODO: Reduce copy and paste by introducing some super class.
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
            // Unexempt all.
            // TODO: might care to find players only ?
            NCPExemptionManager.clear();
            sender.sendMessage(TAG + "Removed exemptions for all players for checks: " + checkType);
            return true;
        }
        // Find player.
        final Player player = DataManager.getPlayer(playerName);
        final UUID id;
        if (player != null) {
            playerName = player.getName();
            id = player.getUniqueId();
        } else {
            id = DataManager.getUUID(playerName);
        }
        if (id == null) {
            sender.sendMessage(TAG + "Not an online player nor a UUID: " + playerName);
        } else {
            NCPExemptionManager.unexempt(id, checkType);
            sender.sendMessage(TAG + "Remove exemptions for " + playerName + " for checks: " + checkType);
        }
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
        return super.testPermission(sender, command, alias, args) || args.length >= 2 && args[1].trim().equalsIgnoreCase(sender.getName()) && sender.hasPermission(permission + ".self");
    }

}
