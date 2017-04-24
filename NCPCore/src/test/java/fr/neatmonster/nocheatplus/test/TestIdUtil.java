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