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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.PrefixTree.LookupEntry;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.PrefixTree.Node;


/**
 * Tree for some sequence lookup. <br>
 * Pretty fat, for evaluation purposes.
 * @author mc_dev
 *
 */
public class PrefixTree<K, N extends Node<K, N>, L extends LookupEntry<K, N>>{
	
	/**
	 * The real thing.
	 * @author mc_dev
	 *
	 * @param <K>
	 * @param <N>
	 */
	public static class Node<K, N extends Node<K,N>>{
		protected int minCap = 4;
		/** End of a sequence marker (not necessarily a leaf) */
		public boolean isEnd = false;
		public Map<K, N> children = null;
		
		public Node(){
		}
		
		public N getChild(final K key){
			if (children == null) return null;
			return children.get(key);
		}
		
		/**
		 * Put the child into the children map.
		 * @param key
		 * @param child
		 * @return The resulting child for the key.
		 */
		public N putChild(final K key, final N child){
			if (children == null) children = new HashMap<K, N>(minCap);
			children.put(key, child);
			return child;
		}
	}
	
	/**
	 * Convenience.
	 * @author mc_dev
	 *
	 * @param <K>
	 */
	public static class SimpleNode<K> extends Node<K, SimpleNode<K>>{
	}
	
	public static interface NodeFactory<K, N extends Node<K, N>>{
		/**
		 * 
		 * @param parent Can be null (root).
		 * @return
		 */
		public N newNode(N parent);
	}
	
	public static class LookupEntry<K, N extends Node<K, N>>{
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
	
	public static interface LookupEntryFactory<K, N extends Node<K, N>, L extends LookupEntry<K, N>>{
		public L newLookupEntry(N node , N insertion, int depth, boolean hasPrefix); 
	}
	
	protected final NodeFactory<K, N> nodeFactory;
	
	protected final LookupEntryFactory<K, N, L> resultFactory;
	
	protected N root;
	
	/** If nodes visit method shall be called. */
	protected boolean visit;
	
	public PrefixTree(final NodeFactory<K, N> nodeFactory, final LookupEntryFactory<K, N, L> resultFactory){
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
	public L lookup(final K[] keys, final boolean create){
		return lookup(Arrays.asList(keys), create);
	}
	
	/**
	 * Look up sequence, if desired fill in the given sequence.
	 * @param keys
	 * @param create
	 * @return
	 */
	public L lookup(final List<K> keys, final boolean create){
		final boolean visit = this.visit;
		N insertion = root;
		int depth = 0;
		N current = root;
		boolean hasPrefix = false;
		for (final K key : keys){
			final N child = current.getChild(key);
			if (child == null){
				if(create){
					final N temp = nodeFactory.newNode(current);
					current = current.putChild(key, temp);
				} 
				else break;
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
		final L result = resultFactory.newLookupEntry(node, insertion, depth, hasPrefix);
		decorate(result);
		return  result;
	}
	
	/**
	 * Visit a node during lookup. Override to make use, you need to det the visit flag for it to take effect.
	 * @param node
	 */
	protected void visit(final N node){
	}
	
	/**
	 * Decorate before returning.
	 * @param result
	 */
	protected void decorate(final L result){
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
	public static <K> PrefixTree<K, SimpleNode<K>, LookupEntry<K, SimpleNode<K>>> newPrefixTree(){
		return new PrefixTree<K, SimpleNode<K>, LookupEntry<K, SimpleNode<K>>>(new NodeFactory<K, SimpleNode<K>>(){
			@Override
			public final SimpleNode<K> newNode(final SimpleNode<K> parent) {
				return new SimpleNode<K>();
			}
		}, new LookupEntryFactory<K, SimpleNode<K>, LookupEntry<K,SimpleNode<K>>>() {
			@Override
			public final LookupEntry<K, SimpleNode<K>> newLookupEntry(final SimpleNode<K> node, final SimpleNode<K> insertion, final int depth, final boolean hasPrefix) {
				return new LookupEntry<K, SimpleNode<K>>(node, insertion, depth, hasPrefix);
			}
		});
	}
}
