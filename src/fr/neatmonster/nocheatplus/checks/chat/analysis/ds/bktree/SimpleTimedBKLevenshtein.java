package fr.neatmonster.nocheatplus.checks.chat.analysis.ds.bktree;

import java.util.Collection;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.bktree.BKModTree.LookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.bktree.SimpleTimedBKLevenshtein.STBKLResult;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.bktree.TimedBKLevenshtein.SimpleTimedLevenNode;

public class SimpleTimedBKLevenshtein extends TimedBKLevenshtein<SimpleTimedLevenNode, STBKLResult> {

	public static class STBKLResult extends LookupEntry<char[], SimpleTimedLevenNode>{
		public STBKLResult(Collection<SimpleTimedLevenNode> nodes, SimpleTimedLevenNode match, int distance, boolean isNew) {
			super(nodes, match, distance, isNew);
		}
		
	}
	
	public SimpleTimedBKLevenshtein() {
		super(
			new NodeFactory<char[], SimpleTimedLevenNode>(){
				@Override
				public SimpleTimedLevenNode newNode( char[] value, SimpleTimedLevenNode parent) {
					return new SimpleTimedLevenNode(value);
				}	
			}
			,
			new LookupEntryFactory<char[], SimpleTimedLevenNode, STBKLResult>() {
				@Override
				public STBKLResult newLookupEntry(Collection<SimpleTimedLevenNode> nodes, SimpleTimedLevenNode match, int distance, boolean isNew) {
					return new STBKLResult(nodes, match, distance, isNew);
				}
			}
			);
	}
}
