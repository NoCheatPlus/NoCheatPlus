package fr.neatmonster.nocheatplus.compat;

/**
 * Some tri-state with booleans in mind.
 * @author mc_dev
 *
 */
public enum AlmostBoolean{
    YES,
    NO,
    MAYBE;

    /**
     * "Match" a boolean.
     * @param value
     * @return
     */
    public static final AlmostBoolean match(final boolean value) {
        return value ? YES : NO;
    }

    /**
     * Match yes/true/y, no/false/n, maybe/default, otherwise returns null.
     * @param input Can be null.
     * @return
     */
    public static final AlmostBoolean match(String input) {
        if (input == null) {
            return null;
        }
        input = input.trim().toLowerCase();
        if (input.equals("true") || input.equals("yes") || input.equals("y")) {
            return AlmostBoolean.YES;
        }
        else if (input.equals("false") || input.equals("no") || input.equals("n")) {
            return AlmostBoolean.NO;
        }
        else if (input.equals("default") || input.equals("maybe")) {
            return AlmostBoolean.MAYBE;
        } else {
            return null;
        }
    }

    /**
     * Pessimistic interpretation: true iff YES.
     * @return
     */
    public boolean decide(){
        return this == YES;
    }

    /**
     * Optimistic interpretation: true iff not NO.
     * @return
     */
    public boolean decideOptimistically() {
        return this != NO;
    }

}
