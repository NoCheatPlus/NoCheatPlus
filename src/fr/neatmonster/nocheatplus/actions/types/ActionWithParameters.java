package fr.neatmonster.nocheatplus.actions.types;

import java.util.ArrayList;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.ViolationData;

/*
 * MMP"""""""MM            dP   oo                   M""MMM""MMM""M oo   dP   dP       
 * M' .mmmm  MM            88                        M  MMM  MMM  M      88   88       
 * M         `M .d8888b. d8888P dP .d8888b. 88d888b. M  MMP  MMP  M dP d8888P 88d888b. 
 * M  MMMMM  MM 88'  `""   88   88 88'  `88 88'  `88 M  MM'  MM' .M 88   88   88'  `88 
 * M  MMMMM  MM 88.  ...   88   88 88.  .88 88    88 M  `' . '' .MM 88   88   88    88 
 * M  MMMMM  MM `88888P'   dP   dP `88888P' dP    dP M    .d  .dMMM dP   dP   dP    dP 
 * MMMMMMMMMMMM                                      MMMMMMMMMMMMMM                    
 * 
 * MM"""""""`YM                                                  dP                              
 * MM  mmmmm  M                                                  88                              
 * M'        .M .d8888b. 88d888b. .d8888b. 88d8b.d8b. .d8888b. d8888P .d8888b. 88d888b. .d8888b. 
 * MM  MMMMMMMM 88'  `88 88'  `88 88'  `88 88'`88'`88 88ooood8   88   88ooood8 88'  `88 Y8ooooo. 
 * MM  MMMMMMMM 88.  .88 88       88.  .88 88  88  88 88.  ...   88   88.  ... 88             88 
 * MM  MMMMMMMM `88888P8 dP       `88888P8 dP  dP  dP `88888P'   dP   `88888P' dP       `88888P' 
 * MMMMMMMMMMMM                                                                                                                                                               
 */
/**
 * Action with parameters is used for the messages (chat, console, log) or the commands.
 */
public abstract class ActionWithParameters extends Action {
    /** The parts of the message. */
    private final ArrayList<Object> messageParts;

    /**
     * Instantiates a new action with parameters.
     * 
     * @param name
     *            the name
     * @param delay
     *            the delay
     * @param repeat
     *            the repeat
     * @param message
     *            the message
     */
    public ActionWithParameters(final String name, final int delay, final int repeat, final String message) {
        super(name, delay, repeat);

        messageParts = new ArrayList<Object>();

        parseMessage(message);
    }

    /**
     * Get a string with all the wildcards replaced with data from the violation data.
     * 
     * @param violationData
     *            the violation data
     * @return the message
     */
    protected String getMessage(final ViolationData violationData) {
        // Should be big enough most of the time.
        final StringBuilder log = new StringBuilder(100);

        for (final Object part : messageParts)
            if (part instanceof String)
                log.append((String) part);
            else
                log.append(violationData.getParameter((ParameterName) part));

        return log.toString();
    }

    /**
     * Parses the message.
     * 
     * @param message
     *            the message
     */
    private void parseMessage(final String message) {
        final String parts[] = message.split("\\[", 2);

        // No opening braces left.
        if (parts.length != 2)
            messageParts.add(message);
        else {
            final String parts2[] = parts[1].split("\\]", 2);

            // Found no matching closing brace.
            if (parts2.length != 2)
                messageParts.add(message);
            else {
                final ParameterName w = ParameterName.get(parts2[0]);

                if (w != null) {
                    // Found an existing wildcard inbetween the braces.
                    messageParts.add(parts[0]);
                    messageParts.add(w);

                    // Go further down recursive.
                    parseMessage(parts2[1]);
                } else
                    messageParts.add(message);
            }
        }
    }

	@Override
	public boolean needsParameters() {
		return true;
	}
}
