package cc.co.evenprime.bukkit.nocheat.actions.types;

import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.ActionWithParameters;
import cc.co.evenprime.bukkit.nocheat.checks.Check;
import cc.co.evenprime.bukkit.nocheat.log.Colors;

/**
 * Print a message to various locations
 * 
 */
public class LogAction extends ActionWithParameters {

    private boolean toChat    = true;
    private boolean toConsole = true;
    private boolean toFile    = true;
    private String  message;

    public LogAction(String name, int delay, int repeat, String message) {
        // Log messages may have color codes now
        super(name, delay, repeat, Colors.replaceColors(message));
        this.message = message;

    }

    private LogAction(String name, int delay, int repeat, boolean toChat, boolean toConsole, boolean toFile, String message) {
        // Log messages may have color codes now
        super(name, delay, repeat, Colors.replaceColors(message));
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

    @Override
    public Action cloneWithProperties(String properties) {
        boolean toChat = properties.toLowerCase().contains("ch");
        boolean toFile = properties.toLowerCase().contains("fi");
        boolean toConsole = properties.toLowerCase().contains("co");
        return new LogAction(name, delay, repeat, toChat, toConsole, toFile, message);
    }

    @Override
    public String getProperties() {
        String props = (toChat ? "ch," : "") + (toFile ? "fi," : "") + (toConsole ? "co," : "");
        if(props.length() > 0) {
            return props.substring(0, props.length() - 1);
        } else {
            return "";
        }
    }
}
