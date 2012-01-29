package cc.co.evenprime.bukkit.nocheat.actions.types;

import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.ActionWithParameters;
import cc.co.evenprime.bukkit.nocheat.checks.Check;

/**
 * Print a message to various locations
 * 
 */
public class LogAction extends ActionWithParameters {

    private boolean toChat    = true;
    private boolean toConsole = true;
    private boolean toFile    = true;
    private String  message;

    public LogAction(String name, String message) {
        // Log messages may have color codes now
        super(name, 0, 5, message);
        this.message = message;

    }

    private LogAction(String name, int delay, int repeat, boolean toChat, boolean toConsole, boolean toFile, String message) {
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

    @Override
    public Action cloneWithProperties(String properties) {
        String propertyFields[] = properties.split(":");

        int delay = Integer.parseInt(propertyFields[0]);
        int repeat = 5;
        if(propertyFields.length > 1)
            repeat = Integer.parseInt(propertyFields[1]);

        boolean toChat = false;
        boolean toFile = false;
        boolean toConsole = false;

        if(propertyFields.length > 2) {
            toChat = propertyFields[2].toLowerCase().contains("ch");
            toFile = propertyFields[2].toLowerCase().contains("fi");
            toConsole = propertyFields[2].toLowerCase().contains("co");
        }

        return new LogAction(name, delay, repeat, toChat, toConsole, toFile, message);
    }

    @Override
    public String getProperties() {
        String props = delay + ":" + repeat + ":" + ((toChat ? "ch," : "") + (toFile ? "fi," : "") + (toConsole ? "co," : ""));
        if(props.endsWith(",")) {
            return props.substring(0, props.length() - 1);
        } else {
            return "";
        }
    }
}
