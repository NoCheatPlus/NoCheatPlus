package fr.neatmonster.nocheatplus.checks.chat.analysis.ds;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.PrefixTree.LookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.PrefixTree.Node;


/**
 * Tree for some sequence lookup. <br>
 * Pretty fat, for evaluation purposes.
 * @author mc_dev
 *
 */
public class PrefixTree<K, N extends Node<K>, L extends LookupEntry<K, N>>{
	
	public static class Node<K>{
		public boolean isEnd = false;
		public Map<K, Node<K>> children = null;
		
		public Node(){
			
		}
		
		public Node<K> getChild(final K key, final NodeFactory<K, Node<K>> factory){
			if (children == null){
				if (factory != null) children = new HashMap<K, Node<K>>(3);
				else return null;
			}
			Node<K> node = children.get(key);
			if (node != null) return node;
			else if (factory == null) return null;
			else{
				node = factory.newNode(this);
				children.put(key, node);
				return node;
			}
		}
	}
	
	public static interface NodeFactory<K, N extends Node<K>>{
		/**
		 * 
		 * @param parent Can be null (root).
		 * @return
		 */
		public N newNode(N parent);
	}
	
	public static class LookupEntry<K, N extends Node<K>>{
		/** The node, if lookup matched.*/
		public final N node;
		/** The node at which insertion did/would happen */
		public final N insertion;
		/** Depth to root from insertion point. */
		public final int depth;
		/** If the tree contained a prefix of the sequence, 
		 * i.e. one of the existent nodes matching the input was a leaf. */
		public final boolean hasPrefix;
		public LookupEntry(N node , N insertion, int depth, boolean hasPrefix){
			this.node = node;
			this.insertion = insertion;
			this.depth = depth;
			this.hasPrefix = hasPrefix;
		}
	}
	
	public static interface LookupEntryFactory<K, N extends Node<K>, L extends LookupEntry<K, N>>{
		public L newLookupEntry(N node , N insertion, int depth, boolean hasPrefix); 
	}
	
	protected final NodeFactory<K, N> nodeFactory;
	
	protected final LookupEntryFactory<K, N, L> resultFactory;
	
	protected N root;
	
	public PrefixTree(NodeFactory<K, N> nodeFactory, LookupEntryFactory<K, N, L> resultFactory){
		this.nodeFactory = nodeFactory;
		this.root = nodeFactory.newNode(null);
		this.resultFactory = resultFactory;
	}
	
	public LookupEntry<K, N> lookup(K[] keys, final boolean create){
		return lookup(Arrays.asList(keys), create);
	}
	
	@SuppressWarnings("unchecked")
	public L lookup(final List<K> keys, final boolean create){
		N insertion = root;
		int depth = 0;
		N current = root;
		boolean hasPrefix = false;
		final NodeFactory<K, N> factory = (NodeFactory<K, N>) (create ? this.nodeFactory : null);
		for (final K key : keys){
			final N child = (N) current.getChild(key, null);
			if (child == null){
				if (factory == null)
					break;
				else{
					current = (N) current.getChild(key, (NodeFactory<K, Node<K>>) factory);
				}
			}
			else{
				// A node already exists, set as insertion point.
				insertion = current = child;
				depth ++;
				if (child.isEnd) hasPrefix = true;
			}
		}
		N node = null;
		if (create){
			node = current;
			current.isEnd = true;
		}
		else if (depth == keys.size()){
			node = current;
		}
		return resultFactory.newLookupEntry(node, insertion, depth, hasPrefix);
	}

	public void clear() {
		root = nodeFactory.newNode(null);
		// TODO: maybe more unlinking ?
	}
	
	/**
	 * Factory method for a simple tree.
	 * @param keyType
	 * @return
	 */
	public static <K> PrefixTree<K, Node<K>, LookupEntry<K, Node<K>>> newPrefixTree(){
		return new PrefixTree<K, Node<K>, LookupEntry<K, Node<K>>>(new NodeFactory<K, Node<K>>(){
			@Override
			public final Node<K> newNode(final Node<K> parent) {
				return new Node<K>();
			}
		}, new LookupEntryFactory<K, Node<K>, LookupEntry<K,Node<K>>>() {
			@Override
			public LookupEntry<K, Node<K>> newLookupEntry(Node<K> node, Node<K> insertion, int depth, boolean hasPrefix) {
				return new LookupEntry<K, PrefixTree.Node<K>>(node, insertion, depth, hasPrefix);
			}
		});
	}
}
