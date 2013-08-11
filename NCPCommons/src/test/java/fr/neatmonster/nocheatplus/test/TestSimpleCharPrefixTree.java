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
