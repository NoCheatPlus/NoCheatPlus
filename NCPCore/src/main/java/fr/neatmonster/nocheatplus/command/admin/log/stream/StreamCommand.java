package fr.neatmonster.nocheatplus.command.admin.log.stream;

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.StreamID;
import fr.neatmonster.nocheatplus.logging.Streams;
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
        this.usage = "ncp log stream stream_id[@level] (message...)";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!demandConsoleCommandSender(sender)) {
            return true;
        }
        if (args.length < 4) {
            return false;
        }
        String streamDef = args[2];
        Level level = null;
        if (streamDef.indexOf('@') != -1) {
            String[] split = streamDef.split("@");
            if (split.length != 2) {
                return false;
            }
            streamDef = split[0];

            // Attempt to parse level.
            try {
                // Convert to upper-case to ignore case.
                level = Level.parse(split[1].toUpperCase());
            }
            catch (IllegalArgumentException e) {
                sender.sendMessage("Bad level: " + split[1]);
                return true;
            }
        }
        LogManager man = NCPAPIProvider.getNoCheatPlusAPI().getLogManager();
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
            else {
                sender.sendMessage("Bad stream id: " + streamDef);
                return true;
            }
        }
        if (level == null) {
            // Instead: context-dependent?
            level = Level.INFO;
        }
        String message = StringUtil.join(args, 3, " ");
        man.log(streamId, level, message);
        // (No success message.)
        return true;
    }

}
