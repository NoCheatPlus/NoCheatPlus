package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import fr.neatmonster.nocheatplus.checks.moving.LocationTrace;
import fr.neatmonster.nocheatplus.checks.moving.LocationTrace.TraceEntry;
import fr.neatmonster.nocheatplus.checks.moving.LocationTrace.TraceIterator;
import fr.neatmonster.nocheatplus.utilities.StringUtil;


public class TestLocationTrace {
	
	protected static final Random random = new Random(System.nanoTime() + 133345691);
	
	/**
	 * +- radius around 0.0.
	 * @param radius
	 * @return
	 */
	public static double rand(double radius) {
		return rand(0.0, radius);
	}
	
	/**
	 * +- radius around center.
	 * @param center
	 * @param radius
	 * @return
	 */
	public static double rand(double center, double radius) {
		return center + 2.0 * radius * (random.nextDouble() - 0.5);
	}
	
	/**
	 * center +- step
	 * @param center
	 * @param step
	 * @return
	 */
	public static double randStep(double center, double step) {
		return center + (random.nextBoolean() ? step : -step);
	}
	
	@Test
	public void testSize() {
		int size = 80;
		double mergeDist = -0.1;
		LocationTrace trace = new LocationTrace(size, mergeDist);
		// Empty
		if (!trace.isEmpty()) {
			fail("Trace must be empty on start.");
		}
		if (trace.size() != 0) {
			fail("Size must be 0 at start.");
		}
		// Adding up to size elements.
		for (int i = 0; i < size ; i++) {
			trace.addEntry(i, i, i, i);
			if (trace.size() != i + 1) {
				fail("Wrong size, expect " + (i + 1) + ", got instead: " + trace.size());
			}
		}
		// Adding a lot of elements.
		for (int i = 0; i < 1000; i ++) {
			trace.addEntry(i + size, i, i, i);
			if (trace.size() != size) {
				fail("Wrong size, expect " + size + ", got instead: " + trace.size());
			}
		}
	}
	
	@Test
	public void testMergeZeroDist() {
		int size = 80;
		double mergeDist = 0.0;
		LocationTrace trace = new LocationTrace(size, mergeDist);
		for (int i = 0; i < 1000; i ++) {
			trace.addEntry(i + size, 0 , 0, 0);
			if (trace.size() != 1) {
				fail("Wrong size, expect 1, got instead: " + trace.size());
			}
		}
	}
	
	@Test
	public void testMergeUpdateAlwaysDist() {
		// Extreme merge dist.
		int size = 80;
		double mergeDist = 1000.0;
		LocationTrace trace = new LocationTrace(size, mergeDist);
		double x = 0;
		double y = 0;
		double z = 0;
		trace.addEntry(0 , x, y, z);
		// Note that entries might get split, if the distance to the second last gets too big, so the maximum number of steps must be limited.
		for (int i = 0; i < 1000; i ++) {
			x = randStep(x, 0.5);
			y = randStep(y, 0.5);
			z = randStep(z, 0.5);
			trace.addEntry(i + 1, x, y, z);
			if (trace.size() != 2) {
				fail("Wrong size, expect 2, got instead: " + trace.size());
			}
		}
	}
	
	@Test
	public void testMergeDist() {
		// Deterministic steps => calculatable size.
		int size = 80;
		double mergeDist = 0.5;
		LocationTrace trace = new LocationTrace(size, mergeDist);
		double x = 0;
		double y = 0;
		double z = 0;
		trace.addEntry(0 , x, y, z);
		for (int i = 0; i < size * 2; i ++) {
			x += 0.5;
			trace.addEntry(i + 1, x, y, z);
			if (Math.abs(trace.size() - (1 + i / 2)) > 1 ) {
				fail("Wrong size, expect roughly half of " + (i + 1) + ", got instead: " + trace.size());
			}
		}
		Iterator<TraceEntry> it = trace.oldestIterator();
		while (it.hasNext()) {
			if (it.next().lastDistSq > 1.0) {
				fail("Spacing should be smaller than 1.0 (sq / actual).");
			}
		}
		
	}
	
