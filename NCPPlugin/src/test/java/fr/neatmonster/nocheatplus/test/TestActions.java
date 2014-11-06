package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.actions.types.LogAction;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.DefaultConfig;

public class TestActions {
    @Test
    public void testOptimizedLogActionPrefixes() {
        final ConfigFile config = new DefaultConfig();
        config.set("actions", "log:dummy:0:0:icf");
        config.set("strings.dummy", "dummy");
        config.set(ConfPaths.LOGGING_BACKEND_CONSOLE_PREFIX, "console_dummy");
        config.set(ConfPaths.LOGGING_BACKEND_FILE_PREFIX, "file_dummy");
        config.set(ConfPaths.LOGGING_BACKEND_INGAMECHAT_PREFIX, "ingame_dummy");
        ActionList actionList = config.getOptimizedActionList("actions", "dummy");
        Action<ViolationData, ActionList>[] actions = actionList.getActions(0.0);
        if (actions.length != 1) {
            fail("Wrong number of actions.");
        }
        if (actions[0] instanceof LogAction) {
            LogAction action = (LogAction) actions[0];
            testString(action.prefixChat, "ingame_dummy");
            testString(action.prefixFile, "file_dummy");
            testString(action.prefixConsole, "console_dummy");
        } else {
            fail("Expect log action.");
        }
        
    }
    
    private static void testString(String value, String match) {
        if (!match.equals(value)) {
            fail("Expect '" + match + "', got instead: '" + value + "'");
        }
    }
    
    @Test
    public void testOptimizedLogActionEmpty() {
        final ConfigFile config = new DefaultConfig();
        config.set("actions", "log:dummy:0:0:icf");
        config.set("strings.dummy", "dummy");
        config.set(ConfPaths.LOGGING_ACTIVE, false);
        ActionList actionList = config.getOptimizedActionList("actions", "dummy");
        Action<ViolationData, ActionList>[] actions = actionList.getActions(0.0);
        if (actions.length != 0) {
            fail("Wrong number of actions.");
        }
    }
    
}
