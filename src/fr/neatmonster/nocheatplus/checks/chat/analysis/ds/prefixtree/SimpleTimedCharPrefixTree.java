package fr.neatmonster.nocheatplus.checks.chat.analysis.ds.prefixtree;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.prefixtree.SimpleTimedCharPrefixTree.SimpleTimedCharLookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.prefixtree.TimedCharPrefixTree.SimpleTimedCharNode;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.prefixtree.TimedCharPrefixTree.TimedCharLookupEntry;

public class SimpleTimedCharPrefixTree extends TimedCharPrefixTree<SimpleTimedCharNode, SimpleTimedCharLookupEntry> {
	
	public static class SimpleTimedCharLookupEntry extends TimedCharLookupEntry<SimpleTimedCharNode>{
		public SimpleTimedCharLookupEntry(SimpleTimedCharNode node, SimpleTimedCharNode insertion, int depth, boolean hasPrefix) {
			super(node, insertion, depth, hasPrefix);
		}
	}
	
	public SimpleTimedCharPrefixTree(final boolean access){
		super(
			new NodeFactory<Character, SimpleTimedCharNode>(){
				@Override
				public final SimpleTimedCharNode newNode(final SimpleTimedCharNode parent) {
					final long ts;
					if (parent == null) ts = System.currentTimeMillis();
					else ts = parent.ts;
					return new SimpleTimedCharNode(ts);
				}
				}
				,
				 new LookupEntryFactory<Character, SimpleTimedCharNode, SimpleTimedCharLookupEntry>() {
					@Override
					public final SimpleTimedCharLookupEntry newLookupEntry(final SimpleTimedCharNode node,
							final SimpleTimedCharNode insertion, final int depth, final boolean hasPrefix) {
						return new SimpleTimedCharLookupEntry(node, insertion, depth, hasPrefix);
					}
				}, 
				access);
	}

}
