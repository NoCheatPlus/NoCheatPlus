package fr.neatmonster.nocheatplus.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.nocheatplus.actions.types.CancelAction;
import fr.neatmonster.nocheatplus.actions.types.CommandAction;
import fr.neatmonster.nocheatplus.actions.types.DummyAction;
import fr.neatmonster.nocheatplus.actions.types.LogAction;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

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
public class ActionFactory {
    private static final Map<String, Object> lib = new HashMap<String, Object>();

    /**
     * Instantiates a new action factory.
     * 
     * @param library
     *            the library
     */
    public ActionFactory(final Map<String, Object> library) {
        lib.putAll(library);
    }

    /**
     * Creates a new Action object.
     * 
     * @param actionDefinition
     *            the action definition
     * @return the action
     */
    public Action createAction(String actionDefinition) {
        actionDefinition = actionDefinition.toLowerCase();

        if (actionDefinition.equals("cancel"))
            return new CancelAction();

        if (actionDefinition.startsWith("log:"))
            return parseLogAction(actionDefinition.split(":", 2)[1]);

        if (actionDefinition.startsWith("cmd:"))
            return parseCmdAction(actionDefinition.split(":", 2)[1]);

        throw new IllegalArgumentException("NoCheatPlus doesn't understand action '" + actionDefinition + "' at all");
    }

    /**
     * Creates a new Action object.
     * 
     * @param definition
     *            the definition
     * @param permission
     *            the permission
     * @return the action list
     */
    public ActionList createActionList(final String definition, final String permission) {
        final ActionList list = new ActionList(permission);

        boolean first = true;

        for (String s : definition.split("vl>")) {
            s = s.trim();

            if (s.length() == 0) {
                first = false;
                continue;
            }

            try {
                Integer vl;
                String def;
                if (first) {
                    first = false;
                    vl = 0;
                    def = s;
                } else {
                    final String[] listEntry = s.split("\\s+", 2);
                    vl = Integer.parseInt(listEntry[0]);
                    def = listEntry[1];
                }
                list.setActions(vl, createActions(def.split("\\s+")));
            } catch (final Exception e) {
                CheckUtils.logWarning("[NoCheatPlus] Couldn't parse action definition 'vl:" + s + "'.");
            }
        }

        return list;
    }

    /**
     * Creates a new Action object.
     * 
     * @param definitions
     *            the definitions
     * @return the action[]
     */
    public Action[] createActions(final String... definitions) {
        final List<Action> actions = new ArrayList<Action>();

        for (final String def : definitions) {
            if (def.length() == 0)
                continue;
            try {
                actions.add(createAction(def));
            } catch (final IllegalArgumentException e) {
            	CheckUtils.logWarning("[NoCheatPlus] Failed to create action: " + e.getMessage());
                actions.add(new DummyAction(def));
            }
        }

        return actions.toArray(new Action[actions.size()]);
    }

    /**
     * Parses the cmd action.
     * 
     * @param definition
     *            the definition
     * @return the action
     */
    private Action parseCmdAction(final String definition) {
        final String[] parts = definition.split(":");
        final String name = parts[0];
        final Object command = lib.get(parts[0]);
        int delay = 0;
        int repeat = 0;

        if (command == null)
            throw new IllegalArgumentException("NoCheatPlus doesn't know command '" + name
                    + "'. Have you forgotten to define it?");

        if (parts.length > 1)
            try {
                delay = Integer.parseInt(parts[1]);
                repeat = Integer.parseInt(parts[2]);
            } catch (final Exception e) {
            	CheckUtils.logWarning("[NoCheatPlus] Couldn't parse details of command '" + definition
                        + "', will use default values instead.");
                delay = 0;
                repeat = 0;
            }

        return new CommandAction(name, delay, repeat, command.toString());
    }

    /**
     * Parses the log action.
     * 
     * @param definition
     *            the definition
     * @return the action
     */
    private Action parseLogAction(final String definition) {
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
        	CheckUtils.logWarning("[NoCheatPlus] Couldn't parse details of log action '" + definition
                    + "', will use default values instead.");
            e.printStackTrace();
            delay = 0;
            repeat = 1;
            toConsole = true;
            toFile = true;
            toChat = true;
        }

        return new LogAction(name, delay, repeat, toChat, toConsole, toFile, message.toString());
    }
}
