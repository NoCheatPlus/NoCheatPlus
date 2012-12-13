package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

public class TestActionFrequency {

	@Test
	public void testSum() {
		ActionFrequency freq = new ActionFrequency(10, 100);
		for (int i = 0; i < 10; i++){
			freq.setBucket(i, 1);
		}
		if (freq.score(1f) != 10f) fail("10x1=10");
		freq.clear(0);
		if (freq.score(1f) != 0f) fail("clear=0");
		
		// TODO: more tests...
	}

}
