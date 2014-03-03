package fr.neatmonster.nocheatplus.command.admin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.checks.ViolationHistory.ViolationLevel;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TopCommand extends BaseCommand {

    /**
    * Container for the results... Ideally the ViolationHistory / ViolationLevel
    * structure could be updated to be more accessible
    */
    public static class VLContainer {
        public final String playerName;
        public ViolationLevel violationLevel;

        public VLContainer(String playerName, ViolationLevel violationLevel) {
            this.playerName = playerName;
            this.violationLevel = violationLevel;
        }

        /**
         * Descending sort.
         */
        public static Comparator<VLContainer> VLCComparator = new Comparator<TopCommand.VLContainer>() {
            @Override
            public int compare(final VLContainer vl1, final VLContainer vl2) {
                if (vl1.violationLevel.time == vl2.violationLevel.time) return 0;
                else if (vl1.violationLevel.time < vl2.violationLevel.time) return 1;
                else return -1;
            }
        };
    }

    public TopCommand(JavaPlugin plugin) {
        super(plugin, "top", Permissions.COMMAND_TOP);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label,
                             String[] args) {
        if (args.length != 2 ) return false;
        handleTopCommand(sender, args[1]);
        return true;
    }

    /**
     * Handle the '/nocheatplus top' command.
     *
     * @param sender
     *            the sender
     * @param checkName
     *            the check name
     */
    private void handleTopCommand(final CommandSender sender, String checkName) {
        final CheckType checkType;
        try{
            checkType = CheckType.valueOf(checkName.toUpperCase().replace('-', '_').replace('.', '_'));
        } catch (Exception e){
            sender.sendMessage(TAG + "Could not interpret: " + checkName);
            sender.sendMessage(TAG + "Check type should be one of: " + StringUtil.join(Arrays.asList(CheckType.values()), " | "));
            return;
        }

        ArrayList<VLContainer> levels = new ArrayList<VLContainer>();
        for(final Player player : sender.getServer().getOnlinePlayers()) {
            ViolationHistory history = ViolationHistory.getHistory(player.getName(), false);
            if(history != null) {
                ViolationLevel[] violationLevels = history.getViolationLevels();
                if(violationLevels.length > 0) {
                    for (final ViolationLevel violationLevel : violationLevels) {
                        CheckType check = ViolationHistory.checkTypeMap.get(violationLevel.check);
                        if(check != null) { // Should never be null but...
                            if (checkType.equals(check)) {
                                levels.add(new VLContainer(player.getName(), violationLevel));
                            }
                        }
                    }
                }
            }
        }

        //Time to sort.  Should we do this async?
        if(levels.size() > 0) {
            sender.sendMessage(TAG + "Displaying top " + checkType.getName() + " violations...");
            Collections.sort(levels, VLContainer.VLCComparator);
            //TODO:
            for(int i = 0; i < 5 && i < levels.size(); i++ ) {
                VLContainer vlc = levels.get(i);
                final long sumVL = Math.round(vlc.violationLevel.sumVL);
                final long maxVL = Math.round(vlc.violationLevel.maxVL);
                final long avVl  = Math.round(vlc.violationLevel.sumVL / (double) vlc.violationLevel.nVL);
                sender.sendMessage(TAG + vlc.playerName + ": " + sumVL + "VL" + ChatColor.GRAY.toString() + "  (n" + vlc.violationLevel.nVL + "a" + avVl +"m" + maxVL +")");
            }
        } else {
            sender.sendMessage(TAG + "Displaying top " + checkType.getName() + " violations... Nothing to display.");
        }
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.command.AbstractCommand#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Fill in players.
        return null;
    }
}
