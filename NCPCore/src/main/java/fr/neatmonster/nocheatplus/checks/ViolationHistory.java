package fr.neatmonster.nocheatplus.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.hooks.APIUtils;
import fr.neatmonster.nocheatplus.utilities.FCFSComparator;

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
         * Descending sort by time.
         */
        public static Comparator<ViolationLevel> VLComparator = new Comparator<ViolationHistory.ViolationLevel>() {
            @Override
            public int compare(final ViolationLevel vl1, final ViolationLevel vl2) {
                return Long.compare(vl1.time, vl2.time);
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

    public static class VLView {

        public static final Comparator<VLView> CmpName = new Comparator<ViolationHistory.VLView>() {
            @Override
            public int compare(VLView o1, VLView o2) {
                return o1.name.compareToIgnoreCase(o2.name);
            }
        };

        public static final Comparator<VLView> CmpCheck = new Comparator<ViolationHistory.VLView>() {
            @Override
            public int compare(VLView o1, VLView o2) {
                return o1.check.compareToIgnoreCase(o2.check);
            }
        };

        public static final Comparator<VLView> CmpSumVL = new Comparator<ViolationHistory.VLView>() {
            @Override
            public int compare(VLView o1, VLView o2) {
                return Double.compare(o1.sumVL, o2.sumVL);
            }
        };

        public static final Comparator<VLView> CmpnVL = new Comparator<ViolationHistory.VLView>() {
            @Override
            public int compare(VLView o1, VLView o2) {
                return Integer.compare(o1.nVL, o2.nVL);
            }
        };

        public static final Comparator<VLView> CmpAvgVL = new Comparator<ViolationHistory.VLView>() {
            @Override
            public int compare(VLView o1, VLView o2) {
                return Double.compare(o1.sumVL / o1.nVL, o2.sumVL / o2.nVL);
            }
        };

        public static final Comparator<VLView> CmpMaxVL = new Comparator<ViolationHistory.VLView>() {
            @Override
            public int compare(VLView o1, VLView o2) {
                return Double.compare(o1.maxVL, o2.maxVL);
            }
        };

        public static final Comparator<VLView> CmpTime = new Comparator<ViolationHistory.VLView>() {
            @Override
            public int compare(VLView o1, VLView o2) {
                return Long.compare(o1.time, o2.time);
            }
        };

        /**
         * Get a mixed/fcfs comparator from parsing given args. Accepted are 
         * @param args
         * @param startIndex
         * @return If none are found, null is returned, no errors will be thrown, duplicates are removed. 
         */
        public static Comparator<VLView> parseMixedComparator(String[] args, int startIndex) {
            final Set<Comparator<VLView>> comparators = new LinkedHashSet<Comparator<VLView>>();
            for (int i = startIndex; i < args.length; i ++) {
                String arg = args[i].toLowerCase();
                while (arg.startsWith("-")) {
                    arg = arg.substring(1);
                }
                if (arg.matches("(name|player|playername)")) {
                    comparators.add(CmpName);
                } else if (arg.matches("(check|type|checktype)")) {
                    comparators.add(CmpCheck);
                } else if (arg.matches("(sum|sumvl|vl)")) {
                    comparators.add(CmpSumVL);
                } else if (arg.matches("(n|num|number|nvl)")) {
                    comparators.add(CmpnVL);
                } else if (arg.matches("(avg|av|average|averagevl|avgvl|avvl|avl)")) {
                    comparators.add(CmpAvgVL);
                } else if (arg.matches("(max|maxvl|maximum|maximumvl)")) {
                    comparators.add(CmpMaxVL);
                } else if (arg.matches("(time|t)")) {
                    comparators.add(CmpTime);
                }
            }
            if (comparators.isEmpty()) {
                return null;
            }
            return new FCFSComparator<ViolationHistory.VLView>(comparators, true);
        }

        public final String name;
        public final String check;
        public final double sumVL;
        public final int nVL;
        public final double maxVL;
        public final long time;

        public VLView(String name, ViolationLevel vl) {
            this(name, vl.check, vl.sumVL, vl.nVL, vl.maxVL, vl.time);
        }

        public VLView(String name, String check, double sumVL, int nVL, double maxVL, long time) {
            this.name = name;
            this.check = check;
            this.sumVL = sumVL;
            this.nVL = nVL;
            this.maxVL = maxVL;
            this.time = time;
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

    /**
     * Get a list of VLView instances for direct check type matches (no inheritance checks).
     * @param checkType
     * @return Always returns a list.
     */
    public static List<VLView> getView(final CheckType checkType) {
        final List<VLView> view = new LinkedList<VLView>();
        for (final Entry<String, ViolationHistory> entry: violationHistories.entrySet()) {
            final ViolationHistory hist = entry.getValue();
            final ViolationLevel vl = hist.getViolationLevel(checkType);
            if (vl != null) {
                view.add(new VLView(entry.getKey(), vl));
            }
        }
        return view;
    }

    public static ViolationHistory removeHistory(final String playerName){
        return violationHistories.remove(playerName);
    }

    public static void clear(final CheckType checkType){
        for (ViolationHistory hist : violationHistories.values()){
            hist.remove(checkType);
        }
    }

    /** The violation levels for every check. */
    private final List<ViolationLevel> violationLevels = new ArrayList<ViolationLevel>();

    /**
     * Gets the violation levels. Sorted by time, descending.
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
     * Return only direct matches, no inheritance checking.
     * @param type
     * @return ViolationLevel instance, if present. Otherwise null.
     */
    public ViolationLevel getViolationLevel(final CheckType type) {
        for (int i = 0; i < violationLevels.size(); i++) {
            final ViolationLevel vl = violationLevels.get(i);
            if (checkTypeMap.get(vl.check) == type) {
                return vl;
            }
        }
        return null;
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
