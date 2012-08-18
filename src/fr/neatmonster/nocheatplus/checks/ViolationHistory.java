package fr.neatmonster.nocheatplus.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.entity.Player;

/*
 * M""MMMMM""M oo          dP            dP   oo                   
 * M  MMMMM  M             88            88                        
 * M  MMMMP  M dP .d8888b. 88 .d8888b. d8888P dP .d8888b. 88d888b. 
 * M  MMMM' .M 88 88'  `88 88 88'  `88   88   88 88'  `88 88'  `88 
 * M  MMP' .MM 88 88.  .88 88 88.  .88   88   88 88.  .88 88    88 
 * M     .dMMM dP `88888P' dP `88888P8   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMM                                                     
 * 
 * M""MMMMM""MM oo            dP                              
 * M  MMMMM  MM               88                              
 * M         `M dP .d8888b. d8888P .d8888b. 88d888b. dP    dP 
 * M  MMMMM  MM 88 Y8ooooo.   88   88'  `88 88'  `88 88    88 
 * M  MMMMM  MM 88       88   88   88.  .88 88       88.  .88 
 * M  MMMMM  MM dP `88888P'   dP   `88888P' dP       `8888P88 
 * MMMMMMMMMMMM                                           .88 
 *                                                    d8888P  
 */
/**
 * The class containg the violation history of a player.
 */
public class ViolationHistory {

    /**
     * The class storing the violation level for a check and a player.
     */
    public class ViolationLevel {

        /** The check. */
        public final String check;

        /** The VL. */
        public double       VL;

        /** The last VL time. */
        private long        time;

        /**
         * Instantiates a new violation level.
         * 
         * @param check
         *            the check
         * @param VL
         *            the vL
         */
        public ViolationLevel(final String check, final double VL) {
            this.check = check;
            this.VL = VL;
            time = System.currentTimeMillis();
        }

        /**
         * Adds a VL to this violation level.
         * 
         * @param VL
         *            the vL
         */
        public void add(final double VL) {
            this.VL += VL;
            time = System.currentTimeMillis();
        }
    }

    /** The histories of all the players. */
    private static Map<String, ViolationHistory> violationHistories = new HashMap<String, ViolationHistory>();

    /**
     * Gets the history of a player.
     * 
     * @param player
     *            the player
     * @return the history
     */
    public static ViolationHistory getHistory(final Player player) {
        if (!violationHistories.containsKey(player.getName()))
            violationHistories.put(player.getName(), new ViolationHistory());
        return violationHistories.get(player.getName());
    }

    /** The violation levels for every check. */
    private final List<ViolationLevel> violationLevels = new ArrayList<ViolationLevel>();

    /**
     * Gets the violation levels.
     * 
     * @return the violation levels
     */
    public TreeMap<Long, ViolationLevel> getViolationLevels() {
        final TreeMap<Long, ViolationLevel> violationLevels = new TreeMap<Long, ViolationLevel>();
        for (final ViolationLevel violationLevel : this.violationLevels)
            violationLevels.put(violationLevel.time, violationLevel);
        return violationLevels;
    }

    /**
     * Log a VL.
     * 
     * @param check
     *            the check
     * @param VL
     *            the vL
     */
    public void log(final String check, final double VL) {
        for (final ViolationLevel violationLevel : violationLevels)
            if (check.equals(violationLevel.check)) {
                violationLevel.add(VL);
                return;
            }
        violationLevels.add(new ViolationLevel(check, VL));
    }
}
