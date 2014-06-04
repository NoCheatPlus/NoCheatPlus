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
