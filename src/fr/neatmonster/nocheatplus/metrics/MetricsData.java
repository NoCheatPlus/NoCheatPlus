package fr.neatmonster.nocheatplus.metrics;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;

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

    /** The violation levels. */
    private static final Map<CheckType, Double>  violationLevels = new HashMap<CheckType, Double>();

    /** The checks fails. */
    private static final Map<CheckType, Integer> checksFails     = new HashMap<CheckType, Integer>();

    /** The events checked. */
    private static final Map<CheckType, Integer> eventsChecked   = new HashMap<CheckType, Integer>();

    /**
     * Called when an event is checked.
     * 
     * @param type
     *            the type
     */
    public static void addChecked(final CheckType type) {
        if (type.getParent() != null)
            eventsChecked.put(type, getChecked(type));
    }

    /**
     * Called when a player fails a check.
     * 
     * @param violationData
     *            the violation data
     */
    public static void addViolation(final ViolationData violationData) {
        final CheckType type = violationData.check.getType();
        if (type.getParent() != null) {
            violationLevels.put(type, getViolationLevel(type) + violationData.addedVL);
            checksFails.put(type, getFailed(type) + 1);
        }
    }

    /**
     * Gets the number of event checked.
     * 
     * @param type
     *            the type
     * @return the number of event checked
     */
    public static int getChecked(final CheckType type) {
        if (type == CheckType.ALL) {
            int eventsChecked = 0;
            for (final double value : MetricsData.eventsChecked.values())
                eventsChecked += value;
            return eventsChecked;
        }
        if (!eventsChecked.containsKey(type))
            eventsChecked.put(type, 0);
        return eventsChecked.get(type);
    }

    /**
     * Gets the number of failed checks.
     * 
     * @param type
     *            the type
     * @return the number of failed checks
     */
    public static int getFailed(final CheckType type) {
        if (type == CheckType.ALL) {
            int checkFails = 0;
            for (final double value : checksFails.values())
                checkFails += value;
            return checkFails;
        }
        if (!checksFails.containsKey(type))
            checksFails.put(type, 0);
        return checksFails.get(type);
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
            violationLevels.put(type, 0D);
        return violationLevels.get(type);
    }
}
