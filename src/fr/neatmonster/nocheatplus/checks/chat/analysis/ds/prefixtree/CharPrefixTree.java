package fr.neatmonster.nocheatplus.checks.chat.analysis.ds.prefixtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.prefixtree.CharPrefixTree.CharLookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.prefixtree.CharPrefixTree.CharNode;

public class CharPrefixTree<N extends CharNode<N>, L extends CharLookupEntry<N>> extends PrefixTree<Character, N, L>{
	
	public static class CharNode<N extends CharNode<N>> extends Node<Character, N>{
	}
	
	public static class SimpleCharNode extends CharNode<SimpleCharNode>{
	}
	
	public static class CharLookupEntry<N extends CharNode<N>> extends LookupEntry<Character, N>{
		public CharLookupEntry(N node, N insertion, int depth, boolean hasPrefix){
			super(node, insertion, depth, hasPrefix);
		}
	}
	
	public CharPrefixTree(final NodeFactory<Character, N> nodeFactory, final LookupEntryFactory<Character, N, L> resultFactory) {
		super(nodeFactory, resultFactory);
	}

	/**
	 * Auxiliary method to get a List of Character.
	 * @param chars
	 * @return
	 */
	public static final List<Character> toCharacterList(final char[] chars){
		final List<Character> characters = new ArrayList<Character>(chars.length);
		for (int i = 0; i < chars.length; i++){
			characters.add(chars[i]);
		}
		return characters;
	}
	
	/**
	 * 
	 * @param chars
	 * @param create
	 * @return
	 */
	public L lookup(final char[] chars, final boolean create){
		return lookup(toCharacterList(chars), create);
	}
	
	/**
	 * 
	 * @param chars
	 * @param create
	 * @return
	 */
	public L lookup(final String input, final boolean create){
		return lookup(input.toCharArray(), create);
	}
	
	/**
	 * 
	 * @param chars
	 * @return If already inside (not necessarily as former end point).
	 */
	public boolean feed(final String input){
		return feed(input.toCharArray());
	}

	/**
	 * 
	 * @param chars
	 * @return If already inside (not necessarily as former end point).
	 */
	public boolean feed(final char[] chars){
		return feed(toCharacterList(chars));
	}
	
	public void feedAll(final Collection<String> inputs, final boolean trim, final boolean lowerCase){
		for (String input : inputs){
			if (trim) input = input.toLowerCase();
			if (lowerCase) input = input.toLowerCase();
			feed(input);
		}
	}
	
	public boolean hasPrefix(final char[] chars){
		return hasPrefix(toCharacterList(chars));
	}
	
	public boolean hasPrefix(final String input){
		return hasPrefix(input.toCharArray());
	}
	
	public boolean isPrefix(final char[] chars){
		return isPrefix(toCharacterList(chars));
	}
	
	public boolean isPrefix(final String input){
		return isPrefix(input.toCharArray());
	}
	
	public boolean matches(final char[] chars){
		return matches(toCharacterList(chars));
	}
	
	public boolean matches(final String input){
		return matches(input.toCharArray());
	}
	
	/**
	 * Factory method for a simple tree.
	 * @param keyType
	 * @return
	 */
	public static CharPrefixTree<SimpleCharNode, CharLookupEntry<SimpleCharNode>> newCharPrefixTree(){
		return new CharPrefixTree<SimpleCharNode, CharLookupEntry<SimpleCharNode>>(new NodeFactory<Character, SimpleCharNode>(){
			@Override
			public final SimpleCharNode newNode(final SimpleCharNode parent) {
				return new SimpleCharNode();
			}
		}, new LookupEntryFactory<Character, SimpleCharNode, CharLookupEntry<SimpleCharNode>>() {
			@Override
			public final CharLookupEntry<SimpleCharNode> newLookupEntry(final SimpleCharNode node, final SimpleCharNode insertion, final int depth, final boolean hasPrefix) {
				return new CharLookupEntry<SimpleCharNode>(node, insertion, depth, hasPrefix);
			}
		});
	}
}
