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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree;

public class TestSimpleCharPrefixTree {
    private List<String> feed = Arrays.asList(
            "op", "op dummy", "ncp info"
            );

    private List<String> mustFind = Arrays.asList(
            "op", "op dummy", "ncp info", "ncp info test"
            );

    private List<String> mustNotFindWords = Arrays.asList(
            "opp", "opp dummy", "op dummy2", "ncp", "ncp dummy"
            );

    private List<String> mustNotFindPrefix = Arrays.asList(
            "ok", "ncp", "ncp dummy", "ncp inf"
            );

    /** Numbers are neither prefix nor suffix of each other. */
    private List<String> uniqueNumbers = Arrays.asList(
            "123456", "2345678", "34567", "456789"
            );

    @Test
    public void testPrefixWords(){
        SimpleCharPrefixTree tree = new SimpleCharPrefixTree();
        tree.feedAll(feed, false, true);
        for (String input : mustFind){
            if (!tree.hasPrefixWords(input)){
                fail("Expect to be matched: '" + input + "'");
            }
        }
        for (String input : mustNotFindWords){
            if (tree.hasPrefixWords(input)){
                fail("Expect not to be matched: '" + input + "'");
            }
        }
    }

    @Test
    public void testHasPrefix() {
        SimpleCharPrefixTree tree = new SimpleCharPrefixTree();
        tree.feedAll(feed, false, true);
        // Same tests as with prefix words.
        for (String input : mustFind){
            if (!tree.hasPrefix(input)){
                fail("Expect to be matched: '" + input + "'");
            }
        }
        for (String input : mustNotFindPrefix){
            if (tree.hasPrefix(input)){
                fail("Expect not to be matched: '" + input + "'");
            }
        }
        // Extra
        if (!tree.hasPrefix("ncp infocrabs")) {
            fail("'ncp info' should be a prefix of 'ncp infocrabs'.");
        }
    }

    @Test
    public void testSuffixTree() {
        SimpleCharPrefixTree prefixTree = new SimpleCharPrefixTree();
        prefixTree.feedAll(uniqueNumbers, false, true);
        SimpleCharPrefixTree suffixTree = new SimpleCharPrefixTree();
        for (String key : uniqueNumbers) {
            suffixTree.feed(StringUtil.reverse(key));
        }
        for (String input : uniqueNumbers) {
            if (!prefixTree.hasPrefix(input)) {
                fail("Fed data not matching prefix tree: " + input);
            }
            if (suffixTree.hasPrefix(input)) {
                fail("Non-reversed data is matching suffix tree: " + input);
            }
        }
        for (String input : uniqueNumbers) {
            input = StringUtil.reverse(input);
            if (prefixTree.hasPrefix(input)) {
                fail("Reversed fed data is matching prefix tree: " + input);
            }
            if (!suffixTree.hasPrefix(input)) {
                fail("Reversed fed data not matching suffix tree: " + input);
            }
        }
    }

}
