package fr.neatmonster.nocheatplus.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.hooks.APIUtils;

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
     * The class storing the violation level for a check and a player.<br>
     * (Comparable by time.)
     */
    public static class ViolationLevel{
    	/**
    	 * Descending sort.
    	 */
    	public static Comparator<ViolationLevel> VLComparator = new Comparator<ViolationHistory.ViolationLevel>() {
			@Override
			public int compare(final ViolationLevel vl1, final ViolationLevel vl2) {
				if (vl1.time == vl2.time) return 0;
				else if (vl1.time < vl2.time) return 1;
				else return -1;
			}
    	};

        /** The check. */
        public final String check;

        /** The sum of violation levels added. */
        public double       sumVL;
        
        /** Number of violations. */
        public int          nVL;
        
        /** Maximal violation level added. */
        public double       maxVL;  
        

        /** The last VL time. */
        public long        time;

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
            this.sumVL = VL;
            this.nVL   = 1;
            this.maxVL = VL;
            time = System.currentTimeMillis();
        }

        /**
         * Adds a VL to this violation level.
         * 
         * @param VL
         *            the vL
         */
        public void add(final double VL) {
            this.sumVL += VL;
            this.nVL   ++;
            this.maxVL = Math.max(maxVL, VL);
            time = System.currentTimeMillis();
        }

		@Override
		public boolean equals(final Object obj) {
			// Might add String.
			if (obj instanceof ViolationLevel) 
				return this.check.equals(((ViolationLevel) obj).check);
			else return false;
		}

		@Override
		public int hashCode() {
			return check.hashCode();
		}
    }
    
    /** Map the check string names to check types (workaround, keep at default, set by Check)*/
    static Map<String, CheckType> checkTypeMap = new HashMap<String, CheckType>();

    // TODO: Maybe add to metrics: average length of violation histories (does it pay to use SkipListSet or so).
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
        return getHistory(player.getName(), true);
    }
    
    /**
     * Get the history of a player, create if desired and not present.
     * @param player
     * @param create 
     * 			If to create if not present.
     * @return 
     * 			The history, or null if create is set to false and no history is present.
     */
    public static ViolationHistory getHistory(final Player player, final boolean create) {
        return getHistory(player.getName(), create);
    }
    
    /**
     * Gets the history of a player by exact name.
     * @param playerName
     * 			Name of the player in exact writing.
     * @param create 
     * 			If to create the entry, if not present.
     * @return 
     */
    public static ViolationHistory getHistory(final String playerName, final boolean create) {
    	final ViolationHistory hist = violationHistories.get(playerName);
    	if (hist != null) 
    		return hist;
    	else if (create){
    		final ViolationHistory newHist = new ViolationHistory();
    		violationHistories.put(playerName, newHist);
    		return newHist;
    	}
    	else 
    		return null;
    }

    /** The violation levels for every check. */
    private final List<ViolationLevel> violationLevels = new ArrayList<ViolationLevel>();

    /**
     * Gets the violation levels.
     * 
     * @return the violation levels
     */
    public ViolationLevel[] getViolationLevels() {
    	final ViolationLevel[] sortedLevels = new ViolationLevel[violationLevels.size()];
    	violationLevels.toArray(sortedLevels);
    	Arrays.sort(sortedLevels, ViolationLevel.VLComparator); // Descending sort.;
        return sortedLevels;
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

    /**
     * Remove entries for certain check types. Will also remove sub check entries, or all for heckType.ALL
     * @param checkType
     * @return If entries were removed.
     */
	public boolean remove(final CheckType checkType) {
		if (checkType == CheckType.ALL){
			final boolean empty = violationLevels.isEmpty();
			violationLevels.clear();
			return !empty;
		}
		final Iterator<ViolationLevel> it = violationLevels.iterator();
		boolean found = false;
		while (it.hasNext()){
			final ViolationLevel vl = it.next();
			final CheckType refType = checkTypeMap.get(vl.check);
			if (refType == null) continue;
			if (refType == checkType || APIUtils.isParent(checkType, refType)){
				found = true;
				it.remove();
			}
		}
		return found;
	}
}
