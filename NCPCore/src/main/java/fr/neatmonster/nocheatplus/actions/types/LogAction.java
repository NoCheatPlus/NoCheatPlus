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

import java.util.logging.Level;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.logging.Streams;

/**
 * Default log action for standard targets.
 */
public class LogAction extends GenericLogAction {

    protected static final GenericLogActionConfig configIngame = new GenericLogActionConfig(
            ConfPaths.LOGGING_BACKEND_INGAMECHAT_ACTIVE, Streams.NOTIFY_INGAME, true, Level.INFO, "i");
    protected static final GenericLogActionConfig configConsole = new GenericLogActionConfig(
            ConfPaths.LOGGING_BACKEND_CONSOLE_ACTIVE, Streams.SERVER_LOGGER, false, Level.INFO, "c");
    protected static final GenericLogActionConfig configFile = new GenericLogActionConfig(
            ConfPaths.LOGGING_BACKEND_FILE_ACTIVE, Streams.DEFAULT_FILE, false, Level.INFO, "f");

    /**
     * Instantiates a new log action (not optimized).
     * 
     * @param name
     *            the name
     * @param delay
     *            the delay
     * @param repeat
     *            the repeat
     * @param toChat
     *            the to chat
     * @param toConsole
     *            the to console
     * @param toFile
     *            the to file
     * @param message
     *            the message
     */
    public LogAction(final String name, final int delay, final int repeat, final boolean toChat,
            final boolean toConsole, final boolean toFile, final String message) {
        super(name, delay, repeat, message, true, toChat ? configIngame : null, toConsole ? configConsole : null, toFile ? configFile : null);
    }

}
