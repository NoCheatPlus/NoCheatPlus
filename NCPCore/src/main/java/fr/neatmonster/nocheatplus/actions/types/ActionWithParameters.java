/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.actions.types;

import java.util.ArrayList;

import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ParameterHolder;
import fr.neatmonster.nocheatplus.actions.ParameterName;

/**
 * Action with parameters is used for the messages (chat, console, log) or the commands.
 */
public abstract class ActionWithParameters<D extends ParameterHolder, L extends AbstractActionList<D, L>> extends Action<D, L> {
	/** The parts of the message. */
	protected final ArrayList<Object> messageParts;

	protected final String message;

	protected boolean needsParameters = true;

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
		this.message = message;
		// Assume we don't nee parameters.
		needsParameters = false;
		parseMessage(message);
	}

    /**
     * Get a string with all the wildcards replaced with data from the violation data.<br>
     * This should set the flag needsParameters if parameters are actually needed.
     * 
     * @param violationData
     *            the violation data
     * @return the message
     */
    protected String getMessage(final D violationData) { // interface
        // Should be big enough most of the time.
        final StringBuilder log = new StringBuilder(150);

        for (final Object part : messageParts)
            if (part instanceof String)
                log.append((String) part);
            else if (part == null) log.append("[???]");
            else{
            	try{
            		log.append(violationData.getParameter((ParameterName) part));
            	}
            	catch (Exception e){
            		log.append(part.toString());
            	}
            }

        return log.toString();
    }

    /**
     * Parses the message.
     * 
     * @param message
     *            the message
     */
    protected void parseMessage(final String message) {
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
                final ParameterName w = ParameterName.get(parts2[0].toLowerCase());

                if (w != null) {
                	needsParameters = true;
                    // Found an existing wildcard in between the braces.
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
		return needsParameters;
	}
}
