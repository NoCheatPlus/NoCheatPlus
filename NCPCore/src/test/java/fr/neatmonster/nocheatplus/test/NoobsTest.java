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

public class NoobsTest {

    @Test
    public void testSmallDoubles() {
        double x;
        x = Double.MIN_VALUE;
        if (x <= 0.0 || !(x > 0.0)) {
            fail("noob");
        }
        x = -Double.MIN_VALUE;
        if (x >= 0.0 || !(x < 0.0)) {
            fail("noob");
        }
    }

    @Test
    public void testSimpleRegex() {
        String ncpMovingAndFurther = "(^|.*,)(ncp\\.moving($|,.*|\\..*))";
        String[][] samplesMatch = new String[][] {
            {"x|y", "^x\\|y$"},
            {"dummy", "dummy"},
            {"lark", "(park|lark|bark)"},
            {"lark", "(park|(lark$)|bark)"},
            {"ncp.moving.survivalfly.hover", "(^|.*,)(ncp\\.moving\\.).*"}, // Simplified
            {"ncp.moving.survivalfly.hover", ncpMovingAndFurther},
            {"ncp.moving", ncpMovingAndFurther},
            {"ncp.moving,random", ncpMovingAndFurther},
            {"random,ncp.moving", ncpMovingAndFurther},
            {"random,ncp.moving,fandom", ncpMovingAndFurther},
        };
        String[][] samplesNotMatch = new String[][] {
            {"dummy", "yummd"},
            {"larkX", "(park|(lark$)|bark)"},
            {"ncp.movingApes", ncpMovingAndFurther},
            {"ncp.moving.survivalfly.hover", "ncp\\.moving($|,.*)"},
        };
        for (final String[] pair : samplesMatch) {
            if (!pair[0].matches(pair[1])) {
                fail("Expect match: " + pair[0] + " <- " + pair[1]);
            }
        }
        for (final String[] pair : samplesNotMatch) {
            if (pair[0].matches(pair[1])) {
                fail("Expect not match: " + pair[0] + " <- " + pair[1]);
            }
        }
    }

}
