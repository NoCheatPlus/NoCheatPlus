package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.AcceptDenyCounter;
import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.IAcceptDenyCounter;

public class TestAcceptDenyCounters {

    /**
     * Test the default implementation.
     */
    @Test
    public void testAcceptDenyCounter() {
        AcceptDenyCounter leaf = new AcceptDenyCounter();
        AcceptDenyCounter parent = new AcceptDenyCounter();
        leaf.setParentCounter(parent);

        // Add to leaf.
        for (int i = 0; i < 73; i++) {
            leaf.accept();
        }
        checkCounts(leaf, 73, 0, "leaf");

        for (int i = 0; i < 65; i++) {
            leaf.deny();
        }
        checkCounts(leaf, 73, 65, "leaf");

        // Add only to parent.
        for (int i = 0; i < 52; i++) {
            parent.accept();
        }
        checkCounts(parent, 73 + 52, 65, "parent");
        checkCounts(leaf, 73, 65, "leaf");

        for (int i = 0; i < 97; i++) {
            parent.deny();
        }
        checkCounts(parent, 73 + 52, 65 + 97, "parent");
        checkCounts(leaf, 73, 65, "leaf");

        // Reset parent.
        parent.resetCounter();
        checkCounts(parent, 0, 0, "parent");
        checkCounts(leaf, 73, 65, "leaf");
    }

    public void checkCounts(IAcceptDenyCounter counter, int acceptCount, int denyCount, String counterName) {
        if (counter.getAcceptCount() != acceptCount) {
            fail("Wrong accept count for counter '" + counterName + "': " + counter.getAcceptCount() + " instead of " + acceptCount + ".");
        }
        if (counter.getDenyCount() != denyCount) {
            fail("Wrong deny count for counter '" + counterName + "': " + counter.getDenyCount() + " instead of " + denyCount + ".");
        }
    }

}
