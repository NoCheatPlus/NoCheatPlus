package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;
import fr.neatmonster.nocheatplus.utilities.ds.map.CoordHashMap;
import fr.neatmonster.nocheatplus.utilities.ds.map.CoordMap;
import fr.neatmonster.nocheatplus.utilities.ds.map.CoordMap.Entry;

public class TestCoordMap {
	
	public static class Pos{
		private static final int p1 = 73856093;
		private static final int p2 = 19349663;
		private static final int p3 = 83492791;
		private static final int getHash(final int x, final int y, final int z) {
			return p1 * x ^ p2 * y ^ p3 * z;
		}
		public final int x;
		public final int y;
		public final int z;
		private final int hash;
		public Pos(int x, int y, int z){
			this.x = x;
			this.y = y;
			this.z = z;
			this.hash = getHash(x, y, z);
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Pos){
				Pos other = (Pos) obj;
				return other.hash == hash && other.x == x && other.y == y && other.z == z;
			}
			else return false;
		}
		@Override
		public int hashCode() {
			return hash;
		}
	}
	
	public int[][] getRandomCoords(int n, int max, Random random) {
		final int [][] coords = new int[n][3];
		for (int i = 0; i < n; i++){
			for (int j = 0; j < 3 ; j++){
				coords[i][j] = random.nextInt(2*max) - max;
			}
		}
		return coords;
	}
	
	public int[][] getUniqueRandomCoords(int n, int max, Random random) {
		Set<Pos> present = new HashSet<Pos>();
		int failures = 0;
		final int [][] coords = new int[n][3];
		for (int i = 0; i < n; i++){
			boolean unique = false;
			Pos pos = null;
			while (!unique){
				pos = new Pos(random.nextInt(2*max) - max, random.nextInt(2*max) - max, random.nextInt(2*max) - max);
				if (!present.contains(pos)) break;
				failures ++;
				if (failures >= 2 * n){
					throw new RuntimeException("Too many failed attempts to create a unique coordinate.");
				}
			}
			coords[i][0] = pos.x;
			coords[i][1] = pos.y;
			coords[i][2] = pos.z;
			present.add(pos);
		}
		present.clear();
		return coords;
	}
	
	public Map<Integer, int[]> getIndexMap(int[][] coords) {
		final Map<Integer, int[]> indexMap = new HashMap<Integer, int[]>(coords.length);
		for (int i = 0; i < coords.length; i ++){
			indexMap.put(i, coords[i]);
		}
		return indexMap;
	}
	
	/**
	 * Fill map and check if filled in elements are inside (no size check).
	 * @param map
	 * @param coords
	 */
	public void fillMap(CoordMap<Integer> map, int[][] coords) {
		for (int i = 0; i < coords.length ; i++){
			map.put(coords[i][0], coords[i][1], coords[i][2], i);
			Integer value = map.get(coords[i][0], coords[i][1], coords[i][2]);
			if (value == null) fail("Value is null, get after put: " + i);
			else if (value.intValue() != i) fail("get right after put");
			if (!map.contains(coords[i][0], coords[i][1], coords[i][2])) fail("Contains returns false: " + i);
		}
	}
	
	/**
	 * Match map contents (must match exactly).
	 * @param map
	 * @param coords
	 */
	public void matchAll(CoordMap<Integer> map, int[][] coords) {
		for (int i = 0; i < coords.length ; i++){
			Integer value = map.get(coords[i][0], coords[i][1], coords[i][2]).intValue();
			if (value == null) fail("Value is null instead of " + i);
			if (value.intValue() != i) fail("Wrong value: " + value + " vs. " + i);
			if (!map.contains(coords[i][0], coords[i][1], coords[i][2])) fail("Contains returns false.");
		}
		if (map.size() != coords.length) fail("Iterator wrong number of elements: " + map.size() + "/" + coords.length);

	}
	
	/**
	 * Match map contents with an (must match exactly).
	 * @param map
	 * @param indexMap
	 */
	public void matchAllIterator(CoordMap<Integer> map, Map<Integer, int[]> indexMap){
		Iterator<Entry<Integer>> it = map.iterator();
		Set<Integer> found = new HashSet<Integer>();
		while (it.hasNext()){
			Entry<Integer> entry = it.next();
			Integer value = entry.getValue();
			if (value == null) fail("Null value.");
			int[] pos = indexMap.get(value);
//			if (pos == null) fail
			if (pos[0] != entry.getX() || pos[1] != entry.getY() || pos[2] != entry.getZ()) fail("Wrong coordinates.");
			if (map.get(pos[0], pos[1], pos[2]).intValue() != value.intValue()) fail("Wrong value.");
			if (found.contains(value)) fail("Already found: " + value);
			if (!map.contains(pos[0], pos[1], pos[2])) fail("Contains returns false");
			found.add(value);
		}
		if (found.size() != indexMap.size()) fail("Iterator wrong number of elements: " + found.size() + "/" + indexMap.size());

	}
	
	/**
	 * Remove all coords (expect map to be only filled with those).
	 * @param map
	 * @param coords
	 */
	public void removeAll(CoordMap<Integer> map, int[][] coords) {
		for (int i = 0; i < coords.length ; i++){
			if (map.remove(coords[i][0], coords[i][1], coords[i][2]).intValue() != i) fail("removed should be " + i );
			int expectedSize = coords.length - (i + 1);
			if (map.size() != expectedSize) fail("Bad size (" + map.size() + "), expect " + expectedSize);
			if (map.get(coords[i][0], coords[i][1], coords[i][2]) != null) fail("get right after remove not null");
			if (map.contains(coords[i][0], coords[i][1], coords[i][2])) fail ("Still contains");
		}
		if (map.size() != 0) fail("Map not emptied, left: " + map.size());
	}
	
	/**
	 *  Remove all coords using an iterator (expect map to be only filled with those).
	 * @param map
	 * @param indexMap
	 */
	public void removeAllIterator(CoordMap<Integer> map, Map<Integer, int[]> indexMap)
	{
		Iterator<Entry<Integer>> it = map.iterator();
		Set<Integer> removed = new HashSet<Integer>();
		while (it.hasNext()){
			Entry<Integer> entry = it.next();
			Integer value = entry.getValue();
			if (value == null) fail("Null value.");
			int[] pos = indexMap.get(value);
//			if (pos == null) fail
			if (pos[0] != entry.getX() || pos[1] != entry.getY() || pos[2] != entry.getZ()) fail("Wrong coordinates.");
			if (removed.contains(value)) fail("Already removed: " + value);
			removed.add(value);
			it.remove();
			if (map.get(pos[0], pos[1], pos[2]) != null) fail("get right after remove not null");
			if (map.contains(pos[0], pos[1], pos[2])) fail("Still contains.");
		}
		if (map.size() != 0) fail("Map not emptied, left: " + map.size());
		if (removed.size() != indexMap.size()) fail("Iterator wrong number of elements: " + removed.size() + "/" + indexMap.size());
	}
	
	public void assertSize(CoordMap<?> map, int size){
		if (map.size() != size) fail("Map returns wrong size: " + map.size() + " instead of " + size);
		int found = 0;
		final Iterator<?> it = map.iterator();
		while (it.hasNext()){
			found ++;
			it.next();
		}
		if (found != size) fail("Iterator has wrong number of elements: " + found + " instead of " + size);
	}

	/**
	 * One integrity test series with a map with given initial size.
	 * @param coords
	 * @param indexMap
	 * @param initialSize
	 */
	public  void series(int[][] coords, Map<Integer, int[]> indexMap, int initialSize, float loadFactor) {
		CoordMap<Integer> map; 
		
		// Fill and check
		map = new CoordHashMap<Integer>(initialSize, loadFactor);
		fillMap(map, coords);
		assertSize(map, indexMap.size());
		matchAll(map, coords);
		
		// Fill and check with iterator.
		map = new CoordHashMap<Integer>(initialSize, loadFactor);
		fillMap(map, coords);
		assertSize(map, indexMap.size());
		matchAllIterator(map, indexMap);
		
		// Normal removing
		map = new CoordHashMap<Integer>(initialSize, loadFactor);
		fillMap(map, coords);
		assertSize(map, indexMap.size());
		removeAll(map, coords);
		assertSize(map, 0);
		
		// Removing with iterator.
		map = new CoordHashMap<Integer>(initialSize, loadFactor);
		fillMap(map, coords);
		assertSize(map, indexMap.size());
		removeAllIterator(map, indexMap);
		assertSize(map, 0);
		
		// Fill twice.
		map = new CoordHashMap<Integer>(initialSize, loadFactor);
		fillMap(map, coords);
		assertSize(map, indexMap.size());
		fillMap(map, coords);
		assertSize(map, indexMap.size());
		matchAll(map, coords);
		removeAll(map, coords);
		assertSize(map, 0);
		
		// Fill twice iterator.
		map = new CoordHashMap<Integer>(initialSize, loadFactor);
		fillMap(map, coords);
		assertSize(map, indexMap.size());
		fillMap(map, coords);
		assertSize(map, indexMap.size());
		matchAllIterator(map, indexMap);
		removeAllIterator(map, indexMap);
		assertSize(map, 0);
		
		// TODO: test / account for identical keys.
		
		// ? random sequence of actions ?
	}
	
	@Test
	public void testIntegrity() {

		final Random random = new Random(System.nanoTime() - (System.currentTimeMillis() % 2 == 1 ? 37 : 137));
		
		final boolean e = BuildParameters.testLevel > 0;
		
		final int n = e ? 40000 : 6000; // Number of coordinates.
		final int max = 800; // Coordinate maximum.
		
		int [][] coords = getUniqueRandomCoords(n, max, random);
		
		Map<Integer, int[]> indexMap = getIndexMap(coords);
		
		// No resize 
		series(coords, indexMap, 3 * n, 0.75f);

		// With some times resize.
		series(coords, indexMap, 1, 0.75f);
		
		// TODO: Also test with certain sets of coords that always are the same.
		// TODO: fill in multiple times + size, fill in new ones (careful random) + size
	}
	
}
