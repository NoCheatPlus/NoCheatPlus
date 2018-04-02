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
