package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import java.util.Random;

import org.bukkit.Location;
import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.RayTracing;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;

public class TestRayTracing {

    protected static final Random random = new Random(System.nanoTime() + 13391);

    protected static double maxFactor = 9.0;

    protected static int maxSteps(double dX, double dY, double dZ){
        return (int) (maxFactor * (1 + Math.abs(dX) + Math.abs(dY) + Math.abs(dZ)));
    }

    public static class CountRayTracing extends RayTracing{
        public CountRayTracing(double x0, double y0, double z0, double x1, double y1, double z1) {
            super(x0, y0, z0, x1, y1, z1);
        }
        @Override
        protected boolean step(int blockX, int blockY, int blockZ, double oX,
                double oY, double oZ, double dT) {
            if (step > maxSteps(dX, dY, dZ)) {
                System.out.println("[WARNING] Max steps exceeded: " + maxSteps(dX, dY, dZ));
                return false; 
            }
            return true;
        }
    }

    public static double[] randomCoords(double max){
        double[] res = new double[6];
        for (int i = 0; i < 6 ; i++){
            res[i] = (random.nextDouble() * 2.0 - 1.0 ) * max;
        }
        return res;
    }

    public static double[] randomBlockCoords(int max){
        double[] res = new double[6];
        for (int i = 0; i < 6 ; i++){
            res[i] = random.nextInt(max * 2 + 1) -  max;
        }
        return res;
    }

    public static void doFail(String message, double[] coords) {
        System.out.println("---- Failure trace ----");
        System.out.println(message);
        if (coords != null){
            System.out.println("{" + coords[0] + ", " + coords[1]+ ", " + coords[2] + "  ,  " + coords[3] + ", " + coords[4]+ ", " + coords[5] + "}");
            dumpRawRayTracing(coords);
        }
        fail(message);
    }

    /**
     * Mostly block-coordinate consistency checking.
     * @param coords
     * @return
     */
    public static RayTracing checkConsistency(final double[] coords){
        RayTracing rt = new RayTracing(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]) {

            protected int lbx, lby, lbz;

            protected double ldt = 0;

            protected int step = 0;

            /* (non-Javadoc)
             * @see fr.neatmonster.nocheatplus.utilities.RayTracing#set(double, double, double, double, double, double)
             */
            @Override
            public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
                super.set(x0, y0, z0, x1, y1, z1);
                lbx = blockX - 1;
                lby = blockY - 1;
                lbz = blockZ - 1;
                ldt = 0;
                step = 0;
            }

//            private boolean ignEdge(double offset, double dTotal){
//                return offset == 1 && dTotal > 0 || offset == 0 && dTotal < 0;
//            }

            @Override
            protected boolean step(int blockX, int blockY, int blockZ, double oX, double oY, double oZ, double dT) {
                // TODO: This does not check last step for some occasions where it should.
                step ++;

                if (dT < 0.0){
                    doFail("dT < 0 at t = " + StringUtil.fdec3.format(t), coords);
                }
                
                // TODO: Check if this check makes sense at all (dT=0 happens during multi-transitions.)l.
//                if (dT == 0.0 && 1.0 - (t + dT) > tol){
//                    if (!ignEdge(oX, dX) && !ignEdge(oY, dY) && !ignEdge(oZ, dZ)){
//                        doFail("Premature dT = 0 at t = " + StringUtil.fdec3.format(t), coords);
//                    }
//                }

                checkOffset(oX, "x");
                checkOffset(oY, "y");
                checkOffset(oZ, "z");

                // TODO: check with last block coordinates
                if (lbx == blockX && lby == blockY && lbz == blockZ){
                    if (1.0 - (t + dT) > tol){
                        doFail("Expect block coordinates to change with each step (step=" + step + ", t=" + StringUtil.fdec3.format(t) +").", coords);
                    }
                }
                // TODO: check offsets
                // Set to current.
                lbx = blockX;
                lby = blockY;
                lbz = blockZ;
                ldt = dT;
                if (step > maxSteps(dX, dY, dZ)) doFail("max steps exceeded: " + maxSteps(dX, dY, dZ), coords); 
                return true;
            }

            private void checkOffset(double offset, String name) {
                if (offset < 0.0 || offset > 1.0) doFail("Bad " + name + "-offset: " + offset, coords);
            }

            @Override
            public void loop() {
                super.loop();
                checkBlockTarget(coords[3], blockX, oX, dX, ldt, "x");
                checkBlockTarget(coords[4], blockY, oY, dY, ldt, "y");
                checkBlockTarget(coords[5], blockZ, oZ, dZ, ldt, "z");
            }

