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

import java.util.Collection;
import java.util.List;

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

    private void testLeftTrim(String input, String expectedResult) {
        String result = StringUtil.leftTrim(input);
        if (!expectedResult.equals(result)) {
            fail("Expect leftTrim for '" + input + "' to return '" + expectedResult + "', got instead: '" + result + "'.");
        }
    }

    @Test
    public void testLeftTrim() {
        if (StringUtil.leftTrim(null) != null) {
            fail("Expect leftTrim to return null for null input, got instead: '" + StringUtil.leftTrim(null) + "'.");
        }
        for (String[] spec : new String[][]{
            {"", ""},
            {" ", ""},
            {" \t", ""},
            {"Z", "Z"},
            {"=(/CG%§87rgv", "=(/CG%§87rgv"},
            {" X", "X"},
            {"Y ", "Y "},
            {"  TEST", "TEST"},
            {"\t\n TEST", "TEST"},
            {"   TEST ", "TEST "}
        }) {
            testLeftTrim(spec[0], spec[1]);
        }
    }

    private void testSplitChars(String input, int expectedLength, char countChar, char... chars) {
        int count = StringUtil.count(input, countChar);
        List<String> res = StringUtil.splitChars(input, chars);
        if (res.size() != expectedLength) {
            fail("Expected length differs. expect=" + expectedLength + " actual=" + res.size());
        }
        if (StringUtil.count(StringUtil.join(res, ""), countChar) != count) {
            fail("Number of countChar has varied between input and output.");
        }
    }

    @Test
    public void testSplitChars() {
        testSplitChars("a,1,.3a-a+6", 6, 'a', ',', '.', '-', '+');
    }

    private void testNonEmpty(Collection<String> nonEmpty) {
        for (String x : nonEmpty) {
            if (x.isEmpty()) {
                fail("Empty string in non empty.");
            }
        }
    }

    private void testNonEmptySplit(String input, int expectedSize, char... chars) {
        List<String> res = StringUtil.getNonEmpty(StringUtil.splitChars(input, chars), true);
        if (res.size() != expectedSize) {
            fail("Expected length differs. expect=" + expectedSize + " actual=" + res.size());
        }
        testNonEmpty(res);
    }

    @Test
    public void testGetNonEmpty() {
        testNonEmptySplit("a,1,.3a-a+6", 5, ',', '.', '-', '+');
    }
}
