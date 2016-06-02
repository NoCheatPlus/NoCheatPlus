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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Wrap a file logger, adding convenience setup methods.
 * @author dev1mc
 *
 */
public class FileLogger {
    
    protected static class FileLogFormatter extends Formatter {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        
        // TODO: Consider storing a custom line break (needs adding to StringUtil.throwableToString).

        @Override
        public String format(final LogRecord record) {
            final Throwable throwable = record.getThrown();
            final String message = record.getMessage();
            int roughLength = 64 + message.length();
            final String throwableMessage;
            if (throwable != null) {
                throwableMessage = StringUtil.throwableToString(throwable);
                roughLength += throwableMessage.length();
            } else {
                throwableMessage = null;
            }
            final StringBuilder builder = new StringBuilder(roughLength);
            builder.append(dateFormat.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(message);
            builder.append('\n');
            if (throwableMessage != null) {
                builder.append(throwableMessage);
            }
            return builder.toString();
        }
        
    }
    
    public final Logger logger;
    protected FileHandler fileHandler = null;
    
    protected boolean inoperable = false;
    
    /**
     * Initialize with an anonymous logger and the given log-file path.
     * 
     * @param file Path to log file or an existing directory.
     */
    public FileLogger(File file) {
        // TODO: Should re-add a file-name prefix (allow null).
        // TODO: File encoding + line endings.
        // TODO: Add options to switch file with file size, rolling files, etc.
        // [could also keep track of rough size of written data, to switch file "on the fly", problem: which log? -> replace logger copy on write :p].
        logger = Logger.getAnonymousLogger();
        detachLogger();
        final File container;
        if (!file.exists() && file.getName().indexOf('.') == -1) {
            // Make it a directory, if no extension is given.
            file.mkdirs();
        }
        if (file.isDirectory()) {
            container  = file;
            // File-name by date within the given folder.
            SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
            String prefix = dateFormat.format(System.currentTimeMillis());
            int n = 1;
            while (true) {
                file = new File(container, prefix + "-" + n + ".log");
                if (!file.exists()) {
                    break;
                }
                n ++;
            }
        } else {
            // Directly use the given file.
            container = file.getParentFile();
        }
        // Ensure the container exists.
        if (!container.exists()) {
            container.mkdirs();
        }
        // Create file and initialize logger.
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                file = null;
            }
        }
        if (file != null) {
            try {
                initLogger(file);
                logger.log(Level.INFO, "Logger started.");
                inoperable = false;
            } catch (SecurityException e) {
            } catch (IOException e) {
            }
        }
        
    }
    
    protected void initLogger(File file) throws SecurityException, IOException {
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        fileHandler = new FileHandler(file.getCanonicalPath(), true);
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new FileLogFormatter());
        logger.addHandler(fileHandler);
    }
    
    public void flush() {
        if (fileHandler != null) {
            fileHandler.flush();
        }
    }
    
    public void detachLogger() {
        if (fileHandler != null) {
            fileHandler.close();
            fileHandler = null;
        }
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
    }

    public boolean isInoperable () {
        return inoperable;
    }

    
}
