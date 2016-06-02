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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.AcceptDenyCounter;
import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.IAcceptDenyCounter;
import fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny.ICounterWithParent;
import fr.neatmonster.nocheatplus.workaround.IStagedWorkaround;
import fr.neatmonster.nocheatplus.workaround.IWorkaround;
import fr.neatmonster.nocheatplus.workaround.IWorkaroundRegistry;
import fr.neatmonster.nocheatplus.workaround.IWorkaroundRegistry.WorkaroundSet;
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

        // Register a single workaround with a parent counter set (created from registry).
        IWorkaround wrp = new WorkaroundCounter("wc.man.rp"); // With parent counter from registry.
        ((ICounterWithParent) wrp.getAllTimeCounter()).setParentCounter(c_man);
        checkSetWorkaroundBluePrint(wrp, reg);

        // Register a single workaround with an externally created parent counter set (not in registry).
        IWorkaround wep = new WorkaroundCounter("wc.man.ep"); // With externally created parent counter.
        ((ICounterWithParent) wep.getAllTimeCounter()).setParentCounter(new AcceptDenyCounter());
        checkSetWorkaroundBluePrint(wep, reg);

        // WorkaroundSet

        // Register workarounds.
        List<WorkaroundCounter> wg1 = getWorkaroundCounters("w.man", 15);
        List<WorkaroundCountDown> wg2 = getWorkaroundcountDowns("w.man", 15);
        List<IWorkaround> wgAll = new ArrayList<IWorkaround>(30);
        wgAll.addAll(wg1);
        wgAll.addAll(wg2);
        try {
            reg.getCheckedIdSet(wg1);
            fail("Expect IllegalArgumentException for not registered workarounds.");
        }
        catch (IllegalArgumentException ex) {
            // Success.
        }
        reg.setWorkaroundBluePrint(wgAll.toArray(new IWorkaround[2 * 15]));
        List<String> ids1 = new ArrayList<String>(reg.getCheckedIdSet(wg1));
        List<String> ids2 = new ArrayList<String>(reg.getCheckedIdSet(wg2));
        List<String> idsAll = new ArrayList<String>(reg.getCheckedIdSet(wgAll));
        // Register groups.
        reg.setGroup("group.mix", Arrays.asList(ids1.get(0), ids2.get(0)));
        reg.setGroup("group.wc", ids1);
        reg.setGroup("group.wcd", ids2);
        // reg.setWorkaroundSet with string ids.
        reg.setWorkaroundSetByIds("ws.all", idsAll, "group.mix", "group.wc", "group.wcd");
        // reg.getWorkaroundSet.
        WorkaroundSet ws = reg.getWorkaroundSet("ws.all");
        // Test the WorkaroundSet
        for (String id : idsAll) {
            ws.getWorkaround(id);
        }
        // Test reset all.
        useAll(idsAll, ws);
        int accept = 1;
        int deny = 0;
        checkAllTimeCount(idsAll, ws, accept, deny);
        checkStageCount(ids2, ws, 1, 0);
        ws.resetConditions();
        checkAllTimeCount(idsAll, ws, accept, deny);
        checkStageCount(ids2, ws, 0, 0);
        // Reset group.wc.
        useAll(idsAll, ws);
        accept += 1;
        ws.resetConditions("group.wc");
        checkAllTimeCount(idsAll, ws, accept, deny);
        checkStageCount(ids2, ws, 1, 0);
        ws.resetConditions();
        // group.wcd
        useAll(idsAll, ws);
        accept += 1;
        ws.resetConditions("group.wcd");
        checkAllTimeCount(idsAll, ws, accept, deny);
        checkStageCount(ids2, ws, 0, 0);
        ws.resetConditions();
        // group.mix
        useAll(idsAll, ws);
        accept += 1;
        ws.resetConditions("group.mix");
        checkAllTimeCount(idsAll, ws, accept, deny);
        TestAcceptDenyCounters.checkCounts(((IStagedWorkaround) (ws.getWorkaround(ids2.get(0)))).getStageCounter(), 0, 0, "stageCounter/" + ids2.get(0));
        for (int i = 1; i < ids2.size(); i++) {
            TestAcceptDenyCounters.checkCounts(((IStagedWorkaround) (ws.getWorkaround(ids2.get(i)))).getStageCounter(), 1, 0, "stageCounter/" + ids2.get(i));
        }
        ws.resetConditions();
        // TODO: Individual group reset (needs half of group.wcd).

        // TODO: More details/cases (also failure cases, exceptions, etc).

    }

    /**
     * Get a collection of new WorkaroundCounter instances.
     * 
     * @param name
     *            Prefix of naming name.class.count
     * @param repeatCount
     * @return
     */
    public static List<WorkaroundCounter> getWorkaroundCounters(String name, int repeatCount) {
        final List<WorkaroundCounter> workarounds = new ArrayList<WorkaroundCounter>();
        for (int i = 0; i < repeatCount; i++) {
            workarounds.add(new WorkaroundCounter(name + ".WorkaroundCounter." + i));
        }
        return workarounds;
    }

    /**
     * Get a collection of new WorkaroundCountDown instances, initialized with
     * counting up from 1.
     * 
     * @param name
     *            Prefix of naming name.class.count
     * @param repeatCount
     * @return
     */
    public static List<WorkaroundCountDown> getWorkaroundcountDowns(String name, int repeatCount) {
        final List<WorkaroundCountDown> workarounds = new ArrayList<WorkaroundCountDown>();
        for (int i = 0; i < repeatCount; i++) {
            workarounds.add(new WorkaroundCountDown(name + ".WorkaroundCountDown." + i, i + 1));
        }
        return workarounds;
    }

    public static void useAll(Collection<String> ids, WorkaroundSet ws) {
        for (String id : ids) {
            ws.use(id);
        }
    }

    public static void checkStageCount(Collection<String> ids, WorkaroundSet ws, int acceptCount, int denyCount) {
        for (String id : ids) {
            IAcceptDenyCounter counter = ((IStagedWorkaround) ws.getWorkaround(id)).getStageCounter();
            TestAcceptDenyCounters.checkCounts(counter, acceptCount, denyCount, "stageCounter/" + id);
        }
    }

    public static void checkAllTimeCount(Collection<String> ids, WorkaroundSet ws, int acceptCount, int denyCount) {
        for (String id : ids) {
            IAcceptDenyCounter counter = ws.getWorkaround(id).getAllTimeCounter();
            TestAcceptDenyCounters.checkCounts(counter, acceptCount, denyCount, "allTimeCounter/" + id);
        }
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
