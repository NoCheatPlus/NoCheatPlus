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
package fr.neatmonster.nocheatplus.logging.details;

public class LogOptions {

    /**
     * Describe the context in which the log method may be used.<br>
     * Policies:
     * <li>..._DIRECT: Will log directly if within context, otherwise schedule to a task.</li>
     * <li>..._TASK: Always schedule to a task.</li>
     * 
     * @author dev1mc
     *
     */
    public static enum CallContext {
        // TODO: Consider making this a general enum for call contexts (API wrappers, player data etc. too?).
        // Primary as long as it exists...
        /** Only execute directly in the primary thread, for other threads use a task to execute within the primary thread. */
        PRIMARY_THREAD_DIRECT,
        /** Always schedule to execute within the primary thread. */
        PRIMARY_THREAD_TASK,
        /** Only log if within the primary thread. */
        PRIMARY_THREAD_ONLY,
        /** Execute directly independently of thread. */
        ANY_THREAD_DIRECT,
        /** Ensure asynchronous logging, but avoid scheduling. */
        ASYNCHRONOUS_DIRECT,
        /** Always schedule to execute within a (more or less) dedicated asynchronous task. */
        ASYNCHRONOUS_TASK,
        /** Only log if it is not the primary thread. */
        ASYNCHRONOUS_ONLY,

        // CUSTOM_THREAD_DIRECT|TASK // Needs a variable (Thread, methods to sync into a specific thread would have to be registered in LogManager).
        ;

        // TODO: Can distinguish further: Call from where, log from where directly, schedule from where (allow to skip certain contexts).
    }

    public final String name;
    public final CallContext callContext;

    public LogOptions(LogOptions options) {
        this(options.name, options.callContext);
    }

    public LogOptions(String name, CallContext callContext) {
        this.name = name;
        this.callContext = callContext;
        // TODO: shutdown policy (clear, log), rather with per-world threads.
    }

}
