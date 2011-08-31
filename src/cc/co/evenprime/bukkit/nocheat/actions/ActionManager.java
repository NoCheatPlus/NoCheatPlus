package cc.co.evenprime.bukkit.nocheat.actions;

import java.util.HashMap;
import java.util.Map;

import cc.co.evenprime.bukkit.nocheat.actions.types.Action;
import cc.co.evenprime.bukkit.nocheat.actions.types.SpecialAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.ConsolecommandAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;
import cc.co.evenprime.bukkit.nocheat.log.LogManager;

/**
 * The ActionManager creates the specific actions and stores them with their
 * unique name.
 * 
 * @author Evenprime
 * 
 */
public class ActionManager {

    private final Map<String, Action> actionIdsToActionMap = new HashMap<String, Action>();

    private final LogManager          log;

    private final String[]            knownTypes           = {"log", "consolecommand", "special"};

    public ActionManager(LogManager log) {
        this.log = log;
    }

    /**
     * 
     * @param name
     *            To identify the action by a name. Same name means same action
     * 
     * @param actionId
     *            Actual string describing the action (may be more than a string
     *            in future)
     * @return
     */
    public void createActionFromStrings(String type, String actionId, String stringDelay, String stringRepeat, String theRest) {

        Action action = null;

        type = type.toLowerCase();
        actionId = actionId.toLowerCase();

        int delay;
        try {
            delay = Integer.parseInt(stringDelay);
        } catch(NumberFormatException e) {
            log.logToConsole(LogLevel.HIGH, "Can't parse action: \"" + actionId + "\", first parameter " + stringDelay + " not a number");
            return;
        }

        int repeat;
        try {
            repeat = Integer.parseInt(stringRepeat);
        } catch(NumberFormatException e) {
            log.logToConsole(LogLevel.HIGH, "Can't parse action: \"" + actionId + "\", second parameter " + stringRepeat + " not a number");
            return;
        }

        // Log actions have the form delay|repeat|level|message
        if(type.equals("log")) {

            String[] pieces = null;

            if(theRest != null) {
                pieces = theRest.split("\\s+", 2);
                if(pieces == null || pieces.length < 2) {
                    log.logToConsole(LogLevel.HIGH, "Can't parse log action: \"" + actionId + "\", missing parameters");
                    return;
                }
            }

            action = createLogActionFromString(delay, repeat, pieces[0], pieces[1]);
        } else if(type.equals("special")) {
            action = createCancelActionFromString(delay, repeat);
        } else if(type.equals("consolecommand")) {
            if(theRest == null) {
                log.logToConsole(LogLevel.HIGH, "Can't parse consolecommand action: \"" + actionId + "\", missing parameter");
                return;
            }
            action = createConsolecommandActionFromString(delay, repeat, theRest);
        } else {
            log.logToConsole(LogLevel.HIGH, "Can't parse action: \"" + actionId + "\", unknown action type");
            return;
        }

        if(action != null) {
            this.actionIdsToActionMap.put(actionId, action);
        }
    }

    public String[] getKnownTypes() {
        return knownTypes;
    }

    private ConsolecommandAction createConsolecommandActionFromString(int delay, int repeat, String command) {

        return new ConsolecommandAction(delay, repeat, command);
    }

    private SpecialAction createCancelActionFromString(int delay, int repeat) {

        return new SpecialAction(delay, repeat);

    }

    private Action createLogActionFromString(int delay, int repeat, String logLevel, String message) {

        LogLevel level = LogLevel.getLogLevelFromString(logLevel);

        if(level.equals(LogLevel.OFF)) {
            return null;
        }

        return new LogAction(delay, repeat, level, message);
    }

    public Action getActionByName(String name) {
        return actionIdsToActionMap.get(name.toLowerCase());
    }
}
