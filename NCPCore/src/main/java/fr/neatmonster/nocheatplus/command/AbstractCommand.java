package fr.neatmonster.nocheatplus.command;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

/**
 * Base command class, featuring some features.<br>
 * Taken from the Archer plugin (@asofold), extended by aliases.
 * @author mc_dev
 *
 */
public abstract class AbstractCommand<A> implements TabExecutor{

    public static final List<String> noTabChoices = Collections.unmodifiableList(new LinkedList<String>());

    /**
     * Convenience method: join with a space in between.
     * @param args
     * @param startIndex
     * @return
     */
    public static String join(String[] args, int startIndex){
        return join(args, startIndex, " ");
    }

    /**
     * Convenience method.
     * @param args
     * @param startIndex
     * @return
     */
    public static String join(String[] args, int startIndex, String sep){
        final StringBuilder b = new StringBuilder(100);
        if (startIndex < args.length) b.append(args[startIndex]);
        for (int i = startIndex + 1; i < args.length; i++){
            b.append(sep);
            b.append(args[i]);
        }
        return b.toString();
    }

    /**
     * 
     * @param sender
     * @return True if sender is a Player, otherwise false is returned and a message sent.
     */
    public static boolean demandPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        } else {
            sender.sendMessage("A player is required to run this command.");
            return false;
        }
    }

    /**
     * 
     * @param sender
     * @return True if sender is a ConsoleCommandSender, otherwise false is returned and a message sent.
     */
    public static boolean demandConsoleCommandSender(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        } else {
            sender.sendMessage("This command can only be run from the console.");
            return false;
        }
    }

    ////////////////
    // Not static.
    ////////////////

    protected final A access;
    public final String label;
    /** Permission necessary to use this command. May be null. */
    public final String permission;
    /** Sub commands for delegation. */
    protected final Map<String, AbstractCommand<?>> subCommands = new LinkedHashMap<String, AbstractCommand<?>>();
    /** The index in args to check for sub-commands. -1 stands for default, either parent + 1 or 0 */
    protected int subCommandIndex = -1;
    /** Aliases for the command label. */
    protected final String[] aliases;

    /** This is only shown if a parent command receives false for onCommand and if it is not null. */
    protected String usage = null;

    /**
     * 
     * @param access
     * @param label Lower-case.
     * @param permission
     */
    public AbstractCommand(A access, String label, String permission){
        this(access, label, permission, null);
    }

    /**
     * 
     * @param access
     * @param label Lower-case.
     * @param permission May be null (no permission necessary).
     * @param aliases May be null (no aliases). If given, the aliases only take effect for tab completion and selection of sub commands. Lower-case.
     */
    public AbstractCommand(A access, String label, String permission, String[] aliases){
        this.access = access;
        this.label = label;
        this.permission = permission;
        this.aliases = aliases;
    }

    public void addSubCommands(AbstractCommand<?>... commands){
        for (AbstractCommand<?> subCommand : commands ){
            subCommands.put(subCommand.label, subCommand);
            if (subCommand.subCommandIndex == -1){
                subCommand.subCommandIndex = Math.max(0, this.subCommandIndex) + 1;
            }
            if (subCommand.aliases != null){
                for (final String alias : subCommand.aliases){
                    if (!subCommands.containsKey(alias)){
                        subCommands.put(alias, subCommand);
                    }
                }
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        final Set<String> choices = new LinkedHashSet<String>(subCommands.size());
        int len = args.length;
        // Attempt to delegate.
        int subCommandIndex = Math.max(0, this.subCommandIndex);
        if (len == subCommandIndex || len == subCommandIndex + 1){
            String arg = len == subCommandIndex ? "" : args[subCommandIndex].trim().toLowerCase();
            for (AbstractCommand<?> cmd : subCommands.values()){
                if (cmd.label.startsWith(arg) && cmd.testPermission(sender, command, alias, args)){
                    // Only completes the label (!).
                    choices.add(cmd.label);
                }
            }
        }
        else if (len > subCommandIndex + 1){
            String arg = args[subCommandIndex].trim().toLowerCase();
            AbstractCommand<?> subCommand = subCommands.get(arg);
            if (subCommand != null && subCommand.testPermission(sender, command, alias, args)){
                return subCommand.onTabComplete(sender, command, alias, args);
            }
        }
        // No tab completion by default.
        if (choices.isEmpty()) return noTabChoices;
        else return new LinkedList<String>(choices);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
    {
        int len = args.length;
        int subCommandIndex = Math.max(0, this.subCommandIndex);
        if (len > subCommandIndex){
            String arg = args[subCommandIndex].trim().toLowerCase();
            AbstractCommand<?> subCommand = subCommands.get(arg);
            if (subCommand != null){
                if (!subCommand.testPermission(sender, command, alias, args)){
                    sender.sendMessage(ChatColor.DARK_RED + "You don't have permission.");
                    return true;
                }
                final boolean res = subCommand.onCommand(sender, command, alias, args);
                if (!res && subCommand.usage != null) {
                    sender.sendMessage(subCommand.usage);
                    return true;
                } else {
                    return res;
                }
            }
        }
        // Usage.
        return false;
    }

    /**
     * Test if the CommandSender has the permission necessary to run THIS command (not meant for checking sub-command permissions recursively).
     * <br>Override for more complex specialized permissions.
     * @param sender
     * @return
     */
    public boolean testPermission(CommandSender sender, Command command, String alias, String args[]){
        return permission == null || sender.hasPermission(permission);
    }

}
