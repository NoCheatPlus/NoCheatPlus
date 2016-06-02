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
package fr.neatmonster.nocheatplus.utilities.ds.bktree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.neatmonster.nocheatplus.utilities.ds.bktree.BKModTree.LookupEntry;
import fr.neatmonster.nocheatplus.utilities.ds.bktree.BKModTree.Node;

/**
 * BK tree for int distances.
 * @author mc_dev
 *
 */
public abstract class BKModTree<V, N extends Node<V, N>, L extends LookupEntry<V, N>>{
	
	// TODO: Support for other value (equals) than used for lookup (distance).
	// TODO: What with dist = 0 -> support for exact hit !
	
	/**
	 * Fat defaultimpl. it iterates over all Children
	 * @author mc_dev
	 *
	 * @param <V>
	 * @param <N>
	 */
	public static abstract class Node<V, N extends Node<V, N>>{
		public V value;
		
		public Node(V value){
			this.value = value;
		}
		public abstract N putChild(final int distance, final N child);
		
		public abstract N getChild(final int distance);
		
		public abstract boolean hasChild(int distance);
		
		public abstract Collection<N> getChildren(final int distance, final int range, final Collection<N> nodes);
	}
	
	/**
	 * Node using a map as base, with basic implementation.
	 * @author mc_dev
	 *
	 * @param <V>
	 * @param <N>
	 */
	public static abstract class MapNode<V, N extends HashMapNode<V, N>> extends Node<V, N>{
		protected Map<Integer, N> children = null; // Only created if needed.
		protected int maxIterate = 12; // Maybe add a setter.
		public MapNode(V value) {
			super(value);
		}
		@Override
		public N putChild(final int distance, final N child){
			if (children == null) children = newMap();
			children.put(distance, child);
			return child;
		}
		@Override
		public N getChild(final int distance){
			if (children == null) return null;
			return children.get(distance);
		}
		@Override
		public boolean hasChild(int distance) {
			if (children == null) return false;
			return children.containsKey(distance);
		}
		@Override
		public Collection<N> getChildren(final int distance, final int range, final Collection<N> nodes){
			if (children == null) return nodes;
			// TODO: maybe just go for iterating till range (from 0 on) to have closest first (no keyset).
			if (children.size() > maxIterate){
				for (int i = distance - range; i < distance + range + 1; i ++){
					final N child = children.get(i);
					if (child != null) nodes.add(child);
				}
			}
			else{
				for (final Integer key : children.keySet()){
					// TODO: Not sure this is faster than the EntrySet in average.
					if (Math.abs(distance - key.intValue()) <= range) nodes.add(children.get(key));
				}
			}
			return nodes;
		}
		/**
		 * Map factory method.
		 * @return
		 */
		protected abstract Map<Integer, N> newMap();
	}
	
	/**
	 * Node using a simple HashMap.
	 * @author mc_dev
	 *
	 * @param <V>
	 * @param <N>
	 */
	public static class HashMapNode<V, N extends HashMapNode<V, N>> extends MapNode<V, N>{
		/** Map Levenshtein distance to next nodes. */
		protected int initialCapacity = 4;
		protected float loadFactor = 0.75f;
		public HashMapNode(V value) {
			super(value);
		}

		@Override
		protected Map<Integer, N> newMap() {
			return new HashMap<Integer, N>(initialCapacity, loadFactor);
		}
	}
	
	public static class SimpleNode<V> extends HashMapNode<V, SimpleNode<V>>{
		public SimpleNode(V content) {
			super(content);
		}
	}
	
	public static interface NodeFactory<V, N extends Node<V, N>>{
		public N newNode(V value, N parent);
	}
	
	/**
	 * Result of a lookup.
	 * @author mc_dev
	 *
	 * @param <V>
	 * @param <N>
	 */
	public static class LookupEntry<V, N extends Node<V, N>>{
		// TODO: What nodes are in nodes, actually? Those from the way that were in range ?
		// TODO: This way one does not know which distance a node has. [subject to changes]
		// TODO: Add depth and some useful info ?
		