	@Test
	public void testEmptyIterator() {
		// Expected to fail.
		int size = 80;
		double mergeDist = -0.1;
		LocationTrace trace = new LocationTrace(size, mergeDist);
		try {
			trace.oldestIterator();
			fail("Expect an exception on trying to get an empty iterator (oldest).");
		} catch (IllegalArgumentException ex) {
			
		}
		try {
			trace.latestIterator();
			fail("Expect an exception on trying to get an empty iterator (latest).");
		} catch (IllegalArgumentException ex) {
			
		}
		try {
			trace.maxAgeIterator(0);
			fail("Expect an exception on trying to get an empty iterator (maxAge).");
		} catch (IllegalArgumentException ex) {
			
		}
	}
	
	@Test
	public void testIteratorSizeAndOrder() {
		int size = 80;
		double mergeDist = -0.1; // Never merge.
		LocationTrace trace = new LocationTrace(size, mergeDist);
		// Adding up to size elements.
		for (int i = 0; i < size; i++) {
			trace.addEntry(i, i, i, i);
		}
		// Test size with one time filled up.
		testIteratorSizeAndOrder(trace);
		// Add size / 2 elements, to test cross-boundary iteration.
		for (int i = 0; i < size / 2; i++) {
			trace.addEntry(i + size, i, i, i);
		}
		// Test size again.
		testIteratorSizeAndOrder(trace);
	}

	private void testIteratorSizeAndOrder(LocationTrace trace) {
		int size = trace.size();
		TraceIterator[] iterators = new TraceIterator[] {
			trace.oldestIterator(),
			trace.latestIterator(),
			trace.maxAgeIterator(0) // Asserts entries to start at time >= 0.
		};
		String[] iteratorNames = new String[]{
			"oldest",
			"latest",
			"maxAge"
		};
		int[] increments = new int[] {
			1,
			-1,
			1
		};
		for (int i = 0; i < iterators.length; i++) {
			int n = 0;
			TraceIterator it = iterators[i];
			TraceEntry last = null;
			TraceEntry current = null;
			while (it.hasNext()) {
				current = it.next();
				n ++;
				if (n > size) {
					fail("Iterator (" + iteratorNames[i] + ") iterates too long.");
				}
				if (last != null) {
					if (current.time - last.time != increments[i]) {
						fail("Bad time increment (" + iteratorNames[i] + "). Expected " + increments[i] + ", got instead: " + (current.time - last.time));
					}
				}
				last = current;
			}
			if (n != size) {
				fail("Iterator (" + iteratorNames[i] + ") should have " + size + " elements, found instead: " + n);
			}
		}
	}
	
	@Test
	public void testMaxAgeIterator() {
		int size = 80;
		double mergeDist = -0.1; // Never merge.
		LocationTrace trace = new LocationTrace(size, mergeDist);
		// Adding up to size elements.
		for (int i = 0; i < size; i++) {
			trace.addEntry(i, i, i, i);
		}
		for (int i = 0; i < size; i++) {
			Iterator<TraceEntry> it = trace.maxAgeIterator(i);
			long got = it.next().time;
			if (got != i) {
				fail("Bad entry point for iterator (maxAge), expected " + i + ", got instead: " + got);
			}
			int n = 1;
			while (it.hasNext()) {
				it.next();
				n ++;
			}
			if (n != size - i) {
				fail("Bad number of elements for iterator (maxAge), expected " + (size - i) + ", got instead: " + n);
			}
		}
	}
	
	@Test
	public void testMaxAgeFirstElementAnyway() {
		int size = 80;
		double mergeDist = -0.1; // Never merge.
		LocationTrace trace = new LocationTrace(size, mergeDist);
		trace.addEntry(0, 0, 0, 0);
		if (!trace.maxAgeIterator(1000).hasNext()) {
			fail("Expect iterator (maxAge) to always contain the latest element.");
		}
		trace.addEntry(1, 0, 0, 0);
		final Iterator<TraceEntry> it = trace.maxAgeIterator(2);
		if (!it.hasNext()) {
			fail("Expect iterator (maxAge) to always contain the latest element.");
		}
		it.next();
		if (it.hasNext()) {
			fail("Expect iterator (maxAge) to have only the latest element for all out of range entries.");
		}
	}
	
}
