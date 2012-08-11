package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.ViolationData;

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
public class CommandAction extends ActionWithParameters {

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

    /**
     * Fill in the placeholders (stuff that looks like '[something]') with information, make a nice String out of it
     * that can be directly used as a command in the console.
     * 
     * @param check
     *            The check that is used to fill in missing dataFactory.
     * @param violationData
     *            the violation data
     * @return The complete, ready to use, command.
     */
    public String getCommand(final Check check, final ViolationData violationData) {
        return super.getMessage(check, violationData);
    }

    /**
     * Convert the commands dataFactory into a string that can be used in the configuration files.
     * 
     * @return the string
     */
    @Override
    public String toString() {
        return "cmd:" + name + ":" + delay + ":" + repeat;
    }
}
