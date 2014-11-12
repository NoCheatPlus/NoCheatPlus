package fr.neatmonster.nocheatplus.test;


import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Tests for StringUtil.
 * @author dev1mc
 *
 */
public class TestStringUtil {
    
    private void assertCount(String data, char searchFor, int num) {
        int res = StringUtil.count(data, searchFor);
        if (res != num) {
            fail("Expect to find '" + searchFor + "' " + num + " times in '" + data + "', got instead: " + res);
        }
    }
    
    @Test
    public void testCount() {
        assertCount("" , 'x', 0);
        assertCount("o" , 'x', 0);
        assertCount("x" , 'x', 1);
        assertCount("xo" , 'x', 1);
        assertCount("ox" , 'x', 1);
        assertCount("oxo" , 'x', 1);
        assertCount("xox" , 'x', 2);
        assertCount("xxo" , 'x', 2);
        assertCount("oxx" , 'x', 2);
        assertCount("230489tuvn1374z1hxk,34htmc1", '3', 3);
    }
    
    private void recursiveFail(int now, int max) {
        now ++;
        if (now >= max) {
            throw new RuntimeException("Reached max. recursion depth: " + max);
        }
        else {
            recursiveFail(now, max);
        }
    }
    
    /**
     * Indirectly by counting line breaks with StringUtil.
     * @param recursionDepth
     * @param minSize
     * @param trim
     */
    private void assertMinimumStackTraceLength(int recursionDepth, int minSize, boolean trim) {
        try {
            recursiveFail(0, recursionDepth);
        } catch (RuntimeException ex) {
            String s = StringUtil.stackTraceToString(ex, true, trim);
            int n = StringUtil.count(s, '\n');
            if (n < minSize) {
                fail("Excpect at least " + minSize +  " line breaks, got instead: " + n);
            }
        }
    }
    
    /**
     * Indirectly by counting line breaks with StringUtil.
     * @param recursionDepth
     * @param maxSize
     * @param trim
     */
    private void assertMaximumStackTraceLength(int recursionDepth, int maxSize, boolean trim) {
        try {
            recursiveFail(0, recursionDepth);
        } catch (RuntimeException ex) {
            String s = StringUtil.stackTraceToString(ex, true, trim);
            int n = StringUtil.count(s, '\n');
            if (n > maxSize) {
                fail("Excpect at most " + maxSize + " line breaks, got instead: " + n);
            }
        }
    }
    
    @Test
    public void testStackTraceLinear() {
        assertMinimumStackTraceLength(1000, 1000, false);
    }
    
    @Test
    public void testStackTraceTrimmed() {
        assertMaximumStackTraceLength(1000, 50, true);
    }
    
}
