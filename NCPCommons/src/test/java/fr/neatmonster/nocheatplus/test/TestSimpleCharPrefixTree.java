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

import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree;

public class TestSimpleCharPrefixTree {
	private List<String> feed = Arrays.asList(
		"op", "op dummy", "ncp info"
		);
	
	private List<String> mustFind = Arrays.asList(
			"op", "op dummy", "ncp info", "ncp info test"
			);
	
	private List<String> mustNotFind = Arrays.asList(
			"opp", "opp dummy", "op dummy2", "ncp", "ncp dummy"
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
		for (String input : mustNotFind){
			if (tree.hasPrefixWords(input)){
				fail("Expect not to be matched: '" + input + "'");
			}
		}
	}
}
