/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.compat.versions.GenericVersion;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;

public class TestMinecraftVersion {

    @Test
    public void testParseMinecraftVersion() {
        for (String[] pair : new String[][] {
                {null, ""},
                {"1.7.5", "1.7.5"},
                {"1.7.5", "1.7.5-R0.1-SNAPSHOT"},
                {"1.7.2", "git-Bukkit-1.7.2-R0.3-14-g8f8716c-b3042jnks"},
                {"1.8", "git-Spigot-081dfa5-7658819 (MC: 1.8)"},
                {"1.7.10", "random-123-Cauldron-MCPC-PLUS-1.7.10-4-5-6-7-aed425aed1"}
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

        // Expect -1
        for (String[] pair : new String[][] {
                {"1.8", "1.8.8"}
        }) {
            testCompare(pair[0], pair[1], -1);
        };

        // Expect 1
        for (String[] pair : new String[][] {
                {"1.8.8", "1.8"}
        }) {
            testCompare(pair[0], pair[1], 1);
        };
    }

    private void testCompare(String v1, String v2, int expectedResult) {
        int res = GenericVersion.compareVersions(v1, v2);
        if (res != expectedResult) {
            fail("Comparing " + v1 + " with " + v2 + " should result in " + expectedResult + ", got instead: " + res);
        }
    }

}