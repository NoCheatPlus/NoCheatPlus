package fr.neatmonster.nocheatplus.command.admin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.checks.ViolationHistory.ViolationLevel;
import fr.neatmonster.nocheatplus.command.NCPCommand;
import fr.neatmonster.nocheatplus.players.Permissions;

public class InfoCommand extends NCPCommand {

	public InfoCommand(NoCheatPlus plugin) {
		super(plugin, "info", Permissions.ADMINISTRATION_INFO);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		if (args.length != 2 ) return false;
		handleInfoCommand(sender, args[1]);
		return true;
	}
	
    /**
     * Handle the '/nocheatplus info' command.
     * 
     * @param sender
     *            the sender
     * @param playerName
     *            the player name
     * @return true, if successful
     */
    private void handleInfoCommand(final CommandSender sender, String playerName) {
    	final Player player = Bukkit.getPlayerExact(playerName);
    	if (player != null) playerName = player.getName();
    	
    	final ViolationHistory history = ViolationHistory.getHistory(playerName, false);
    	final boolean known = player != null || history != null;
    	if (history == null){
    		sender.sendMessage(TAG + "No entries for " + playerName + "'s violations... " + (known?"":"(exact spelling?)") +".");
    		return;
    	}
    	
        final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        final ViolationLevel[] violations = history.getViolationLevels();
        if (violations.length > 0) {
            sender.sendMessage(TAG + "Displaying " + playerName + "'s violations...");
            final String c = (sender instanceof Player) ? ChatColor.GRAY.toString() : ""; 
            for (final ViolationLevel violationLevel : violations) {
                final long time = violationLevel.time;
                final String[] parts = violationLevel.check.split("\\.");
                final String check = parts[parts.length - 1].toLowerCase();
                final String parent = parts[parts.length - 2].toLowerCase();
                final long sumVL = Math.round(violationLevel.sumVL);
                final long maxVL = Math.round(violationLevel.maxVL);
                final long avVl  = Math.round(violationLevel.sumVL / (double) violationLevel.nVL);
                sender.sendMessage(TAG + "[" + dateFormat.format(new Date(time)) + "] " + parent + "." + check
                        + " VL " + sumVL + c + "  (n" + violationLevel.nVL + "a" + avVl +"m" + maxVL +")");
            }
        } else
            sender.sendMessage(TAG + "Displaying " + playerName + "'s violations... nothing to display.");
        
    }
	
}
