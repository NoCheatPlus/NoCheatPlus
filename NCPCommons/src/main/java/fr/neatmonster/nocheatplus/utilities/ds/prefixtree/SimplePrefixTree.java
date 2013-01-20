package fr.neatmonster.nocheatplus.utilities.ds.prefixtree;

import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.PrefixTree.LookupEntry;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.PrefixTree.SimpleNode;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimplePrefixTree.SimpleLookupEntry;

/**
 * Simple prefix tree, for one given key type.
 * <hr>
 * Interesting: ctrl-shift-o (eclipse) is not idempotent.
 * @author mc_dev
 *
 * @param <K>
 */
public class SimplePrefixTree<K> extends PrefixTree<K, SimpleNode<K>, SimpleLookupEntry<K>> {

	public static class SimpleLookupEntry<K> extends LookupEntry<K, SimpleNode<K>>{
		public SimpleLookupEntry(SimpleNode<K> node, SimpleNode<K> insertion, int depth, boolean hasPrefix) {
			super(node, insertion, depth, hasPrefix);
		}
	}
	
	public SimplePrefixTree() {
		super(new NodeFactory<K, SimpleNode<K>>(){
			@Override
			public final SimpleNode<K> newNode(final SimpleNode<K> parent) {
				return new SimpleNode<K>();
			}
		}, new LookupEntryFactory<K, SimpleNode<K>, SimpleLookupEntry<K>>(){
			@Override
			public SimpleLookupEntry<K> newLookupEntry(
					SimpleNode<K> node, SimpleNode<K> insertion,
					int depth, boolean hasPrefix) {
				return new SimpleLookupEntry<K>(node, insertion, depth, hasPrefix);
			}
		});
		
	}	
}
