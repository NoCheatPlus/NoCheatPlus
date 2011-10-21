package cc.co.evenprime.bukkit.nocheat.actions.types;

import cc.co.evenprime.bukkit.nocheat.data.LogData;

/**
 * Execute a command by imitating an admin typing the command directly into the
 * console
 * 
 */
public class ConsolecommandAction extends ActionWithParameters {

    public ConsolecommandAction(String name, int delay, int repeat, String command) {
        super(name, delay, repeat, command);

    }

    public String getCommand(LogData ldata) {

        return super.getMessage(ldata);
    }
}
