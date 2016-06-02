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
package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.PenaltyTime;


public class TestPenaltyTime {
	
	@Test
	public void testZeroSequence() {
		long now = System.currentTimeMillis();
		PenaltyTime pt = new PenaltyTime();
		pt.applyPenalty(now, 0);
		if (pt.isPenalty(now )) {
			fail("Expect no penalty with duration 0.");
		}
	}
	
	@Test
	public void testSequence() {
		long now = System.currentTimeMillis();
		PenaltyTime pt = new PenaltyTime();
		for (long i = 0; i < 10000; i ++) {
			long j = i % 100;
			if (j == 0) {
				if (pt.isPenalty(now + i)) {
					fail("Expect no penalty at i=" + i);
				}
				pt.applyPenalty(now + i, 50);
			} else if (j < 50) {
				if (!pt.isPenalty(now + i)) {
					fail("Expect penalty at i=" + i);
				}
			} else {
				if (pt.isPenalty(now + i)) {
					fail("Expect no penalty at i=" + i);
				}
			}
			
			
		}
	}
	
	@Test
	public void testReset() {
		long now = System.currentTimeMillis();
		PenaltyTime pt = new PenaltyTime();
		pt.applyPenalty(now, 73);
		if (pt.isPenalty(now - 1)) {
			fail("isPenalty should return false on past time.");
		}
		pt.applyPenalty(now - 1, 73);
		if (pt.isPenalty(now + 72)) {
			fail("isPenalty should not return edge time after reset.");
		}
	}
}
