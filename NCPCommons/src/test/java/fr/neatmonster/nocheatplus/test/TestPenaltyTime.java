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
