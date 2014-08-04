package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.neatmonster.nocheatplus.compat.bukkit.MCAccessBukkit;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.DefaultConfig;
import fr.neatmonster.nocheatplus.config.RawConfigFile;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.RayTracing;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Auxiliary classes packed in here.
 * @author dev1mc
 *
 */
public class BlockTests {
    
    public static class SimpleWorldConfigProvider <C extends RawConfigFile> implements WorldConfigProvider <C>{
        
        private final C config;

        public SimpleWorldConfigProvider(C config) {
            this.config = config;
        }

        @Override
        public C getDefaultConfig() {
            return config;
        }

        @Override
        public C getConfig(String worldName) {
            return config;
        }

        @Override
        public Collection<C> getAllConfigs() {
            final List<C> list = new ArrayList<C>();
            list.add(config);
            return list;
        }
        
    }
    
    public static class DefaultConfigWorldConfigProvider extends SimpleWorldConfigProvider<ConfigFile> {
        public DefaultConfigWorldConfigProvider() {
            super(new DefaultConfig());
        }
    }
    
    /**
     * Initialize BlockProperties with default config and Bukkit-API compliance :p.
     */
    public static void initBlockProperties() {
        BlockProperties.init(new MCAccessBukkit(), new DefaultConfigWorldConfigProvider());
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
                fail("Expect not to collide, "+ tag + ".");
            }
        } else {
            if (expectCollide) {
                fail("Expect to collide, "+ tag + ".");
            }
        }
        if (stepsManhattan > 0.0) {
            final double maxSteps = stepsManhattan * TrigUtil.manhattan(setup[0], setup[1], setup[2], setup[3], setup[4], setup[5]);
            if ((double) rt.getStepsDone() > maxSteps) {
                fail("Expect less than " + maxSteps + " steps for moving straight through a block, "+ tag + ".");
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
