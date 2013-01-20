package fr.neatmonster.nocheatplus.utilities.ds.prefixtree;

import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.CharPrefixTree.CharLookupEntry;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.CharPrefixTree.SimpleCharNode;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree.SimpleCharLookupEntry;

/**
 * Adding some convenience methods.
 * @author mc_dev
 *
 */
public class SimpleCharPrefixTree extends CharPrefixTree<SimpleCharNode, SimpleCharLookupEntry>{

	public static class SimpleCharLookupEntry extends CharLookupEntry<SimpleCharNode>{
		public SimpleCharLookupEntry(SimpleCharNode node, SimpleCharNode insertion, int depth, boolean hasPrefix) {
			super(node, insertion, depth, hasPrefix);
		}
	}
	
	public SimpleCharPrefixTree(){
		super(new NodeFactory<Character, SimpleCharNode>(){
			@Override
			public final SimpleCharNode newNode(final SimpleCharNode parent) {
				return new SimpleCharNode();
			}
		}, new LookupEntryFactory<Character, SimpleCharNode, SimpleCharLookupEntry>() {
			@Override
			public final SimpleCharLookupEntry newLookupEntry(final SimpleCharNode node, final SimpleCharNode insertion, final int depth, final boolean hasPrefix) {
				return new SimpleCharLookupEntry(node, insertion, depth, hasPrefix);
			}
		});
	}

}
