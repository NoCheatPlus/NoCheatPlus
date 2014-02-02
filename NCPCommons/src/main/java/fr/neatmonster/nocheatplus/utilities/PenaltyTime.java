package fr.neatmonster.nocheatplus.utilities;

/**
 * Simple penalty duration checker for "current ms", taking into account clocks running backwards.
 * @author mc_dev
 *
 */
public class PenaltyTime {
	
    /** Last time a penalty was dealt (for consistency). */
    private long   penaltyLast = 0;
    
    /** Time when the penalty ends. Penalty ends with hitting the penaltyEnd time (equality).*/
    private long   penaltyEnd  = 0;
    
	/**
	 * Merges new penalty time.
	 * @param now Current ms time.
	 */
	public void applyPenalty(long duration) {
		applyPenalty(System.currentTimeMillis(), duration);
	}
	
	/**
	 * Merges new penalty time.
	 * @param now Current ms time.
	 * @param duration Penalty duration in ms.
	 */
	public void applyPenalty(long now, long duration) {
		penaltyLast = now;
		if (now < penaltyLast) {
			penaltyEnd = now + duration;
		} else {
			penaltyEnd = Math.max(now + duration, penaltyEnd);
		}
	}
	
	/**
	 * Test if a penalty applies right now.
	 * @return
	 */
	public boolean isPenalty() {
		return isPenalty(System.currentTimeMillis());
	}
	
	/**
	 * Test if a penalty applies at the given time. Penalty ends with hitting the penaltyEnd time (equality).
	 * @param now Current time in ms.
	 * @return
	 */
	public boolean isPenalty(long now) {
		if (now < penaltyLast) {
			resetPenalty();
			return false;
		} else {
			return now < penaltyEnd;
		}
	}
	
	/**
	 * Reset the penalty.
	 */
	public void resetPenalty() {
		penaltyLast = 0;
		penaltyEnd = 0;
	}
}
