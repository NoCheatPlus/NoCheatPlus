package fr.neatmonster.nocheatplus.actions.types;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;

import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.ParameterHolder;
import fr.neatmonster.nocheatplus.logging.LogUtil;

/*
 * MM'""""'YMM                                                        dP 
 * M' .mmm. `M                                                        88 
 * M  MMMMMooM .d8888b. 88d8b.d8b. 88d8b.d8b. .d8888b. 88d888b. .d888b88 
 * M  MMMMMMMM 88'  `88 88'`88'`88 88'`88'`88 88'  `88 88'  `88 88'  `88 
 * M. `MMM' .M 88.  .88 88  88  88 88  88  88 88.  .88 88    88 88.  .88 
 * MM.     .dM `88888P' dP  dP  dP dP  dP  dP `88888P8 dP    dP `88888P8 
 * MMMMMMMMMMM                                                           
 * 
 * MMP"""""""MM            dP   oo                   
 * M' .mmmm  MM            88                        
 * M         `M .d8888b. d8888P dP .d8888b. 88d888b. 
 * M  MMMMM  MM 88'  `""   88   88 88'  `88 88'  `88 
 * M  MMMMM  MM 88.  ...   88   88 88.  .88 88    88 
 * M  MMMMM  MM `88888P'   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMMM                                      
 */
/**
 * Execute a command by imitating an administrator typing the command directly into the console.
 */
public class CommandAction<D extends ParameterHolder, L extends AbstractActionList<D, L>> extends ActionWithParameters<D, L> {

    /**
     * Instantiates a new command action.
     * 
     * @param name
     *            the name
     * @param delay
     *            the delay
     * @param repeat
     *            the repeat
     * @param command
     *            the command
     */
    public CommandAction(final String name, final int delay, final int repeat, final String command) {
        // Log messages may have color codes now.
        super(name, delay, repeat, command);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.actions.Action#execute(fr.neatmonster.nocheatplus.checks.ViolationData)
     */
    @Override
    public boolean execute(final D violationData) {
        final String command = super.getMessage(violationData);
        try {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (final CommandException e) {
            LogUtil.logWarning("[NoCheatPlus] Failed to execute the command '" + command + "': " + e.getMessage()
                    + ", please check if everything is setup correct.");
        } catch (final Exception e) {
            // I don't care in this case, your problem if your command fails.
        }
        return false;
    }

    /**
     * Convert the commands data into a string that can be used in the configuration files.
     * 
     * @return the string
     */
    @Override
    public String toString() {
        return "cmd:" + name + ":" + delay + ":" + repeat;
    }
}
