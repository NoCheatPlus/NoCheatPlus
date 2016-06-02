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
package fr.neatmonster.nocheatplus.utilities.ds.prefixtree;

import java.util.Arrays;
import java.util.List;

import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.TimedCharPrefixTree.TimedCharLookupEntry;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.TimedCharPrefixTree.TimedCharNode;

public class TimedCharPrefixTree<N extends TimedCharNode<N>, L extends TimedCharLookupEntry<N>> extends CharPrefixTree<N, L> {

	public static class TimedCharLookupEntry<N extends TimedCharNode<N>> extends CharLookupEntry<N>{
		public long[] timeInsertion = null;
		public TimedCharLookupEntry(N node, N insertion, int depth, boolean hasPrefix){
			super(node, insertion, depth, hasPrefix);
		}
	}
	
	public static class TimedCharNode<N extends TimedCharNode<N>> extends CharNode<N>{
		public long ts = 0;
		public TimedCharNode(long ts){
			this.ts = ts;
		}
	}
	
	public static class SimpleTimedCharNode extends TimedCharNode<SimpleTimedCharNode>{
		public SimpleTimedCharNode(long ts) {
			super(ts);
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
	public static TimedCharPrefixTree<SimpleTimedCharNode, TimedCharLookupEntry<SimpleTimedCharNode>> newTimedCharPrefixTree(final boolean access) {
		return new TimedCharPrefixTree<SimpleTimedCharNode, TimedCharLookupEntry<SimpleTimedCharNode>>(
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
			 new LookupEntryFactory<Character, SimpleTimedCharNode, TimedCharLookupEntry<SimpleTimedCharNode>>() {
				@Override
				public final TimedCharLookupEntry<SimpleTimedCharNode> newLookupEntry(final SimpleTimedCharNode node,
						final SimpleTimedCharNode insertion, final int depth, final boolean hasPrefix) {
					return new TimedCharLookupEntry<SimpleTimedCharNode>(node, insertion, depth, hasPrefix);
				}
			},
			access
		);
	}

}
