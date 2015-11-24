package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;

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
	
	@Test
	public void testAddFromZeroTime(){
		addFromTime(0);
	}
	
	@Test
	public void testAddFromCurrentTime(){
		addFromTime(System.currentTimeMillis());
	}
	
	/**
	 * Test adding 1 such that each bucket gets filled with an equal total amount.
	 * @param time Point of time from which to start.
	 */
	public void addFromTime(long time){
		ActionFrequency freq = new ActionFrequency(3, 333);
		freq.update(time);
		for (int i = 0; i < 999; i++){
			freq.add(time + i, 1f);
			// TODO: maybe test sums here already.
		}
		if (freq.score(1f) != 999) fail("Sum should be 999, got instead: " + freq.score(1f));
		freq.update(time + 999);
		if (freq.score(1f) != 666f) fail("Sum should be 666, got instead: " + freq.score(1f));
		freq.update(time + 1332);
		if (freq.score(1f) != 333f) fail("Sum should be 333, got instead: " + freq.score(1f));
		freq.update(time + 1665);
		if (freq.score(1f) != 0f) fail("Sum should be 0, got instead: " + freq.score(1f));
	}
	
	@Test
	public void testUpdateAlternatingSignumTimes(){
		// Basically fails if this generates an exception.
		int sig = 1;
		ActionFrequency freq = new ActionFrequency(10, 100);
		for (int i = 0; i < 1000; i++){
			freq.update(i * sig);
			sig = sig * -1;
		}
	}

}
