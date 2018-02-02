package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.permissions.PermissionPolicy;
import fr.neatmonster.nocheatplus.permissions.PermissionSettings;
import fr.neatmonster.nocheatplus.permissions.PermissionSettings.PermissionRule;

public class TestPermissionSettings {

    @Test
    public void testRegex() {
        PermissionPolicy dummy = new PermissionPolicy();
        String regex = "^nocheatplus\\.checks\\..*\\.silent$";
        String permissionName = "nocheatplus.checks.moving.survivalfly.silent";
        // Also/rather a config test. 
        if (!permissionName.matches(regex)) {
            fail("Expect regex to match.");
        }
        PermissionRule rule = PermissionSettings.getMatchingRule("regex:" + regex, dummy);
        if (rule == null) {
            fail("Expect factory to return a regex rule.");
        }
        if (!rule.matches(permissionName)) {
            fail("Expect rule to match permissions name.");
        }
        if (rule.matches("xy" + permissionName) || rule.matches(permissionName + "yx")) {
            fail("Rule matches wrong start/end.");
        }
    }

}
