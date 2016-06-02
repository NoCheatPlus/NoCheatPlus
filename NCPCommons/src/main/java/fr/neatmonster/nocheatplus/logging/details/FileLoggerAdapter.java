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

import java.io.File;
import java.util.logging.Level;

public class FileLoggerAdapter extends FileLogger implements ContentLogger<String> {

    // TODO: Store path/file either here or on FileLogger.

    // TODO: Do store the string path (directory / direct file path), to reference correctly.

    private final String prefix;

    /**
     * See FileLogger(File).
     * @param file Path to log file or existing directory.
     */
    public FileLoggerAdapter(File file) {
        this(file, null);
    }

    /**
     * See FileLogger(File).
     * @param file Path to log file or existing directory.
     * @param prefix Prefix for all log messages.
     */
    public FileLoggerAdapter(File file, String prefix) {
        super(file);
        this.prefix = (prefix == null || prefix.isEmpty()) ? null : prefix; 
    }

    @Override
    public void log(Level level, String content) {
        // TODO: Check loggerisInoperable() ?
        logger.log(level, prefix == null ? content : (prefix + content));
    }

}
