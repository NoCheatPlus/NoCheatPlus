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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

public class TestBlockFlags {

    @Test
    public void testIfFlagsAreUnique() {
        final Collection<String> flags = BlockProperties.getAllFlagNames();
        final Set<Long> occupied = new HashSet<Long>();
        for (final String name : flags) {
            final long flag = BlockProperties.parseFlag(name);
            if (flag == 0L) {
                fail("Flag '" + name + "' must not be 0L.");
            }
            if (occupied.contains(flag)) {
                fail("Flag '" + flag + "' already is occupied.");
            }
            occupied.add(flag);
        }
    }

}
