package fr.neatmonster.nocheatplus.checks.chat.analysis.ds;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.CharPrefixTree.CharLookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.CharPrefixTree.CharNode;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.SimpleCharPrefixTree.SimpleCharLookupEntry;

/**
 * Adding some convenience methods.
 * @author mc_dev
 *
 */
public class SimpleCharPrefixTree extends CharPrefixTree<CharNode, SimpleCharLookupEntry>{

	public static class SimpleCharLookupEntry extends CharLookupEntry<CharNode>{
		public SimpleCharLookupEntry(CharNode node, CharNode insertion, int depth, boolean hasPrefix) {
			super(node, insertion, depth, hasPrefix);
		}
	}
	
	public SimpleCharPrefixTree(){
		super(new NodeFactory<Character, CharNode>(){
			@Override
			public final CharNode newNode(final CharNode parent) {
				return new CharNode();
			}
		}, new LookupEntryFactory<Character, CharNode, SimpleCharLookupEntry>() {
			@Override
			public final SimpleCharLookupEntry newLookupEntry(final CharNode node, final CharNode insertion, final int depth, final boolean hasPrefix) {
				return new SimpleCharLookupEntry(node, insertion, depth, hasPrefix);
			}
		});
	}

}