		/** All visited nodes within range of distance. */
		public final Collection<N> nodes;
		/** Matching node */
		public final N match;
		/** Distance from value to match.value */
		public final int distance;
		/** If the node match is newly inserted.*/
		public final boolean isNew;
		
		public LookupEntry(Collection<N> nodes, N match, int distance, boolean isNew){
			this.nodes = nodes;
			this.match = match;
			this.distance = distance;
			this.isNew = isNew;
		}
	}
	
	public static interface LookupEntryFactory<V, N extends Node<V, N>, L extends LookupEntry<V, N>>{
		public L newLookupEntry(Collection<N> nodes, N match, int distance, boolean isNew);
	}

	protected final NodeFactory<V, N> nodeFactory;
	
	protected final LookupEntryFactory<V, N, L> resultFactory;
	
	protected N root = null;
	
	/** Set to true to have visit called */
	protected boolean visit = false;
	
	public BKModTree(NodeFactory<V, N> nodeFactory, LookupEntryFactory<V, N, L> resultFactory){
		this.nodeFactory = nodeFactory;
		this.resultFactory = resultFactory;
	}
	
	public void clear(){
		root = null;
	}
	
	/**
	 * 
	 * @param value
	 * @param range Maximum difference from distance of node.value to children.
	 * @param seekMax If node.value is within distance but not matching, this is the maximum number of steps to search on.
	 * @param create
	 * @return
	 */
	public L lookup(final V value, final int range, final int seekMax, final boolean create){ // TODO: signature.
		final List<N> inRange = new LinkedList<N>();
		if (root == null){
			if (create){
				root = nodeFactory.newNode(value, null);
				return resultFactory.newLookupEntry(inRange, root, 0, true);
			}
			else{
				return resultFactory.newLookupEntry(inRange, null, 0, false);
			}
		}
		// TODO: best queue type.
		final List<N> open = new ArrayList<N>();
		open.add(root);
		N insertion = null;
		int insertionDist = 0;
		do{
			final N current = open.remove(open.size() - 1);
			int distance = distance(current.value, value);
			if (visit) visit(current, value, distance);
			if (distance == 0){
				// exact match.
				return resultFactory.newLookupEntry(inRange, current, distance, false);
			}
			// Set node as insertion point.
			if (create && insertion == null && !current.hasChild(distance)){
				insertion = current;
				insertionDist = distance;
				// TODO: use
			}
			// Within range ?
			if (Math.abs(distance) <= range){
				inRange.add(current);
				// Check special abort conditions.
				if (seekMax > 0 && inRange.size() >= seekMax){
					// TODO: Keep this ?
					// Break if insertion point is found, or not needed.
					if (!create || insertion != null){
						break;
					}
				}
			}
			// Continue search with children.
			current.getChildren(distance, range, open);
			
			// TODO: deterministic: always same node visited for the same value ? [Not with children = HashMap...]
		} while (!open.isEmpty());
		
		// TODO: is the result supposed to be the closest match, if any ?
		
		if (create && insertion != null){
			final N newNode = nodeFactory.newNode(value, insertion);
			insertion.putChild(insertionDist, newNode);
			return resultFactory.newLookupEntry(inRange, newNode, 0, true);
		}
		else{
			return resultFactory.newLookupEntry(inRange, null, 0, false);
		}
	}
	
	/**
	 * Visit a node during lookup.
	 * @param node
	 * @param distance 
	 * @param value 
	 */
	protected void visit(N node, V value, int distance){
		// Override if needed.
	}
	
	//////////////////////////////////////////////
	// Abstract methods.
	//////////////////////////////////////////////
	
	/**
	 * Calculate the distance of two values.
	 * @param v1
	 * @param v2
	 * @return
	 */
	public abstract int distance(V v1, V v2);
	
}
