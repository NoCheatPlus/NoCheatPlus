package fr.neatmonster.nocheatplus.command;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.checks.ViolationHistory.ViolationLevel;
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
    private void handleInfoCommand(final CommandSender sender, final String playerName) {
        final Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            final TreeMap<Long, ViolationLevel> violations = ViolationHistory.getHistory(player).getViolationLevels();
            if (violations.size() > 0) {
                sender.sendMessage(TAG + "Displaying " + playerName + "'s violations...");
                for (final long time : violations.descendingKeySet()) {
                    final ViolationLevel violationLevel = violations.get(time);
                    final String[] parts = violationLevel.check.split("\\.");
                    final String check = parts[parts.length - 1];
                    final String parent = parts[parts.length - 2];
                    final double VL = Math.round(violationLevel.VL);
                    sender.sendMessage(TAG + "[" + dateFormat.format(new Date(time)) + "] (" + parent + ".)" + check
                            + " VL " + VL);
                }
            } else
                sender.sendMessage(TAG + "Displaying " + playerName + "'s violations... nothing to display.");
        } else {
            sender.sendMessage(TAG + "404 Not Found");
            sender.sendMessage(TAG + "The requested player was not found on this server.");
        }
    }
	
}
