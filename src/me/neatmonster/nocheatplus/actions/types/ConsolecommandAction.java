package me.neatmonster.nocheatplus.actions.types;

import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.checks.Check;

/**
 * Execute a command by imitating an admin typing the command directly into the
 * console
 * 
 */
public class ConsolecommandAction extends ActionWithParameters {

    public ConsolecommandAction(final String name, final int delay, final int repeat, final String command) {
        // Log messages may have color codes now
        super(name, delay, repeat, command);
    }

    /**
     * Fill in the placeholders ( stuff that looks like '[something]') with
     * information, make a nice String out of it that can be directly used
     * as a command in the console.
     * 
     * @param player
     *            The player that is used to fill in missing data
     * @param check
     *            The check that is used to fill in missing data
     * @return The complete, ready to use, command
     */
    public String getCommand(final NoCheatPlusPlayer player, final Check check) {
        return super.getMessage(player, check);
    }

    /**
     * Convert the commands data into a string that can be used in the config
     * files
     */
    @Override
    public String toString() {
        return "cmd:" + name + ":" + delay + ":" + repeat;
    }
}
