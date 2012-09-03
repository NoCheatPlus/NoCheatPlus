package fr.neatmonster.nocheatplus.checks.chat.analysis.ds;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.PrefixTree.LookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.PrefixTree.Node;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.SimplePrefixTree.SimpleLookupEntry;

/**
 * Simple prefix tree, for one given key type.
 * <hr>
 * Interesting: ctrl-shift-o (eclipse) is not idempotent.
 * @author mc_dev
 *
 * @param <K>
 */
public class SimplePrefixTree<K> extends PrefixTree<K, Node<K>, SimpleLookupEntry<K>> {

	public static class SimpleLookupEntry<K> extends LookupEntry<K, Node<K>>{
		public SimpleLookupEntry(Node<K> node, Node<K> insertion, int depth, boolean hasPrefix) {
			super(node, insertion, depth, hasPrefix);
		}
	}
	
	public SimplePrefixTree() {
		super(new NodeFactory<K, Node<K>>(){
			@Override
			public final Node<K> newNode(final Node<K> parent) {
				return new Node<K>();
			}
		}, new LookupEntryFactory<K, Node<K>, SimpleLookupEntry<K>>(){
			@Override
			public SimpleLookupEntry<K> newLookupEntry(
					Node<K> node, Node<K> insertion,
					int depth, boolean hasPrefix) {
				return new SimpleLookupEntry<K>(node, insertion, depth, hasPrefix);
			}
		});
		
	}	
}
