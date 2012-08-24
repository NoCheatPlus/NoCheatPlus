package fr.neatmonster.nocheatplus.metrics;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.nocheatplus.checks.CheckType;

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
 * This class is used to store the data that will be sent to Metrics later.
 */
public class MetricsData {

    /** Is data collecting enabled? */
    private static boolean                       enabled      = false;

    /** The map containing the number of fails per check. */
    private static final Map<CheckType, Integer> checksFailed = new HashMap<CheckType, Integer>();

    /** The map containing the number of seconds per number of ticks this seconds contain. */
    private static final Map<Integer, Integer>   ticksNumbers = new HashMap<Integer, Integer>();

    /**
     * Adds a failed check to the specified check type.
     * 
     * @param type
     *            the check type
     */
    public static void addFailed(final CheckType type) {
        if (enabled && type.getParent() != null)
            checksFailed.put(type, checksFailed.get(type) + 1);
    }

    /**
     * Adds a seconds to the number of ticks it has contained.
     * 
     * @param ticks
     *            the ticks number
     */
    public static void addTicks(final int ticks) {
        if (enabled)
            ticksNumbers.put(ticks, ticksNumbers.get(ticks) + 1);
    }

    /**
     * Gets the number of failed checks for the specified check type.
     * 
     * @param type
     *            the check type
     * @return the failed
     */
    public static int getFailed(final CheckType type) {
        final int failed = checksFailed.get(type);
        checksFailed.put(type, 0);
        return failed;
    }

    /**
     * Gets the number of seconds which have contained the specified number of ticks.
     * 
     * @param ticks
     *            the ticks number
     * @return the ticks
     */
    public static int getTicks(final int ticks) {
        final int number = ticksNumbers.get(ticks);
        ticksNumbers.put(ticks, 0);
        return number;
    }

    /**
     * Initialize the class.
     */
    public static void initialize() {
        enabled = true;
        for (final CheckType type : CheckType.values())
            if (type.getParent() != null)
                checksFailed.put(type, 0);
        for (int ticks = 0; ticks < 21; ticks++)
            ticksNumbers.put(ticks, 0);
    }
}
