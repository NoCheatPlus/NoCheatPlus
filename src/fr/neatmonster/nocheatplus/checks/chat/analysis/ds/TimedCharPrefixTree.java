package fr.neatmonster.nocheatplus.checks.chat.analysis.ds;

import java.util.Arrays;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.TimedCharPrefixTree.TimedCharLookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.TimedCharPrefixTree.TimedCharNode;

public class TimedCharPrefixTree<N extends TimedCharNode, L extends TimedCharLookupEntry<N>> extends CharPrefixTree<N, L> {

	public static class TimedCharLookupEntry<N extends TimedCharNode> extends CharLookupEntry<N>{
		public long[] timeInsertion = null;
		public TimedCharLookupEntry(N node, N insertion, int depth, boolean hasPrefix){
			super(node, insertion, depth, hasPrefix);
		}
	}
	
	public static class TimedCharNode extends CharNode{
		public long ts = 0;
		public TimedCharNode(long ts){
			this.ts = ts;
		}
	}
	
	protected long ts;
	
	protected long[] timeInsertion = new long[200];
	
	protected final boolean access;
	
	protected boolean updateTime = false;
	
	protected int depth;
	
	protected float arrayGrowth = 1.3f;
	
	/**
	 * 
	 * @param nodeFactory
	 * @param resultFactory
	 * @param access If to set timestamps, even if create is false.
	 */
	public TimedCharPrefixTree(final NodeFactory<Character, N> nodeFactory, final LookupEntryFactory<Character, N, L> resultFactory, final boolean access) {
		super(nodeFactory, resultFactory);
		visit = true;
		this.access = access;
	}
	
	@Override
	public L lookup(final List<Character> keys, final boolean create) {
		ts = System.currentTimeMillis();
		updateTime = access || create;
		depth = 0;
		return super.lookup(keys, create);
	}

	@Override
	protected void visit(final N node) {
		if (depth == timeInsertion.length){
			// This might be excluded by contract.
			timeInsertion = Arrays.copyOf(timeInsertion, (int) (timeInsertion.length * arrayGrowth));
		}
		timeInsertion[depth] = node.ts;
		if (updateTime) node.ts = ts;
		depth ++;
	}

	@Override
	protected void decorate(final L result) {
		result.timeInsertion = Arrays.copyOf(timeInsertion, depth);
	}

	/**
	 * 
	 * @param access If to set timestamps, even if create is false.
	 * @return
	 */
	public static TimedCharPrefixTree<TimedCharNode, TimedCharLookupEntry<TimedCharNode>> newTimedCharPrefixTree(final boolean access) {
		return new TimedCharPrefixTree<TimedCharNode, TimedCharLookupEntry<TimedCharNode>>(
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
			 new LookupEntryFactory<Character, TimedCharNode, TimedCharLookupEntry<TimedCharNode>>() {
				@Override
				public final TimedCharLookupEntry<TimedCharNode> newLookupEntry(final TimedCharNode node,
						final TimedCharNode insertion, final int depth, final boolean hasPrefix) {
					return new TimedCharLookupEntry<TimedCharNode>(node, insertion, depth, hasPrefix);
				}
			},
			access
		);
	}

}
