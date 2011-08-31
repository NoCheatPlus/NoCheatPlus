package cc.co.evenprime.bukkit.nocheat.actions.types;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Execute a command by imitating an admin typing the command directly into the
 * console
 * 
 * @author Evenprime
 * 
 */
public class ConsolecommandAction extends Action {

    private final String command;

    public ConsolecommandAction(int delay, int repeat, String command) {
        super(delay, repeat);

        this.command = command;
    }

    public String getCommand(Map<String, String> map) {

        String com = command;

        for(Entry<String, String> entry : map.entrySet()) {
            com = com.replaceAll(entry.getKey(), entry.getValue());
        }

        return com;
    }
}
