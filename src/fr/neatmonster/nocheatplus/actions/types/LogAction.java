package fr.neatmonster.nocheatplus.actions.types;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;

/*
 * M""MMMMMMMM                   MMP"""""""MM            dP   oo                   
 * M  MMMMMMMM                   M' .mmmm  MM            88                        
 * M  MMMMMMMM .d8888b. .d8888b. M         `M .d8888b. d8888P dP .d8888b. 88d888b. 
 * M  MMMMMMMM 88'  `88 88'  `88 M  MMMMM  MM 88'  `""   88   88 88'  `88 88'  `88 
 * M  MMMMMMMM 88.  .88 88.  .88 M  MMMMM  MM 88.  ...   88   88 88.  .88 88    88 
 * M         M `88888P' `8888P88 M  MMMMM  MM `88888P'   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMM               .88 MMMMMMMMMMMM                                      
 *                       d8888P                                                    
 */
/**
 * Print a log message to various locations.
 */
public class LogAction extends ActionWithParameters {
    // Some flags to decide where the log message should show up, based on the configuration file.
    /** Log to chat? */
    private final boolean toChat;

    /** Log to console? */
    private final boolean toConsole;

    /** Log to file? */
    private final boolean toFile;

    /**
     * Instantiates a new log action.
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
        super(name, delay, repeat, message);
        this.toChat = toChat;
        this.toConsole = toConsole;
        this.toFile = toFile;
    }

    /**
     * Parse the final log message out of various data from the player and check that triggered the action.
     * 
     * @param player
     *            The player that is used as a source for the log message.
     * @param check
     *            The check that is used as a source for the log message.
     * @return the log message
     */
    public String getLogMessage(final Player player, final Check check) {
        return super.getMessage(player, check);
    }

    /**
     * Should the message be shown in chat?
     * 
     * @return true, if yes
     */
    public boolean toChat() {
        return toChat;
    }

    /**
     * Should the message be shown in the console?
     * 
     * @return true, if yes
     */
    public boolean toConsole() {
        return toConsole;
    }

    /**
     * Should the message be written to the logfile?
     * 
     * @return true, if yes
     */
    public boolean toFile() {
        return toFile;
    }

    /**
     * Create the string that's used to define the action in the logfile.
     * 
     * @return the string
     */
    @Override
    public String toString() {
        return "log:" + name + ":" + delay + ":" + repeat + ":" + (toConsole ? "c" : "") + (toChat ? "i" : "")
                + (toFile ? "f" : "");
    }
}
