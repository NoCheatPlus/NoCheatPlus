package cc.co.evenprime.bukkit.nocheat.actions.types;

import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.Check;

/**
 * Print a log message to various locations
 * 
 */
public class LogAction extends ActionWithParameters {

    private final boolean toChat;
    private final boolean toConsole;
    private final boolean toFile;

    public LogAction(String name, int delay, int repeat, boolean toChat, boolean toConsole, boolean toFile, String message) {
        // Log messages may have color codes now
        super(name, delay, repeat, message);
        this.toChat = toChat;
        this.toConsole = toConsole;
        this.toFile = toFile;
    }

    public String getLogMessage(NoCheatPlayer player, Check check) {
        return super.getMessage(player, check);
    }

    public boolean toChat() {
        return toChat;
    }

    public boolean toConsole() {
        return toConsole;
    }

    public boolean toFile() {
        return toFile;
    }

    public String toString() {
        return "log:" + name + ":" + delay + ":" + repeat + ":" + (toConsole ? "c" : "") + (toChat ? "i" : "") + (toFile ? "f" : "");
    }
}
