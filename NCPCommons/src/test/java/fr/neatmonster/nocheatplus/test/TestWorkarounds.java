package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.AcceptDenyCounter;
import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.ICounterWithParent;
import fr.neatmonster.nocheatplus.workaround.IWorkaround;
import fr.neatmonster.nocheatplus.workaround.WorkaroundCountDown;
import fr.neatmonster.nocheatplus.workaround.WorkaroundCounter;

public class TestWorkarounds {

    /**
     * Simple isolated testing for one WorkaroundCounter instance, plus parent
     * count.
     */
    @Test
    public void testWorkaroundCounter() {
        WorkaroundCounter wac = new WorkaroundCounter("test.wac");
        AcceptDenyCounter pc = new AcceptDenyCounter();
        ((ICounterWithParent) wac.getAllTimeCounter()).setParentCounter(pc);

        for (int i = 0; i < 57; i++) {
            checkCanUseAndUse(wac);
        }
        TestAcceptDenyCounters.checkSame(57, 0, "WorkaroundCounter(c/p)", wac.getAllTimeCounter(), pc);
    }

    /**
     * Simple isolated testing for one WorkaroundCountDown instance, plus parent
     * count.
     */
    @Test
    public void testWorkaroundCountDown() {
        WorkaroundCountDown wacd = new WorkaroundCountDown("test.wacd", 1);
        AcceptDenyCounter pc = new AcceptDenyCounter();
        ((ICounterWithParent) wacd.getAllTimeCounter()).setParentCounter(pc);

        // Attempt to use a lot of times (all but one get denied).
        for (int i = 0; i < 141; i++) {
            checkCanUseAndUse(wacd);
        }
        int accept = 1; // All time count.
        int deny = 140; // All time count.
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use(s/a/p)", wacd.getStageCounter(), wacd.getAllTimeCounter(), pc);

        // Reset.
        wacd.resetConditions();
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use(s/a/p)",  wacd.getAllTimeCounter(), pc);
        TestAcceptDenyCounters.checkCounts(wacd.getStageCounter(), 0, 0, "test.wacd.stage");

        // Attempt to use a lot of times (all but one get denied).
        for (int i = 0; i < 141; i++) {
            checkCanUseAndUse(wacd);
        }
        accept *= 2;
        deny *= 2;
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use(s/a/p)", wacd.getAllTimeCounter(), pc);
        TestAcceptDenyCounters.checkCounts(wacd.getStageCounter(), 1, 140, "test.wacd.stage");

        // Set to 5 and use (5xaccept).
        wacd.resetConditions();
        wacd.setCurrentCount(5);
        for (int i = 0; i < 141; i++) {
            checkCanUseAndUse(wacd);
        }
        accept += 5;
        deny += 141 - 5;
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use(s/a/p)", wacd.getAllTimeCounter(), pc);
        TestAcceptDenyCounters.checkCounts(wacd.getStageCounter(), 5, 141 - 5, "test.wacd.stage");

        // Set to -1 and use.
        wacd.resetConditions();
        wacd.setCurrentCount(-1);
        for (int i = 0; i < 141; i++) {
            checkCanUseAndUse(wacd);
        }
        deny += 141;
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use(s/a/p)", wacd.getAllTimeCounter(), pc);
        TestAcceptDenyCounters.checkCounts(wacd.getStageCounter(), 0, 141, "test.wacd.stage");

        // Set to 14 and use (14xaccept).
        wacd.resetConditions();
        wacd.setCurrentCount(14);
        for (int i = 0; i < 141; i++) {
            checkCanUseAndUse(wacd);
        }
        accept += 14;
        deny += 141 - 14;
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use(s/a/p)", wacd.getAllTimeCounter(), pc);
        TestAcceptDenyCounters.checkCounts(wacd.getStageCounter(), 14, 141 - 14, "test.wacd.stage");

    }

    // TODO: Test SimpleWorkaroundRegistry and WorkaroundSet (all sorts of tests. Consistency for workarounds and counters.).

    /**
     * Check consistency of results of canUse and use called in that order.
     * 
     * @param workaround
     * @return Result of use().
     */
    public static boolean checkCanUseAndUse(IWorkaround workaround) {
        boolean preRes = workaround.canUse();
        boolean res = workaround.use();
        if (!preRes && res) {
            fail("Inconsistency: use() must not return true, if canUse() has returned false.");
        }

        return res;
    }

}
