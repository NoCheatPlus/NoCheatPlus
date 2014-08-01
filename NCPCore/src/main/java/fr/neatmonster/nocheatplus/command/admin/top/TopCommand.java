package fr.neatmonster.nocheatplus.command.admin.top;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationHistory;
import fr.neatmonster.nocheatplus.checks.ViolationHistory.VLView;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.hooks.APIUtils;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.FCFSComparator;

public class TopCommand extends BaseCommand{
    
    protected static class PrimaryThreadWorker implements Runnable{
        private final Collection<CheckType> checkTypes;
        private final CommandSender sender;
        private final Comparator<VLView> comparator;
        private final int n;
        private final Plugin plugin;
        public PrimaryThreadWorker(CommandSender sender, Collection<CheckType> checkTypes, Comparator<VLView> comparator, int n, Plugin plugin) {
            this.checkTypes = new LinkedHashSet<CheckType>(checkTypes);
            this.sender = sender;
            this.comparator = comparator;
            this.n = n;
            this.plugin = plugin;
        }
        
        @Override
        public void run() {
            final Iterator<CheckType> it = checkTypes.iterator();
            List<VLView> views = null;
            CheckType type = null;
            while (it.hasNext()) {
                type = it.next();
                it.remove();
                views = ViolationHistory.getView(type);
                if (views.isEmpty()) {
                    views = null;
                } else {
                    break;
                }
            }
            if (views == null) {
                sender.sendMessage("No more history to process.");
            } else {
                // Start sorting and result processing asynchronously.
                Bukkit.getScheduler().runTaskAsynchronously(plugin, 
                    new AsynchronousWorker(sender, type, views, checkTypes, comparator, n, plugin));
            }
        }
        
    }
    
    protected static class AsynchronousWorker implements Runnable{
        private final CommandSender sender;
        private final CheckType checkType;
        private final List<VLView> views;
        private final Collection<CheckType> checkTypes;
        private final Comparator<VLView> comparator;
        private final int n;
        private final Plugin plugin;
        public AsynchronousWorker(CommandSender sender, CheckType checkType, List<VLView> views, Collection<CheckType> checkTypes, Comparator<VLView> comparator, int n, Plugin plugin) {
            this.sender = sender;
            this.checkType = checkType;
            this.views = views;
            this.checkTypes = checkTypes;
            this.comparator = comparator;
            this.n = n;
            this.plugin = plugin;
        }
        @Override
        public void run() {
            final DecimalFormat format = new DecimalFormat("#.#");
            // Sort
            Collections.sort(views, comparator);
            // Display.
            final StringBuilder builder = new StringBuilder(100 + 32 * views.size());
            builder.append(checkType.toString());
            builder.append(":");
            final String c1, c2;
            if (sender instanceof Player) {
                c1 = ChatColor.WHITE.toString();
                c2 = ChatColor.GRAY.toString();
            } else {
                c1 = c2 = "";
            }
            int done = 0;
            for (final VLView view : views) {
                builder.append(" " + c1);
                builder.append(view.name);
                // Details
                builder.append(c2 + "(");
                // sum
                builder.append("sum=");
                builder.append(format.format(view.sumVL));
                // n
                builder.append("/n=");
                builder.append(view.nVL);
                // avg
                builder.append("/avg=");
                builder.append(format.format(view.sumVL / view.nVL));
                // max
                builder.append("/max=");
                builder.append(format.format(view.maxVL));
                builder.append(")");
                done ++;
                if (done >= n) {
                    break;
                }
            }
            if (views.isEmpty()) {
                builder.append(c1 + "Nothing to display.");
            }
            final String message = builder.toString();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                new Runnable() {
                    @Override
                    public void run() {
                        sender.sendMessage(message);
                    }
                });
            if (!checkTypes.isEmpty()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                    new PrimaryThreadWorker(sender, checkTypes, comparator, n, plugin));
            }
        }
    }

    public TopCommand(JavaPlugin plugin) {
        super(plugin, "top", Permissions.COMMAND_TOP);
        this.usage = "Optional: Specify number of entries to show (once).\nObligatory: Specify check types (multiple possible).\nOptional: Specify what to sort by (multiple possible: -sumvl, -avgvl, -maxvl, -nvl, -name, -time).\nThis is a heavy operation, use with care."; // -check
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length < 2) {
            return false;
        }
        int startIndex = 1;
        int n = 10;
        try {
            n = Integer.parseInt(args[1].trim());
            startIndex = 2;
        } catch (NumberFormatException e) {}
        if (n <= 0) {
            sender.sendMessage("Setting number of entries to 10");
            n = 1;
        } else if ((sender instanceof Player) && n > 300) {
            sender.sendMessage("Capping number of entries at 300.");
            n = 300;
        } else if  (n > 10000) {
            sender.sendMessage("Capping number of entries at 10000.");
            n = 10000;
        }
        
        Set<CheckType> checkTypes = new LinkedHashSet<CheckType>();
        for (int i = startIndex; i < args.length; i ++) {
            CheckType type = null;
            try {
                type = CheckType.valueOf(args[i].trim().toUpperCase().replace('-', '_').replace('.', '_'));
            } catch (Throwable t) {} // ...
            if (type != null) {
                checkTypes.addAll(APIUtils.getWithChildren(type)); // Includes type.
            }
        }
        if (checkTypes.isEmpty()) {
            sender.sendMessage("No check types specified!");
            return false;
        }
        
        Comparator<VLView> comparator = VLView.parseMixedComparator(args, startIndex);
        if (comparator == null) {
            // TODO: Default comparator ?
            comparator = new FCFSComparator<VLView>(Arrays.asList(VLView.CmpnVL, VLView.CmpSumVL), true);
        }
        
        // Run a worker task.
        Bukkit.getScheduler().scheduleSyncDelayedTask(access, 
            new PrimaryThreadWorker(sender, checkTypes, comparator, n, access));
        
        return true;
    }
    
    // TODO: Tab completion (!).
    
}
