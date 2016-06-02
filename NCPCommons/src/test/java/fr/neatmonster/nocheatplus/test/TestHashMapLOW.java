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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.ds.map.HashMapLOW;

/**
 * Tests for HashMapLow.
 * @author asofold
 *
 */
public class TestHashMapLOW {

    // TODO: fill after remove, etc.
    // TODO: Concurrent iteration / adding / removal.

    /**
     * Basic tests.
     */
    @Test
    public void testBase() {
        HashMap<String, Integer> refMap = new LinkedHashMap<String, Integer>();
        HashMapLOW<String, Integer> map = new HashMapLOW<String, Integer>(100);

        testSize(map, refMap);
        fill(map, refMap, 1000, true);
        testSize(map,refMap);
        testValuesIdentity(map, refMap);
        testIterator(map);

        map.clear();
        refMap.clear();
        if (!map.isEmpty()) {
            fail("Expect map to be empty after clear.");
        }

    }

    /**
     * Ordinary removal.
     */
    @Test
    public void testRemove() {
        HashMap<String, Integer> refMap = new LinkedHashMap<String, Integer>();
        HashMapLOW<String, Integer> map = new HashMapLOW<String, Integer>(100);
        fill(map, refMap, 1000, true);

        int i = 0;
        int initialSize = map.size();
        for (Entry<String, Integer> entry : refMap.entrySet()) {
            map.remove(entry.getKey());
            i ++;
            testRemoveStage(map, initialSize, i, entry);
        }
    }

    /**
     * Remove elements using an iterator.
     */
    @Test
    public void testRemoveWithIterator() {
        HashMap<String, Integer> refMap = new LinkedHashMap<String, Integer>();
        HashMapLOW<String, Integer> map = new HashMapLOW<String, Integer>(100);
        fill(map, refMap, 1000, true);

        int i = 0;
        int initialSize = map.size();
        Iterator<Entry<String, Integer>> it = map.iterator();
        while (it.hasNext()) {
            Entry<String, Integer> entry = it.next();
            it.remove();
            i++;
            testRemoveStage(map, initialSize, i, entry);
        }
    }

    @Test
    public void testReplaceValues() {
        HashMap<String, Integer> refMap = new LinkedHashMap<String, Integer>();
        HashMapLOW<String, Integer> map = new HashMapLOW<String, Integer>(100);
        fill(map, refMap, 1000, true);
        for (Entry<String, Integer> entry : refMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            Integer tempValue = -value;
            // Override with tempValue (put).
            map.put(key, tempValue);
            testSize(map, refMap); // Must stay the same.
            testInPlace(map, key, tempValue);
            // Reset to the value before (put).
            map.put(key, value);
            testSize(map, refMap);
            testInPlace(map, key, value);
        }
        testValuesIdentity(map, refMap); // Finally check all.
    }

    @Test
    public void testReplaceValuesIterator() {
        HashMap<String, Integer> refMap = new LinkedHashMap<String, Integer>();
        HashMapLOW<String, Integer> map = new HashMapLOW<String, Integer>(100);
        fill(map, refMap, 1000, true);
        Iterator<Entry<String, Integer>> it = map.iterator();
        while (it.hasNext()) {
            Entry<String, Integer> entry = it.next();
            String key = entry.getKey();
            Integer value = entry.getValue();
            Integer tempValue = -value;
            // Override with tempValue (put).
            map.put(key, tempValue);
            testSize(map, refMap); // Must stay the same.
            testInPlace(map, key, tempValue);
            // Reset to the value before (put).
            map.put(key, value);
            testSize(map, refMap);
            testInPlace(map, key, value);
            // Override with tempValue (entry.setValue).
            entry.setValue(tempValue);
            testSize(map, refMap); // Must stay the same.
            testInPlace(map, key, tempValue);
            // Reset to the value before (entry.setValue).
            entry.setValue(value);
            testSize(map, refMap);
            testInPlace(map, key, value);
        }
    }

    private void testInPlace(HashMapLOW<String, Integer> map, String key, Integer value) {
        if (map.get(key) != value) {
            fail("Overriding a value in-place fails. Got " + map.get(key) + " instead of " + value);
        }
    }

    private void testRemoveStage(HashMapLOW<String, Integer> map, int initialSize, int removed, Entry<String, Integer> entry) {
        if (map.size() != initialSize - removed) {
            fail("Exepect entries to decrease from " + initialSize + " to " + (initialSize - removed) + " after removing " + removed + " elements.");
        }
        if (map.containsKey(entry.getKey())) {
            fail("Removed key still inside map: " + entry.getKey());
        }
        testIterator(map); // Somewhat concurrent.
    }

    private void testIterator(HashMapLOW<String, Integer> map) {
        Iterator<Entry<String, Integer>> it = map.iterator();
        Map<String, Integer> refMap2 = new HashMap<String, Integer>();
        int i = 0;
        while (it.hasNext()) {
            i++;
            Entry<String, Integer> entry = it.next();
            refMap2.put(entry.getKey(), entry.getValue());
        }
        if (i != map.size()) {
            fail("Number of elements iterated is different to map size.");
        }
        // Test values identity vs. refMap2.
        testValuesIdentity(map, refMap2);
        testSize(map, refMap2);
        // (Should not need refMap.equals(refMap2)?)
    }

    /**
     * (Size is tested with testSize.)
     * @param map
     * @param refMap
     */
    private void testValuesIdentity(HashMapLOW<String, Integer> map, Map<String, Integer> refMap) {
        for (Entry<String, Integer> entry : refMap.entrySet()) {
            // Assume identity of objects.
            if (map.get(entry.getKey()) != entry.getValue()) {
                fail("Inconsistent entry: expect " + entry.getValue() + " for key " + entry.getKey() + ", got instead: " + map.get(entry.getKey()));
            }
        }
    }

    /**
     * Fill maps with identical keys and values 0 to maxCount - 1, String ->
     * Integer with the 'same' content.
     * 
     * @param map
     * @param refMap
     * @param maxCount
     * @param testSize
     */
    private void fill(HashMapLOW<String, Integer> map, Map<String, Integer> refMap, int maxCount, boolean testSize) {
        for (int i = 0; i < maxCount; i++) {
            String key = Integer.toString(i);
            Integer value = i;
            map.put(key, value);
            if (!map.containsKey(key)) {
                fail("Key missing after put: " + key);
            }
            refMap.put(key, value);
            if (testSize) {
                testSize(map, refMap);
            }
        }
    }

    private void testSize(HashMapLOW<?, ?> map, Map<?, ?> refMap) {
        if (map.size() != refMap.size()) {
            fail("Sizes differ: low=" + map.size() + " ref=" + refMap.size());
        }
        if (map.size() == 0 && !map.isEmpty()) {
            fail("Expect isEmpty() on size == 0.");
        }
        if (map.size() > 0 && map.isEmpty()) {
            fail("Expect !isEmpty() on size > 0");
        }
        if (map.size() < 0) {
            fail("Expect size >= 0");
        }
    }

}
