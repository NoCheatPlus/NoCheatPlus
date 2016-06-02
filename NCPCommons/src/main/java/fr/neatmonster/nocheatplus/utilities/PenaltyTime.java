/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
