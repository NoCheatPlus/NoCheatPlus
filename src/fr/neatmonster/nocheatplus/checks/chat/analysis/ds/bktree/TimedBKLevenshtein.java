package fr.neatmonster.nocheatplus.checks.chat.analysis.ds.bktree;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.bktree.BKModTree.LookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.bktree.TimedBKLevenshtein.TimedLevenNode;

public class TimedBKLevenshtein<N extends TimedLevenNode<N>, L extends LookupEntry<char[], N>> extends BKLevenshtein<N, L> {

	
	public static class TimedLevenNode<N extends TimedLevenNode<N>> extends LevenNode<N>{
		public long ts;
		/**
		 * Set time to now.
		 * @param value
		 */
		public TimedLevenNode(char[] value) {
			super(value);
			this.ts = System.currentTimeMillis();
		}
		public TimedLevenNode(char[] value, long ts){
			super(value);
			this.ts = ts;
		}
	}
	
	public static class SimpleTimedLevenNode extends TimedLevenNode<SimpleTimedLevenNode>{
		public SimpleTimedLevenNode(char[] value) {
			super(value);
		}
		public SimpleTimedLevenNode(char[] value, long ts){
			super(value, ts);
		}
	}
	
	public TimedBKLevenshtein(NodeFactory<char[], N> nodeFactory, LookupEntryFactory<char[], N, L> resultFactory) {
		super(nodeFactory, resultFactory);
	}

}
