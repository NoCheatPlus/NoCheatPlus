package fr.neatmonster.nocheatplus.actions.types;

import org.bukkit.ChatColor;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.LogUtil;

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
    public final boolean toChat;

    /** Log to console? */
    public final boolean toConsole;

    /** Log to file? */
    public final boolean toFile;

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
        // TODO: already use && flagfromconfig.
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.neatmonster.nocheatplus.actions.Action#execute(fr.neatmonster.nocheatplus
	 * .checks.ViolationData)
	 */
	@Override
	public boolean execute(final ViolationData violationData) {
		final ConfigFile configurationFile = ConfigManager.getConfigFile();
		if (configurationFile.getBoolean(ConfPaths.LOGGING_ACTIVE) && !violationData.player.hasPermission(violationData.getPermissionSilent())) {
			final String message = super.getMessage(violationData);
			if (toChat && configurationFile.getBoolean(ConfPaths.LOGGING_INGAMECHAT)) {
				NoCheatPlus.sendAdminNotifyMessage(ChatColor.RED + "NCP: " + ChatColor.WHITE + CheckUtils.replaceColors(message));
			}
			if (toConsole && configurationFile.getBoolean(ConfPaths.LOGGING_CONSOLE)) LogUtil.logInfo("[NoCheatPlus] " + CheckUtils.removeColors(message));
			if (toFile && configurationFile.getBoolean(ConfPaths.LOGGING_FILE)) CheckUtils.fileLogger.info(CheckUtils.removeColors(message));
		}
		return false;
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

	@Override
	public Action getOptimizedCopy(final ConfigFile config, final Integer threshold) {
		if (!config.getBoolean(ConfPaths.LOGGING_ACTIVE)) return null;
		final boolean toConsole = this.toConsole && config.getBoolean(ConfPaths.LOGGING_CONSOLE);
		final boolean toFile = this.toFile&& config.getBoolean(ConfPaths.LOGGING_FILE);
		final boolean toChat= this.toChat&& config.getBoolean(ConfPaths.LOGGING_INGAMECHAT);
		if (!toChat && ! toConsole && !toFile) return null;
		return new LogAction(name, delay, repeat, toChat, toConsole, toFile, message);
	}
	
}
