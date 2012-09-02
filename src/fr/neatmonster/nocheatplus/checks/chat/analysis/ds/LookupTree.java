package fr.neatmonster.nocheatplus.checks.chat.analysis.ds;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.LookupTree.Node;


/**
 * Tree for some sequence lookup. <br>
 * Pretty fat, for evaluation purposes.
 * @author mc_dev
 *
 */
public class LookupTree<K, N extends Node<K>>{
	
	public static class Node<K>{
		public boolean isEnd = false;
		public Map<K, Node<K>> children = null;
		
		public Node(){
			
		}
		
		public Node<K> getChild(final K key, final NodeFactory<K, Node<K>> factory){
			if (children == null){
				if (factory != null) children = new HashMap<K, Node<K>>();
				else return null;
			}
			Node<K> node = children.get(key);
			if (node != null) return node;
			else if (factory == null) return null;
			else{
				node = factory.getNewNode();
				children.put(key, node);
				return node;
			}
		}
	}
	
	public static interface NodeFactory<K, N extends Node<K>>{
		public N getNewNode();
	}
	
	public static class LookupEntry<K, N extends Node<K>>{
		/** The node, if lookup matched.*/
		public final N node;
		/** The node at which insertion did/would happen */
		public final N insertion;
		/** Depth to root from insertion point. */
		public final int depth;
		public LookupEntry(N node , N insertion, int depth){
			this.node = node;
			this.insertion = insertion;
			this.depth = depth;
		}
	}
	
	protected final NodeFactory<K, N> factory;
	
	protected N root;
	
	public LookupTree(final Class<N> clazz){
		this(new NodeFactory<K, N>() {
			@Override
			public N getNewNode() {
				try {
					return clazz.newInstance();
				} catch (InstantiationException e) {
					return null;
				} catch (IllegalAccessException e) {
					return null;
				}
			}
		});
	}
	
	public LookupTree(NodeFactory<K, N> factory){
		this.factory = factory;
		this.root = factory.getNewNode();
	}
	
	public LookupEntry<K, N> lookup(K[] keys, final boolean create){
		return lookup(Arrays.asList(keys), create);
	}
	
	@SuppressWarnings("unchecked")
	public LookupEntry<K, N> lookup(final List<K> keys, final boolean create){
		N insertion = root;
		N leaf = null;
		int depth = 0;
		N current = root;
		final NodeFactory<K, N> factory = (NodeFactory<K, N>) (create ? this.factory : null);
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
				// Node already exists, set as insertion point.
				insertion = current = child;
				depth ++;
			}
		}
		if (create || insertion.isEnd && depth == keys.size()){
			leaf = current;
			if (insertion != current) current.isEnd = true;
		}
		return new LookupEntry<K, N>(leaf, insertion, depth);
	}

	public void clear() {
		root = factory.getNewNode();
		// TODO: maybe more unlinking ?
	}
}
