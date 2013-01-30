package fr.neatmonster.nocheatplus.actions;

import java.util.Map;

import fr.neatmonster.nocheatplus.actions.types.CancelAction;
import fr.neatmonster.nocheatplus.actions.types.LogAction;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.logging.LogUtil;

/*
 * MMP"""""""MM            dP   oo                   MM""""""""`M                     dP                              
 * M' .mmmm  MM            88                        MM  mmmmmmmM                     88                              
 * M         `M .d8888b. d8888P dP .d8888b. 88d888b. M'      MMMM .d8888b. .d8888b. d8888P .d8888b. 88d888b. dP    dP 
 * M  MMMMM  MM 88'  `""   88   88 88'  `88 88'  `88 MM  MMMMMMMM 88'  `88 88'  `""   88   88'  `88 88'  `88 88    88 
 * M  MMMMM  MM 88.  ...   88   88 88.  .88 88    88 MM  MMMMMMMM 88.  .88 88.  ...   88   88.  .88 88       88.  .88 
 * M  MMMMM  MM `88888P'   dP   dP `88888P' dP    dP MM  MMMMMMMM `88888P8 `88888P'   dP   `88888P' dP       `8888P88 
 * MMMMMMMMMMMM                                      MMMMMMMMMMMM                                                 .88 
 *                                                                                                            d8888P  
 */
/**
 * Helps with creating Actions out of text string definitions.
 */
public class ActionFactory extends AbstractActionFactory<ViolationData, ActionList> {

    /**
     * Instantiates a new action factory.
     * 
     * @param library
     *            the library
     */
    public ActionFactory(final Map<String, Object> library) {
        super(library, ActionList.listFactory);
    }

    /**
     * Creates a new Action object.
     * 
     * @param actionDefinition
     *            the action definition
     * @return the action
     */
	public Action<ViolationData, ActionList> createAction(String actionDefinition) {
        actionDefinition = actionDefinition.toLowerCase();

        if (actionDefinition.equals("cancel"))
            return new CancelAction<ViolationData, ActionList>();
        
        if (actionDefinition.startsWith("cmd:"))
            return parseCmdAction(actionDefinition.split(":", 2)[1]);

        if (actionDefinition.startsWith("log:"))
            return parseLogAction(actionDefinition.split(":", 2)[1]);



        throw new IllegalArgumentException("NoCheatPlus doesn't understand action '" + actionDefinition + "' at all");
    }



    /**
     * Parses the log action.
     * 
     * @param definition
     *            the definition
     * @return the action
     */
    protected Action<ViolationData, ActionList> parseLogAction(final String definition) {
        final String[] parts = definition.split(":");
        final String name = parts[0];
        final Object message = lib.get(parts[0]);
        int delay = 0;
        int repeat = 1;
        boolean toConsole = true;
        boolean toFile = true;
        boolean toChat = true;

        if (message == null)
            throw new IllegalArgumentException("NoCheatPlus doesn't know log message '" + name
                    + "'. Have you forgotten to define it?");

        try {
            delay = Integer.parseInt(parts[1]);
            repeat = Integer.parseInt(parts[2]);
            toConsole = parts[3].contains("c");
            toChat = parts[3].contains("i");
            toFile = parts[3].contains("f");
        } catch (final Exception e) {
        	LogUtil.logWarning("[NoCheatPlus] Couldn't parse details of log action '" + definition
                    + "', will use default values instead.");
        	LogUtil.logWarning(e);
            delay = 0;
            repeat = 1;
            toConsole = true;
            toFile = true;
            toChat = true;
        }

        return new LogAction(name, delay, repeat, toChat, toConsole, toFile, message.toString());
    }
}
