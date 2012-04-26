package fr.neatmonster.nocheatplus.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.actions.types.ConsolecommandAction;
import fr.neatmonster.nocheatplus.actions.types.DummyAction;
import fr.neatmonster.nocheatplus.actions.types.LogAction;
import fr.neatmonster.nocheatplus.actions.types.SpecialAction;

/**
 * Helps with creating Actions out of text string definitions
 * 
 */
public class ActionFactory {

    private static final Map<String, Object> lib = new HashMap<String, Object>();

    public ActionFactory(final Map<String, Object> library) {
        lib.putAll(library);
    }

    public Action createAction(String actionDefinition) {

        actionDefinition = actionDefinition.toLowerCase();

        if (actionDefinition.equals("cancel"))
            return new SpecialAction();

        if (actionDefinition.startsWith("log:"))
            return parseLogAction(actionDefinition.split(":", 2)[1]);

        if (actionDefinition.startsWith("cmd:"))
            return parseCmdAction(actionDefinition.split(":", 2)[1]);

        throw new IllegalArgumentException("NoCheatPlus doesn't understand action '" + actionDefinition + "' at all");
    }

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
                System.out.println("NoCheatPlus couldn't parse action definition 'vl:" + s + "'");
            }
        }

        return list;
    }

    public Action[] createActions(final String... definitions) {
        final List<Action> actions = new ArrayList<Action>();

        for (final String def : definitions) {
            if (def.length() == 0)
                continue;
            try {
                actions.add(createAction(def));
            } catch (final IllegalArgumentException e) {
                System.out.println(e.getMessage());
                actions.add(new DummyAction(def));
            }
        }

        return actions.toArray(new Action[actions.size()]);
    }

    private Action parseCmdAction(final String definition) {
        final String[] parts = definition.split(":");
        final String name = parts[0];
        final Object command = lib.get(parts[0]);
        int delay = 0;
        int repeat = 1;

        if (command == null)
            throw new IllegalArgumentException("NoCheatPlus doesn't know command '" + name
                    + "'. Have you forgotten to define it?");

        if (parts.length > 1)
            try {
                delay = Integer.parseInt(parts[1]);
                repeat = Integer.parseInt(parts[2]);
            } catch (final Exception e) {
                System.out.println("NoCheatPlus couldn't parse details of command '" + definition
                        + "', will use default values instead.");
                delay = 0;
                repeat = 1;
            }

        return new ConsolecommandAction(name, delay, repeat, command.toString());
    }

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
            System.out.println("NoCheatPlus couldn't parse details of log action '" + definition
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
