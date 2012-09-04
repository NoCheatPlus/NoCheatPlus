package fr.neatmonster.nocheatplus.checks.chat.analysis.ds;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.SimpleTimedCharPrefixTree.SimpleTimedCharLookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.TimedCharPrefixTree.TimedCharLookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.TimedCharPrefixTree.TimedCharNode;

public class SimpleTimedCharPrefixTree extends TimedCharPrefixTree<TimedCharNode, SimpleTimedCharLookupEntry> {
	
	public static class SimpleTimedCharLookupEntry extends TimedCharLookupEntry<TimedCharNode>{
		public SimpleTimedCharLookupEntry(TimedCharNode node, TimedCharNode insertion, int depth, boolean hasPrefix) {
			super(node, insertion, depth, hasPrefix);
		}
	}
	
	public SimpleTimedCharPrefixTree(final boolean access){
		super(
			new NodeFactory<Character, TimedCharNode>(){
				@Override
				public final TimedCharNode newNode(final TimedCharNode parent) {
					final long ts;
					if (parent == null) ts = System.currentTimeMillis();
					else ts = parent.ts;
					return new TimedCharNode(ts);
				}
				}
				,
				 new LookupEntryFactory<Character, TimedCharNode, SimpleTimedCharLookupEntry>() {
					@Override
					public final SimpleTimedCharLookupEntry newLookupEntry(final TimedCharNode node,
							final TimedCharNode insertion, final int depth, final boolean hasPrefix) {
						return new SimpleTimedCharLookupEntry(node, insertion, depth, hasPrefix);
					}
				}, 
				access);
	}

}
