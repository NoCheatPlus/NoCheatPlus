package fr.neatmonster.nocheatplus.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.logging.LogUtil;

public class CommandUtil {

    /**
     * Return plugin + server commands [Subject to change].
     * @return Returns null if not CraftBukkit or CommandMap not available.
     */
    public static CommandMap getCommandMap() {
        try {
            return NCPAPIProvider.getNoCheatPlusAPI().getMCAccess().getCommandMap();
        }
        catch (Throwable t) {
            LogUtil.logSevere(t);
            return null;
        }
    }

    /**
     * Get all Command instances, that NCP can get hold of. Attempt to get a SimpleCommandMap instance from the server to get the actually registered commands, but also get commands from JavaPlugin instances.
     * @return
     */
    public static Collection<Command> getCommands() {
        final Collection<Command> commands = new LinkedHashSet<Command>(500);

        // All (?) commands from the SimpleCommandMap of the server, if available.
        final CommandMap commandMap = getCommandMap();
        if (commandMap != null && commandMap instanceof SimpleCommandMap) {
            commands.addAll(((SimpleCommandMap) commandMap).getCommands());
        }
        // TODO: Fall-back for Vanilla / CB commands? [Fall-back should be altering permission defaults, though negating permissions is the right way.]

        // Fall-back: plugin commands.
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof JavaPlugin) {
                final JavaPlugin javaPlugin = (JavaPlugin) plugin;
                final Map<String, Map<String, Object>> map = javaPlugin.getDescription().getCommands();
                if (map != null) {
                    for (String label : map.keySet()) {
                        Command command = javaPlugin.getCommand(label);
                        if (command != null) {
                            commands.add(command);
                        }
                    }
                }
            }
        }

        return commands;
    }

    /**
     * Get the command label (trim + lower case), include server commands [subject to change].
     * @param alias
     * @param strict If to return null if no command is found.
     * @return The command label, if possible to find, or the alias itself (+ trim + lower-case).
     */
    public static String getCommandLabel(final String alias, final boolean strict) {
        final Command command = getCommand(alias);
        if (command == null) {
            return strict ? null : alias.trim().toLowerCase();
        }
        else {
            return command.getLabel().trim().toLowerCase();
        }
    }

    /**
     * Get a command, include server commands [subject to change].
     * @param alias
     * @return
     */
    public static Command getCommand(final String alias) {
        final String lcAlias = alias.trim().toLowerCase();
        final CommandMap map = getCommandMap();
        if (map != null) {
            return map.getCommand(lcAlias);
        } else {
            // TODO: maybe match versus plugin commands.
            return null;
        }
    }

    /**
     * Match for CheckType, some smart method, to also match after first "_" for convenience of input. 
     * @param input
     * @return
     */
    public static List<String> getCheckTypeTabMatches(final String input) {
        final String ref = input.toUpperCase().replace('-', '_').replace('.', '_');
        final List<String> res = new ArrayList<String>();
        for (final CheckType checkType : CheckType.values()) {
            final String name = checkType.name();
            if (name.startsWith(ref)) {
                res.add(name);
            }
        }
        if (ref.indexOf('_') == -1) {
            for (final CheckType checkType : CheckType.values()) {
                final String name = checkType.name();
                final String[] split = name.split("_", 2);
                if (split.length > 1 && split[1].startsWith(ref)) {
                    res.add(name);
                }
            }
        }
        if (!res.isEmpty()) {
            Collections.sort(res);
            return res;
        }
        return null;
    }
    
    /**
     * Convenience method to map all matches within one of the String[] arrays
     * to its first element.<br>
     * Do note that this does exact-case comparison, for case insensitive
     * comparison use a lower-case model and pass a lower-case input.
     * 
     * @param input
     * @param model
     *            Should be lower case (!).
     * @return Always returns a modifiable list.
     */
    public static List<String> getTabMatches(final String input, final String[][] model) {
        final List<String> res = new LinkedList<String>();
        for (final String[] choices : model) {
            for (final String choice : choices) {
                if (choice.startsWith(input)) {
                    res.add(choices[0]);
                    break;
                }
            }
        }
        return res;
    }

}