            private void checkBlockTarget(double target, int current, double offset, double dTotal, double dT, String name){
                int b = Location.locToBlock(target);
                if (current != b){
                    // TODO: Might do with or without these ?
                    //					if (current == b + 1 && dTotal > 0 && offset == 0) return;
                    //					if (current == b - 1 && dTotal < 0 && offset == 1) return;
                    if (Math.abs(dT * dTotal + offset + (double) current - target) <= 0.001){
                        // TODO: Narrow down by edge coordinates or so.
                        return;
                    }
                    System.out.println(target + "|" +  current + "|" + offset + "|" + dT * dTotal);
                    // Failure.
                    doFail("Bad target " + name + "-coordinate: " + current + " instead of " + b, coords);
                }
            }		
        };
        rt.loop();
        return rt;
    }

    public static RayTracing checkNumberOfSteps(double[] coords, int steps) {
        CountRayTracing crt = new CountRayTracing(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
        crt.loop();
        int done = crt.getStepsDone();
        if (done != steps) doFail("Wrong number of steps: " + done + " instead of " + steps, coords);
        return crt;
    }

    public static void dump(int blockX, int blockY, int blockZ, double oX, double oY, double oZ, double t, double dT) {
        String sdt = StringUtil.fdec3.format(dT);
        if ("0".equals(sdt) && dT > 0) sdt = "0.X";
        System.out.println(StringUtil.fdec3.format(t) + " (+" + sdt + "): " + blockX + ", "+blockY + ", " + blockZ + " / " + StringUtil.fdec3.format(oX) + ", " + StringUtil.fdec3.format(oY)+ ", " + StringUtil.fdec3.format(oZ));
    }

    public static RayTracing dumpRawRayTracing(final double[] coords) {
        RayTracing rt = new RayTracing(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]) {
            @Override
            protected boolean step(int blockX, int blockY, int blockZ, double oX, double oY, double oZ, double dT) {
                dump(blockX, blockY, blockZ, oX, oY, oZ, t, dT);
                if (step > maxSteps(dX, dY, dZ)){
                    System.out.println("[WARNING] Max steps exceeded: " + maxSteps(dX, dY, dZ));
                    return false;
                }
                return true;
            }
        };
        rt.loop();
        return rt;
    }

    @Test
    public void testNumberOfSteps(){
        // Hand picked stuff.
        checkNumberOfSteps(new double[]{0.5, 0.5, 0.5, 1.5, -0.5, 1.5}, 4);
    }

    @Test
    public void testConsistency(){
        // Past failures / making a difference.		
        for (double[] coords : new double[][]{
                // Sort by x0.
                new double[]{-9.873, -4.773, -3.387, -0.161, -1.879, -7.079},
                new double[]{-3.0066423238842366, 0.8056808285866079, 5.359238045631369  ,  2.0000000356757375, -2.3002237817433757, -5.889349195033338},
                new double[]{2.5619753859456917, -5.010424935746547, -7.39326637860553  ,  -4.678643570182639, -2.0000000105642313, -4.634727842675916},
                new double[]{7.388348424961977, -8.000000029346532, -2.5365675909347507  ,  2.17126848312847, 3.236994108042559, -8.423292642985071},
                new double[]{7.525633617461991, 2.654408573114717, 3.5119744782127893  ,  9.99999995904821, 9.599753890871172, 6.721727939686946},
                new double[] {-6.0, -4.0, -3.0  ,  -4.0, -3.0, -2.0},
        }){
            checkConsistency(coords);
        }

        final boolean e = BuildParameters.testLevel > 0;

        // Random tests.
        for (int i = 0; i < (e ? 50000000 : 100000); i++){
            checkConsistency(randomCoords(10.0));
        }

        // TODO: make these work.
        for (int i = 0; i < (e ? 10000000 : 1000); i++){
            checkConsistency(randomBlockCoords(6));
        }

        // TODO: Add tests for typical coordinates a with interact, passable.
    }

    public static void runCoordinates(RayTracing rt, double[] setup, boolean expectCollide, boolean expectNotCollide, double stepsManhattan, boolean reverse, String tag) {
        if (reverse) {
            rt.set(setup[3], setup [4], setup[5], setup[0], setup[1], setup[2]);
            tag += "/reversed";
        } else {
            rt.set(setup[0], setup[1], setup[2], setup[3], setup [4], setup[5]);
        }
        rt.loop();
        if (rt.collides()) {
            if (expectNotCollide) {
                doFail("Expect not to collide, "+ tag + ".", setup);
            }
        } else {
            if (expectCollide) {
                doFail("Expect to collide, "+ tag + ".", setup);
            }
        }
        if (stepsManhattan > 0.0) {
            final double maxSteps = stepsManhattan * Math.max(1.0, TrigUtil.manhattan(setup[0], setup[1], setup[2], setup[3], setup[4], setup[5]));
            if ((double) rt.getStepsDone() > maxSteps) {
                doFail("Expect less than " + maxSteps + " steps, "+ tag + ".", setup);
            }
        }
    }

    /**
     * 
     * @param rt
     * @param setups Array of Arrays of 6 doubles as argument for RayTracing.set(...).
     * @param stepsManhattan
     * @return Int array of size 2: {not colliding, colliding}
     */
    public static void runCoordinates(RayTracing rt, double[][] setups, boolean expectCollide, boolean expectNotCollide, double stepsManhattan, boolean testReversed) {
        for (int i = 0; i < setups.length; i++) {
            double[] setup = setups[i];
            runCoordinates(rt, setup, expectCollide, expectNotCollide, stepsManhattan, false, "index=" + i);
            if (testReversed) {
                // Reverse.
                runCoordinates(rt, setup, expectCollide, expectNotCollide, stepsManhattan, true, "index=" + i);
            }
        }
    }

}
