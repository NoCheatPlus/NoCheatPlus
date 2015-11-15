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
