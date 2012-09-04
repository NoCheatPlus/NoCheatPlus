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
		protected int minCap = 4;
		public boolean isEnd = false;
		public Map<K, Node<K>> children = null;
		
		public Node(){
			
		}
		
		public Node<K> getChild(final K key, final NodeFactory<K, Node<K>> factory){
			if (children == null){
				if (factory != null) children = new HashMap<K, Node<K>>(minCap);
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
	
	/** If nodes visit method shall be called. */
	protected boolean visit;
	
	public PrefixTree(NodeFactory<K, N> nodeFactory, LookupEntryFactory<K, N, L> resultFactory){
		this.nodeFactory = nodeFactory;
		this.root = nodeFactory.newNode(null);
		this.resultFactory = resultFactory;
	}
	
	/**
	 * Look up without creating new nodes.
	 * @param keys
	 * @return
	 */
	public L lookup(final K[] keys){
		return lookup( keys, false);
	}
	
	/**
	 * Look up without creating new nodes.
	 * @param keys
	 * @return
	 */
	public L lookup(final List<K> keys){
		return lookup( keys, false);
	}
	
	/**
	 * Look up sequence, if desired fill in the given sequence.
	 * @param keys
	 * @param create
	 * @return
	 */
	public L lookup(K[] keys, final boolean create){
		return lookup(Arrays.asList(keys), create);
	}
	
	/**
	 * Look up sequence, if desired fill in the given sequence.
	 * @param keys
	 * @param create
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public L lookup(final List<K> keys, final boolean create){
		final boolean visit = this.visit;
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
				if (visit) visit(child);
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
	
	/**
	 * Visit a node during lookup. Override to make use, you need to det the visit flag for it to take effect.
	 * @param node
	 */
	protected void visit(final N node){
	}
	
	/**
	 * 
	 * @param keys
	 * @return If already inside (not necessarily as former end point).
	 */
	public boolean feed(final List<K> keys){
		final L result = lookup(keys, true);
		return result.insertion == result.node;
	}
	
	/**
	 * 
	 * @param chars
	 * @return If already inside (not necessarily as former end point).
	 */
	public boolean feed(final K[] keys){
		return feed(Arrays.asList(keys));
	}
	
	/**
	 * Check if the tree has a prefix of keys. This does not mean a common prefix, but that the tree contains an end point that is a prefix of the input. 
	 * @param keys
	 * @return
	 */
	public boolean hasPrefix(final List<K> keys){
		return lookup(keys, false).hasPrefix;
	}
	
	/**
	 * Check if the tree has a prefix of keys. This does not mean a common prefix, but that the tree contains an end point that is a prefix of the input. 
	 * @param keys
	 * @return
	 */
	public boolean hasPrefix(final K[] keys){
		return hasPrefix(Arrays.asList(keys));
	}
	
	/**
	 * Check if the input is prefix of a path inside of the tree, need not be an end point.
	 * @param keys
	 * @return
	 */
	public boolean isPrefix(final List<K> keys){
		return lookup(keys, false).depth == keys.size();
	}
	
	/**
	 * Check if the input is prefix of a path inside of the tree, need not be an end point.
	 * @param keys
	 * @return
	 */
	public boolean isPrefix(final K[] keys){
		return isPrefix(Arrays.asList(keys));
	}
	
	/**
	 * Check if the input is an inserted sequence (end point), but not necessarily a leaf.
	 * @param keys
	 * @return
	 */
	public boolean matches(final List<K> keys){
		final L result = lookup(keys, false);
		return result.node == result.insertion && result.insertion.isEnd;
	}
	
	/**
	 * Check if the input is an inserted sequence (end point), but not necessarily a leaf.
	 * @param keys
	 * @return
	 */
	public boolean matches(final K[] keys){
		return matches(Arrays.asList(keys));
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
