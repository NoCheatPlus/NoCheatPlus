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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class VersionCommand extends BaseCommand{

    public VersionCommand(JavaPlugin plugin) {
        super(plugin, "version", Permissions.COMMAND_VERSION, new String[]{"versions", "ver"});
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> lines = getVersionInfo();
        sender.sendMessage(lines.toArray(new String[lines.size()]));
        return true;
    }

    public static List<String> getVersionInfo() {
        final List<String> lines = new LinkedList<String>();
        final MCAccess mcAccess = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(MCAccess.class);
        lines.addAll(Arrays.asList(new String[]{
                "---- Version information ----",
                "#### Server ####",
                alt(Bukkit.getServer().getVersion()),
                "  detected: " + alt(ServerVersion.getMinecraftVersion()),
                "#### NoCheatPlus ####",
                "Plugin: " + alt(Bukkit.getPluginManager().getPlugin("NoCheatPlus").getDescription().getVersion()),
                "MCAccess: " + alt(mcAccess.getMCVersion() + " / " + mcAccess.getServerVersionTag()),
        }));
        final Map<String, Set<String>> featureTags = NCPAPIProvider.getNoCheatPlusAPI().getAllFeatureTags();
        if (!featureTags.isEmpty()) {
            final List<String> features = new LinkedList<String>();
            // Add present features.
            for (final Entry<String, Set<String>> entry : featureTags.entrySet()) {
                features.add(alt("  " + entry.getKey() + ": " + StringUtil.join(entry.getValue(), " | ")));
            }
            // Sort and add.
            Collections.sort(features, String.CASE_INSENSITIVE_ORDER);
            features.add(0, "Features:");
            lines.addAll(features);
        }
        final Collection<NCPHook> hooks = NCPHookManager.getAllHooks();
        if (!hooks.isEmpty()){
            final List<String> fullNames = new LinkedList<String>();
            for (final NCPHook hook : hooks){
                fullNames.add(alt(hook.getHookName() + " " + hook.getHookVersion()));
            }
            Collections.sort(fullNames, String.CASE_INSENSITIVE_ORDER);
            lines.add("Hooks: " + StringUtil.join(fullNames, " | "));
        }
        final List<String> relatedPlugins = new LinkedList<String>();
        for (final String name : new String[]{
                "CompatNoCheatPlus", "ProtocolLib", "ViaVersion", "ProtocolSupport", "PNCP", "NTAC"}) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
            if (plugin != null) {
                relatedPlugins.add(alt(plugin.getDescription().getFullName()));
            }
        }
        if (!relatedPlugins.isEmpty()) {
            lines.add("#### Related Plugins ####");
            lines.add(StringUtil.join(relatedPlugins, " | "));
        }
        return lines;
    }

    private static String alt(String x) {
        return x.replace('(', '~').replace(')', '~');
    }

}
