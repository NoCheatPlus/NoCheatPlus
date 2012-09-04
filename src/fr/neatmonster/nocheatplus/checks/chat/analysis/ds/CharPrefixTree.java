package fr.neatmonster.nocheatplus.checks.chat.analysis.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.CharPrefixTree.CharLookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.CharPrefixTree.CharNode;

public class CharPrefixTree<N extends CharNode, L extends CharLookupEntry<N>> extends PrefixTree<Character, N, L>{
	
	public static class CharNode extends Node<Character>{
	}
	
	public static class CharLookupEntry<N extends CharNode> extends LookupEntry<Character, N>{
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
	public static CharPrefixTree<CharNode, CharLookupEntry<CharNode>> newCharPrefixTree(){
		return new CharPrefixTree<CharNode, CharLookupEntry<CharNode>>(new NodeFactory<Character, CharNode>(){
			@Override
			public final CharNode newNode(final CharNode parent) {
				return new CharNode();
			}
		}, new LookupEntryFactory<Character, CharNode, CharLookupEntry<CharNode>>() {
			@Override
			public final CharLookupEntry<CharNode> newLookupEntry(final CharNode node, final CharNode insertion, final int depth, final boolean hasPrefix) {
				return new CharLookupEntry<CharNode>(node, insertion, depth, hasPrefix);
			}
		});
	}
}
