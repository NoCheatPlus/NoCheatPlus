package cc.co.evenprime.bukkit.nocheat.actions.types;

import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.ActionWithParameters;
import cc.co.evenprime.bukkit.nocheat.checks.Check;

/**
 * Execute a command by imitating an admin typing the command directly into the
 * console
 * 
 */
public class ConsolecommandAction extends ActionWithParameters {

    private String command;

    public ConsolecommandAction(String name, String command) {
        super(name, 0, 1, command);
        this.command = command;
    }

    private ConsolecommandAction(String name, int delay, int repeat, String command) {
        // Log messages may have color codes now
        super(name, delay, repeat, command);
    }

    public String getCommand(NoCheatPlayer player, Check check) {
        return super.getMessage(player, check);
    }

    /**
     * Make a copy of the action, with some modifications
     * @param properties
     * @return
     */
    @Override
    public Action cloneWithProperties(String properties) {
        String propertyFields[] = properties.split(":");

        int delay = Integer.parseInt(propertyFields[0]);
        int repeat = 5;
        if(propertyFields.length > 1)
            repeat = Integer.parseInt(propertyFields[1]);

        return new ConsolecommandAction(name, delay, repeat, command);
    }
    
    @Override
    public String getProperties() {
        return delay + ":" + repeat;
    }
}
