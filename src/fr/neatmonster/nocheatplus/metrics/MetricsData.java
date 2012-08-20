package fr.neatmonster.nocheatplus.metrics;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.metrics.Metrics.Plotter;

/*
 * M"""""`'"""`YM            dP            oo                   M""""""'YMM            dP            
 * M  mm.  mm.  M            88                                 M  mmmm. `M            88            
 * M  MMM  MMM  M .d8888b. d8888P 88d888b. dP .d8888b. .d8888b. M  MMMMM  M .d8888b. d8888P .d8888b. 
 * M  MMM  MMM  M 88ooood8   88   88'  `88 88 88'  `"" Y8ooooo. M  MMMMM  M 88'  `88   88   88'  `88 
 * M  MMM  MMM  M 88.  ...   88   88       88 88.  ...       88 M  MMMM' .M 88.  .88   88   88.  .88 
 * M  MMM  MMM  M `88888P'   dP   dP       dP `88888P' `88888P' M       .MM `88888P8   dP   `88888P8 
 * MMMMMMMMMMMMMM                                               MMMMMMMMMMM                          
 */
/**
 * The Metrics data.
 */
public class MetricsData {

    /**
     * The ticks plotter.
     */
    public static class TicksPlotter extends Plotter {

        /** The ticks. */
        private final int ticks;

        /**
         * Instantiates a new ticks plotter.
         * 
         * @param ticks
         *            the ticks
         */
        public TicksPlotter(final int ticks) {
            super(ticks + " tick(s)");
            this.ticks = ticks;
        }

        /* (non-Javadoc)
         * @see fr.neatmonster.nocheatplus.metrics.Metrics.Plotter#getValue()
         */
        @Override
        public int getValue() {
            final int ticks = MetricsData.getServerTicks(this.ticks);
            MetricsData.resetServerTicks(this.ticks);
            return ticks;
        }
    }

    /** The violation levels. */
    private static final Map<CheckType, Double>  violationLevels = new HashMap<CheckType, Double>();

    /** The checks failed. */
    private static final Map<CheckType, Integer> checksFailed    = new HashMap<CheckType, Integer>();

    /** The events checked. */
    private static final Map<CheckType, Integer> eventsChecked   = new HashMap<CheckType, Integer>();

    /** The server ticks. */
    private static final Map<Integer, Integer>   serverTicks     = new HashMap<Integer, Integer>();

    /**
     * Adds the checked.
     * 
     * @param type
     *            the type
     */
    public static void addChecked(final CheckType type) {
        if (type.getParent() != null)
            eventsChecked.put(type, getChecked(type) + 1);
    }

    /**
     * Adds the ticks.
     * 
     * @param ticks
     *            the ticks
     */
    public static void addTicks(final int ticks) {
        serverTicks.put(ticks, serverTicks.get(ticks) + 1);
    }

    /**
     * Adds the violation.
     * 
     * @param violationData
     *            the violation data
     */
    public static void addViolation(final ViolationData violationData) {
        final CheckType type = violationData.check.getType();
        if (type.getParent() != null) {
            checksFailed.put(type, getFailed(type) + 1);
            violationLevels.put(type, getViolationLevel(type) + violationData.addedVL);
        }
    }

    /**
     * Gets the checked.
     * 
     * @param type
     *            the type
     * @return the checked
     */
    public static int getChecked(final CheckType type) {
        if (type == CheckType.ALL) {
            int eventsChecked = 0;
            for (final double value : MetricsData.eventsChecked.values())
                eventsChecked += value;
            return eventsChecked;
        }
        if (!eventsChecked.containsKey(type))
            resetChecked(type);
        return eventsChecked.get(type);
    }

    /**
     * Gets the failed.
     * 
     * @param type
     *            the type
     * @return the failed
     */
    public static int getFailed(final CheckType type) {
        if (type == CheckType.ALL) {
            int checkFails = 0;
            for (final double value : checksFailed.values())
                checkFails += value;
            return checkFails;
        }
        if (!checksFailed.containsKey(type))
            resetFailed(type);
        return checksFailed.get(type);
    }

    /**
     * Gets the server ticks.
     * 
     * @param ticks
     *            the ticks
     * @return the server ticks
     */
    public static int getServerTicks(final int ticks) {
        if (!serverTicks.containsKey(ticks))
            resetServerTicks(ticks);
        return serverTicks.get(ticks);
    }

    /**
     * Gets the violation level.
     * 
     * @param type
     *            the type
     * @return the violation level
     */
    public static double getViolationLevel(final CheckType type) {
        if (type == CheckType.ALL) {
            double violationLevel = 0D;
            for (final double value : violationLevels.values())
                violationLevel += value;
            return violationLevel;
        }
        if (!violationLevels.containsKey(type))
            resetViolationLevel(type);
        return violationLevels.get(type);
    }

    /**
     * Reset checked.
     * 
     * @param type
     *            the type
     */
    public static void resetChecked(final CheckType type) {
        eventsChecked.put(type, 0);
    }

    /**
     * Reset failed.
     * 
     * @param type
     *            the type
     */
    public static void resetFailed(final CheckType type) {
        checksFailed.put(type, 0);
    }

    /**
     * Reset server ticks.
     * 
     * @param ticks
     *            the ticks
     */
    public static void resetServerTicks(final int ticks) {
        serverTicks.put(ticks, 0);
    }

    /**
     * Reset violation level.
     * 
     * @param type
     *            the type
     */
    public static void resetViolationLevel(final CheckType type) {
        violationLevels.put(type, 0D);
    }
}
