package me.neatmonster.nocheatplus.actions.types;

import java.util.ArrayList;

import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.Action;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.checks.Check;

/**
 * Action with parameters is used to
 * 
 */
public abstract class ActionWithParameters extends Action {

    private final ArrayList<Object> messageParts;

    public ActionWithParameters(final String name, final int delay, final int repeat, final String message) {
        super(name, delay, repeat);

        messageParts = new ArrayList<Object>();

        parseMessage(message);
    }

    /**
     * Get a string with all the wildcards replaced with data from LogData
     * 
     * @param data
     * @return
     */

    protected String getMessage(final NoCheatPlusPlayer player, final Check check) {

        final StringBuilder log = new StringBuilder(100); // Should be big enough most
        // of the time

        for (final Object part : messageParts)
            if (part instanceof String)
                log.append((String) part);
            else
                log.append(check.getParameter((ParameterName) part, player));

        return log.toString();
    }

    private void parseMessage(final String message) {
        final String parts[] = message.split("\\[", 2);

        // No opening braces left
        if (parts.length != 2)
            messageParts.add(message);
        else {
            final String parts2[] = parts[1].split("\\]", 2);

            // Found no matching closing brace
            if (parts2.length != 2)
                messageParts.add(message);
            else {
                final ParameterName w = ParameterName.get(parts2[0]);

                if (w != null) {
                    // Found an existing wildcard inbetween the braces
                    messageParts.add(parts[0]);
                    messageParts.add(w);

                    // Go further down recursive
                    parseMessage(parts2[1]);
                } else
                    messageParts.add(message);
            }
        }
    }
}
