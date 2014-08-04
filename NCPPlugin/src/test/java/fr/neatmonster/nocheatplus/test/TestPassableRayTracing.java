package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.bukkit.Material;
import org.junit.Test;

import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.utilities.FakeBlockCache;
import fr.neatmonster.nocheatplus.utilities.PassableRayTracing;

public class TestPassableRayTracing {
    
    // TODO: Moving into a block, moving out of a block, just moving on ground, moving up stairs etc.
    
    public TestPassableRayTracing() {
        LogUtil.setUseBukkitLogger(false);
        BlockTests.initBlockProperties();
        LogUtil.setUseBukkitLogger(true);
    }
    
    @Test
    public void testAir() {
        FakeBlockCache bc = new FakeBlockCache();
        PassableRayTracing rt = new PassableRayTracing();
        rt.setBlockCache(bc);
        rt.set(0.5, 0.5, -0.5, 0.5, 0.5, 1.5);
        rt.loop();
        if (rt.collides()) {
            fail("Expect not to collide when moving through a block.");
        }
        if (rt.getStepsDone() > 4) {
            fail("Expect less than 4 steps for moving straight through a block of air.");
        }
        rt.cleanup();
        bc.cleanup();
    }
    
    @Test
    public void testThroughOneBlock() {
        FakeBlockCache bc = new FakeBlockCache();
        bc.set(0, 0, 0, Material.STONE);
        PassableRayTracing rt = new PassableRayTracing();
        rt.setBlockCache(bc);
        double[][] setups = new double[][] {
            // Through the middle of the block.
            {0.5, 0.5, -0.5, 0.5, 0.5, 1.5},
            {-0.5, 0.5, 0.5, 1.5, 0.5, 0.5},
            {0.5, -0.5, 0.5, 0.5, 1.5, 0.5},
            // Along the edges.
            {0.5, 0.0, -0.5, 0.5, 0.0, 1.5},
            {-0.5, 0.0, 0.5, 1.5, 0.0, 0.5},
            // Exactly diagonal.
            {-0.5, -0.5, -0.5, 1.5, 1.5, 1.5}, // 3d
            {-0.5, 0.0, -0.5, 1.5, 0.0, 1.5}, // 2d
            // Through a corner.
            {1.2, 0.5, 0.5, 0.5, 0.5, 1.2},
            
            // TODO: More of each and other... + generic set-ups?
        };
        BlockTests.runCoordinates(rt, setups, true, false, 2.0, true);
        rt.cleanup();
        bc.cleanup();
    }
    
//    /**
//     * Moving diagonally through an "empty corner", seen from above:<br>
//     * ox<br>
//     * xo
//     */
//    @Test
//    public void testEmptyCorner() {
//        FakeBlockCache bc = new FakeBlockCache();
//        // The "empty corner" setup.
//        bc.set(10, 70, 10, Material.STONE);
//        bc.set(11, 70, 11, Material.STONE);
//        // Ground.
//        for (int x = 9; x < 13; x++) {
//            for (int z = 9; z < 13; z++) {
//                bc.set(x, 69, z, Material.STONE);
//            }
//        }
//        PassableRayTracing rt = new PassableRayTracing();
//        rt.setBlockCache(bc);
//        // TODO: More Directions, over a corner, sides, etc.
//        double[][] setups = new double[][] {
//            // Slightly off the middle (11, y, 11)
//            {11.4, 70.0, 10.4, 10.6, 70.0, 11.4},
//            // Going exactly through the middle (11, y, 11)
//            {11.4, 70.0, 10.6, 10.6, 70.0, 11.4},
//            {11.5, 70.0, 10.5, 10.5, 70.0, 11.5},
//        };
//        BlockTests.runCoordinates(rt, setups, true, false, 2.0, true);
//        rt.cleanup();
//        bc.cleanup();
//    }
    
}
