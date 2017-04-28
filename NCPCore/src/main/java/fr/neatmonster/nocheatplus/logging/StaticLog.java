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
package fr.neatmonster.nocheatplus.logging;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Static access methods for more or less direct logging using either LogManager
 * (usually INIT or STATUS) or System.out.
 * 
 * @author mc_dev
 *
 */
public class StaticLog {

    // TODO: Remove this class, instead use an implementation of LogManager for testing.

    private static boolean useLogManager = false;

    private static StreamID streamID = Streams.INIT;

    /** The Constant logOnce. */
    // TODO: Quick and dirty - should probably use an access ordered LinkedHashSet, to expire the eldest half :p.
    private static final Set<Integer> logOnce = Collections.synchronizedSet(new HashSet<Integer>());


    /**
     * Now needs to be set, in order to log to the INIT stream instead of the console.
     * @param useLogManager
     */
    public static void setUseLogManager(boolean useLogManager) {
        StaticLog.useLogManager = useLogManager;
    }

    public static void setStreamID(StreamID streamID) {
        if (streamID == null) {
            throw new NullPointerException("StreamID must not be null, use setUseLogManager(false) instead.");
        }
        StaticLog.streamID = streamID;
    }

    public static void logInfo(final String msg) {
        log(Level.INFO, msg);
    }

    public static void logWarning(final String msg) {
        log(Level.WARNING, msg);
    }

    public static void logSevere(final String msg) {
        log(Level.SEVERE, msg);
    }

    public static void logInfo(final Throwable t) {
        log(Level.INFO, StringUtil.throwableToString(t));
    }

    public static void logWarning(final Throwable t) {
        log(Level.WARNING, StringUtil.throwableToString(t));
    }

    public static void logSevere(final Throwable t) {
        log(Level.SEVERE, StringUtil.throwableToString(t));
    }

    public static void log(final Level level, final String msg) {
        log(StaticLog.streamID, level, msg);
    }

    /**
     * 
     * @param streamID
     *            May get ignored if only the console is available.
     * @param level
     * @param msg
     */
    public static void log(final StreamID streamID, final Level level, final String msg) {
        if (useLogManager) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().log(streamID, level, msg);
        } else {
            System.out.println("[" + level + "][NoCheatPlus] " + new Date());
            System.out.println(msg);
        }
    }

    /**
     * Not really once: Always log the header with an id: create a hash of to be
     * logged parts and keep the hash in memory, log it with the header - only
     * log the longMessage once per hash. Once the id tracking storage has
     * reached a certain size, many/all are removed. The hash involves both the
     * header and longMessage strings as well as their lengths. No distinction
     * is made for log level. This is intended for long messages like stack
     * traces.
     * 
     * @param level
     * @param message
     */
    public static void logOnce(final StreamID stream, final Level level, 
            final String header, final String longMessage) {
        // TODO: LogOnce should be in static log ?
        final int ref = header.hashCode() ^ longMessage.hashCode() ^ new Integer(header.length()).hashCode()
                ^ new Integer(longMessage.length()).hashCode();
        final String extra;
        final boolean details = logOnce.add(ref);
        if (details) {
            // Not already contained.
            extra = " -> log once id=";
        } else {
            extra = " See earlier in this log, search for -> log once id=";
        }
        log(stream, level, header + extra + ref);
        if (details) {
            log(stream, level, longMessage);
            if (logOnce.size() > 10000) {
                logOnce.clear();
                log(stream, level, "Cleared log once ids, due to exceeding the maximum number of stored ids.");
            }
        }
    }

}
