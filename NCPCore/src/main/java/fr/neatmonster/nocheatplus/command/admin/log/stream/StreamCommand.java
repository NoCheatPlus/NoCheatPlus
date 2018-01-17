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
package fr.neatmonster.nocheatplus.command.admin.log.stream;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.StreamID;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Log to any log stream (console only).
 * 
 * @author asofold
 *
 */
public class StreamCommand extends BaseCommand {

    public StreamCommand(JavaPlugin plugin) {
        super(plugin, "stream", null); // No permission: currently console-only.
        this.usage = "ncp log stream (stream_id)[@(level)][?color|?nocolor][+(stream_id2)[@(level2)][?color|?nocolor][+...]] (message...) ";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!demandConsoleCommandSender(sender)) {
            return true;
        }
        if (args.length < 4) {
            return false;
        }
        LogManager man = NCPAPIProvider.getNoCheatPlusAPI().getLogManager();
        String message = null;
        String messageColor = null;
        String messageNoColor = null;
        for (String streamDef : args[2].split("\\+")) {
            Level level = null;
            boolean color = false;
            boolean noColor = false;
            // Check for color def.
            if (streamDef.indexOf('?') != -1) {
                String[] split = streamDef.split("\\?");
                if (split.length != 2) {
                    sender.sendMessage("Bad flag (color|nocolor): " + streamDef);
                    continue;
                }
                streamDef = split[0];
                String temp = split[1].toLowerCase();
                if (temp.matches("^(nc|noc|nocol|nocolor)$")) {
                    noColor = true;
                }
                else if (temp.matches("^(c|col|color)$")) {
                    color = true;
                }
                else {
                    sender.sendMessage("Bad flag (color|nocolor): " + temp);
                    continue;
                }
            }
            // Parse level first.
            if (streamDef.indexOf('@') != -1) {
                String[] split = streamDef.split("@");
                if (split.length != 2) {
                    sender.sendMessage("Bad level definition: " + streamDef);
                    continue;
                }
                streamDef = split[0];

                // Attempt to parse level.
                try {
                    // Convert to upper-case to ignore case.
                    level = Level.parse(split[1].toUpperCase());
                }
                catch (IllegalArgumentException e) {
                    sender.sendMessage("Bad level: " + split[1]);
                    continue;
                }
            }
            // Get StreamID, account for shortcuts.
            StreamID streamId = man.getStreamID(streamDef);
            if (streamId == null) {
                String altStreamDef = streamDef.toLowerCase();
                if (altStreamDef.equals("notify")) {
                    // Default level should be INFO.
                    streamId = Streams.NOTIFY_INGAME;
                }
                else if (altStreamDef.equals("debug")) {
                    streamId = Streams.TRACE_FILE;
                    if (level == null) {
                        level = Level.FINE;
                    }
                }
                else if (altStreamDef.equals("status")) {
                    streamId = Streams.STATUS;
                }
                else if (altStreamDef.equals("init")) {
                    streamId = Streams.INIT;
                }
                else if (altStreamDef.equals("console")) {
                    // Prefer the plugin logger.
                    streamId = Streams.PLUGIN_LOGGER;
                }
                else if (altStreamDef.equals("file")) {
                    streamId = Streams.DEFAULT_FILE;
                }
                else {
                    sender.sendMessage("Bad stream id: " + streamDef);
                    continue;
                }
            }
            // Finally log.
            if (level == null) {
                // Instead: context-dependent?
                level = Level.INFO;
            }
            if (message == null) {
                message = StringUtil.join(args, 3, " ");
            }
            final String logMessage;
            if (noColor) {
                if (messageNoColor == null) {
                    messageNoColor = ChatColor.stripColor(ColorUtil.removeColors(message));
                }
                logMessage = messageNoColor;
            }
            else if (color) {
                if (messageColor == null) {
                    messageColor = ColorUtil.replaceColors(message);
                }
                logMessage = messageColor;
            }
            else {
                logMessage = message;
            }
            man.log(streamId, level, logMessage);
        }
        // (No success message.)
        return true;
    }

}
