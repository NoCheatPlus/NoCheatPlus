package fr.neatmonster.nocheatplus.test;

import org.junit.Test;

import fr.neatmonster.nocheatplus.PluginTests;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.CheckType.CheckTypeType;
import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;

/**
 * Uh oh.
 * @author asofold
 *
 */
public class TestDump {
    @Test
    public void dumpCheckSilentPermissionForPluginYML() {
        PluginTests.setUnitTestNoCheatPlusAPI(false);
        for (CheckType checkType : CheckType.values()) {
            final RegisteredPermission permission = checkType.getPermission();
            // Only add for actual checks at present.
            if (permission == null || checkType.getType() != CheckTypeType.CHECK) {
                continue;
            }
            // Print:
            System.out.print("    " + permission.getStringRepresentation() + ".silent:\n        default: false\n");
        }
    }
}
