package fr.neatmonster.nocheatplus;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;

public class TestMinecraftVersion {

    @Test
    public void testParseMinecraftVersion() {
        for (String[] pair : new String[][] {
                {null, ""},
                {"1.7.5", "1.7.5"},
                {"1.7.5", "1.7.5-R0.1-SNAPSHOT"},
                {"1.7.2", "git-Bukkit-1.7.2-R0.3-14-g8f8716c-b3042jnks"},
                {"1.8", "git-Spigot-081dfa5-7658819 (MC: 1.8)"}
        }) {
            String parsed = ServerVersion.parseMinecraftVersion(pair[1]);
            if (pair[0] == null) {
                if (parsed != null) {
                    fail("Expect null output on: " + pair[1] + ", got instead: " + parsed);
                }
            } else if (!pair[0].equals(parsed)) {
                fail("Expect " + pair[0] + " for input: " + pair[1] + ", got instead: " + parsed);
            }
        }
    }

}