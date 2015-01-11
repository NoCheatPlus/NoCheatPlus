package fr.neatmonster.nocheatplus;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.IdUtil;

public class TestIdUtil {

    @Test
    public void testMinecraftUserNames() {
        String[] valid = new String[] {
                "xdxdxd",
                "XDXDXD",
                "sa_Sd_ASD"
        };

        for (String name : valid) {
            if (!IdUtil.isValidMinecraftUserName(name)) {
                fail("Expect user name to be valid: " + name);
            }
        }

        String[] inValid = new String[] {
                "xd xd xd",
                "",
                "x",
                "0123456789abcdefX",
                "*§$FUJAL"
        };

        for (String name : inValid) {
            if (IdUtil.isValidMinecraftUserName(name)) {
                fail("Expect user name to be invalid: " + name);
            }
        }

    }

}