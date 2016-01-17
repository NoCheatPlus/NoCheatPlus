package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.AcceptDenyCounter;
import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.IAcceptDenyCounter;
import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.ICounterWithParent;
import fr.neatmonster.nocheatplus.workaround.IStagedWorkaround;
import fr.neatmonster.nocheatplus.workaround.IWorkaround;
import fr.neatmonster.nocheatplus.workaround.IWorkaroundRegistry;
import fr.neatmonster.nocheatplus.workaround.SimpleWorkaroundRegistry;
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
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use, a=1 (s/a/p)", wacd.getStageCounter(), wacd.getAllTimeCounter(), pc);

        // Reset.
        wacd.resetConditions();
        TestAcceptDenyCounters.checkSame(accept, deny, "Reset (s/a/p)",  wacd.getAllTimeCounter(), pc);
        TestAcceptDenyCounters.checkCounts(wacd.getStageCounter(), 0, 0, "test.wacd.stage");

        // Attempt to use a lot of times (all but one get denied).
        for (int i = 0; i < 141; i++) {
            checkCanUseAndUse(wacd);
        }
        accept *= 2;
        deny *= 2;
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use, repeat a=1, (s/a/p)", wacd.getAllTimeCounter(), pc);
        TestAcceptDenyCounters.checkCounts(wacd.getStageCounter(), 1, 140, "test.wacd.stage");

        // Set to 5 and use (5xaccept).
        wacd.resetConditions();
        wacd.setCurrentCount(5);
        for (int i = 0; i < 141; i++) {
            checkCanUseAndUse(wacd);
        }
        accept += 5;
        deny += 141 - 5;
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use, a=5, (s/a/p)", wacd.getAllTimeCounter(), pc);
        TestAcceptDenyCounters.checkCounts(wacd.getStageCounter(), 5, 141 - 5, "test.wacd.stage");

        // Set to -1 and use.
        wacd.resetConditions();
        wacd.setCurrentCount(-1);
        for (int i = 0; i < 141; i++) {
            checkCanUseAndUse(wacd);
        }
        deny += 141;
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use, a=0 (s/a/p)", wacd.getAllTimeCounter(), pc);
        TestAcceptDenyCounters.checkCounts(wacd.getStageCounter(), 0, 141, "test.wacd.stage");

        // Set to 14 and use (14xaccept).
        wacd.resetConditions();
        wacd.setCurrentCount(14);
        for (int i = 0; i < 141; i++) {
            checkCanUseAndUse(wacd);
        }
        accept += 14;
        deny += 141 - 14;
        TestAcceptDenyCounters.checkSame(accept, deny, "Just use, a=14 (s/a/p)", wacd.getAllTimeCounter(), pc);
        TestAcceptDenyCounters.checkCounts(wacd.getStageCounter(), 14, 141 - 14, "test.wacd.stage");

        // TODO: Might also test getNewInstance().

    }

    @Test
    public void testSimpleWorkaroundRegistry() {
        // (all sorts of tests. Consistency for workarounds and counters.).

        IWorkaroundRegistry reg = new SimpleWorkaroundRegistry();

        // Simple tests with registering individual instances.

        // Call get for non existing counter.
        if (reg.getGlobalCounter("exist.not") != null) {
            fail("getGlobalCounter: expect null for not registered id.");
        }
        // Create a counter.
        IAcceptDenyCounter c_man = reg.createGlobalCounter("c.man");
        if (c_man == null) {
            fail("createGlobalCounter: expect a counter");
        }
        // Ensure the same counter is returned as last time.
        if (reg.createGlobalCounter("c.man") != c_man) {
            fail("createGlobalCounter must return the same instance each time.");
        }

        // Register a single workaround (no parent counter).
        checkSetWorkaroundBluePrint(new WorkaroundCounter("wc.man"), reg);

        // Register a single workaround with a parent counter set (createed from registry).
        IWorkaround wrp = new WorkaroundCounter("wc.man.rp"); // With parent counter from registry.
        ((ICounterWithParent) wrp.getAllTimeCounter()).setParentCounter(c_man);
        checkSetWorkaroundBluePrint(wrp, reg);

        // Register a single workaround with an externally created parent counter set (not in registry).
        IWorkaround wep = new WorkaroundCounter("wc.man.ep"); // With externally created parent counter.
        ((ICounterWithParent) wep.getAllTimeCounter()).setParentCounter(new AcceptDenyCounter());
        checkSetWorkaroundBluePrint(wep, reg);

        // TODO: Test a WorkaroundSet with all types of workarounds. Groups, WorkaroundSet methods.
    }

    /**
     * Set blueprint and test:
     * <ul>
     * <li>global counter existence.</li>
     * </ul>
     * 
     * @param bluePrint
     * @param reg
     * @return The given bluePrint for chaining.
     */
    public static <W extends IWorkaround> W checkSetWorkaroundBluePrint(W bluePrint, IWorkaroundRegistry reg) {
        // Remember old settings.
        final String id = bluePrint.getId();
        IAcceptDenyCounter oldAllTimeCount = bluePrint.getAllTimeCounter();
        if (oldAllTimeCount == null) {
            fail("getAllTimeCounter must not return null: " + id);
        }
        IAcceptDenyCounter oldAllTimeParent = (bluePrint instanceof ICounterWithParent) ? ((ICounterWithParent) bluePrint).getParentCounter() : null;
        IAcceptDenyCounter stageCount = (bluePrint instanceof IStagedWorkaround) ? ((IStagedWorkaround) bluePrint).getStageCounter() : null;
        IAcceptDenyCounter oldRegCounter = reg.getGlobalCounter(id);
        // Register.
        reg.setWorkaroundBluePrint(bluePrint);
        // Demand existence of a counter for that id.
        IAcceptDenyCounter regCount = reg.getGlobalCounter(id);
        if (oldAllTimeParent != null && regCount == null) {
            fail("There must be a global counter present, if no parent counter was present at the time of registration: " + id);
        }
        // Demand existence of global counter, if none was set.
        if (oldAllTimeParent == null && reg.getGlobalCounter(id) == null) {
            fail("A parent counter must be present, after registering a workaround without a parent counter set: " + id);
        }
        // Demand no counter to be registered, if none was set and the bluePrint had a parent counter set.
        if (oldRegCounter == null && oldAllTimeParent != null && reg.getGlobalCounter(id) != null) {
            fail("Expect no counter to be registered, if none was and a parent had already been set: " + id);
        }
        // Demand the registered counter to stay the same, if it already existed.
        if (oldRegCounter != null && oldRegCounter != reg.getGlobalCounter(id)) {
            fail("Expect an already registeded counter not to change: " + id);
        }

        // Fetch an instance for this id.
        // W newInstance = reg.getWorkaround(id, bluePrint.getClass()); // FAIL
        IWorkaround newInstance = reg.getWorkaround(id);
        // Demand newInstancse is not bluePrint.
        if (newInstance == bluePrint) {
            fail("getWorkaround must not return the same instance: " + id);
        }
        // Demand class identity (for now).
        if (bluePrint.getClass() != newInstance.getClass()) {
            fail("Demand class identity for factory methods (subject to discussion: ): " + id);
        }
        // Demand identity of a global counter, if none was set before.
        if (oldAllTimeParent != null && oldAllTimeParent != newInstance.getAllTimeCounter()) {
            fail("Expect the global counter to be the same as the parent of a new instance, if none was set: " + id);
        }
        // Demand stage count to differ.
        if ((newInstance instanceof IStagedWorkaround) && ((IStagedWorkaround) newInstance).getStageCounter() == stageCount) {
            fail("Expect stage counter of a new instance to differ: " + id);
        }

        // (More specific stuff is possible.)

        return bluePrint;
    }


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
