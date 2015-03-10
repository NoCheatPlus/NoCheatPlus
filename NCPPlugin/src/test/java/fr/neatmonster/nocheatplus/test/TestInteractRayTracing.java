package fr.neatmonster.nocheatplus.test;

import org.bukkit.Material;
import org.junit.Test;

import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.FakeBlockCache;
import fr.neatmonster.nocheatplus.utilities.InteractRayTracing;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;

public class TestInteractRayTracing {

    public final class CenteredInteractRayTracing extends InteractRayTracing {
        private int centerX, centerY, centerZ;
        public CenteredInteractRayTracing(boolean strict, int centerX, int centerY, int centerZ) {
            super(strict);
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
        }
        @Override
        public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
            super.set(x0, y0, z0, x1, y1, z1, centerX, centerY, centerZ);
        }
    }

    // TODO: Blunt copy and paste from TestPassableRayTracing, add something that makes sense.

    public TestInteractRayTracing() {
        StaticLog.setUseLogManager(false);
        BlockTests.initBlockProperties();
        StaticLog.setUseLogManager(true);
    }

    @Test
    public void testAir() {
        FakeBlockCache bc = new FakeBlockCache();
        InteractRayTracing rt = new InteractRayTracing();
        rt.setBlockCache(bc);
        double[] coords = new double[]{0.5, 0.5, -0.5, 0.5, 0.5, 1.5};
        rt.set(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
        rt.loop();
        if (rt.collides()) {
            TestRayTracing.doFail("Expect not to collide with air.", coords);
        }
        if (rt.getStepsDone() > 4) {
            TestRayTracing.doFail("Expect less than 4 steps for moving straight through a block of air.", coords);
        }
        rt.cleanup();
        bc.cleanup();
    }

    /**
     * Moving diagonally through an "empty corner", seen from above:<br>
     * ox<br>
     * xo
     */
    @Test
    public void testEmptyCorner() {
        FakeBlockCache bc = new FakeBlockCache();
        // The "empty corner" setup.
        for (int y = 70 ; y < 73; y ++) {
            bc.set(10, y, 10, Material.STONE);
            bc.set(11, y, 11, Material.STONE);
        }
        // Ground.
        for (int x = 9; x < 13; x++) {
            for (int z = 9; z < 13; z++) {
                bc.set(x, 69, z, Material.STONE);
            }
        }
        // TODO: Make work with strict set to false.
        InteractRayTracing rt = new InteractRayTracing(true);
        //InteractRayTracing rt = new InteractRayTracing(false);
        rt.setBlockCache(bc);
        // TODO: More Directions, also just behind the corner.
        double[][] setups = new double[][] {
                // Slightly off the middle (11, y, 11)
                {11.4, 70.0, 10.4, 10.6, 70.0, 11.4},
                // Going exactly through the middle (11, y, 11)
                {11.4, 70.0, 10.6, 10.6, 70.0, 11.4},
                {11.5, 70.0, 10.5, 10.5, 70.0, 11.5},
        };
        TestRayTracing.runCoordinates(rt, setups, true, false, 3.0, true);
        rt.cleanup();
        bc.cleanup();
    }

    @Test
    public void testWall() {
        FakeBlockCache bc = new FakeBlockCache();
        // Wall using full blocks.
        bc.walls(0, 65, 0, 16, 67, 0, Material.STONE);
        // Ground using full blocks (roughly 16 margin to each side).
        bc.fill(-16, 64, -16, 32, 64, 16, Material.STONE);
        // TODO: Test chest like bounds for target blocks.
        InteractRayTracing rt = new InteractRayTracing(false);
        rt.setBlockCache(bc);
        // TODO: More cases, head inside block itself, angles, ...
        double[][] noCollision = new double[][] {
                {8.5, 66.75, 1.2  ,  8.5, 65.8, 1.0},
                {8.5, 66.75, 1.2  ,  8.5, 69.0, 0.0}, // "Above enough".
        };
        TestRayTracing.runCoordinates(rt, noCollision, false, true, 3.0, true);
        double[][] shouldCollide = new double[][] {
                {8.5, 66.75, 1.2  ,  8.5, 65.8, 0.0},
                {8.5, 66.75, 1.2  ,  8.5, 65.8, -0.2},
        };
        TestRayTracing.runCoordinates(rt, shouldCollide, true, false, 3.0, true);
        rt.cleanup();
        bc.cleanup();
    }

    @Test
    public void testRoom() {
        // TODO: Test for differing middle points (negative to positive range, random, selected rays).
        FakeBlockCache bc = new FakeBlockCache();
        bc.room(-1, 64, -1, 1, 66, 1, Material.STONE);
        // Note that reversed checks are slightly different with the centered version, but start + end blocks are air anyway.
        double[] middle = new double[] {0.5, 65.5, 0.5}; // Free spot.
        // TODO: Must work with strict set to false.
        //CenteredInteractRayTracing rt = new CenteredInteractRayTracing(false, 0, 65, 0);
        CenteredInteractRayTracing rt = new CenteredInteractRayTracing(true, 0, 65, 0);
        rt.setBlockCache(bc);
        double[][] pastFailures = new double[][] {
                {2.1393379885667643, 67.18197661625649, 1.7065201483677281  ,  0.0, 65.0, 0.0},
                {2.7915547712543676, 66.65545738305906, 1.310222428430474  ,  0.0, 65.0, 0.0},
                {0.0, 65.0, 4.5  ,  0.0, 65.0, 1.0}, // strict is false.
                {-3.5, 61.5, -3.5  ,  0.0, 65.0, 0.0} // strict is false.
        };
        TestRayTracing.runCoordinates(rt, pastFailures, true, false, 3, true);
        boolean intense = BuildParameters.testLevel > 1;
        for (double x = -0.5; x < 1.0; x += 0.5) {
            for (double y = -0.5; y < 1.0; y += 0.5) {
                for (double z = -0.5; z < 1.0; z += 0.5) {
                    double add = Math.abs(x) + Math.abs(y) + Math.abs(z);
                    TestRayTracing.runCenterRays(rt, middle[0] + x, middle[1] + y, middle[2] + z, 2.0 + add, intense ? 10000 : 1000, true, false, true);
                }
            }
        }
        rt.cleanup();
        bc.cleanup();
    }

}
