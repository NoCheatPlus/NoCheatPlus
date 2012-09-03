package fr.neatmonster.nocheatplus.checks.chat.analysis.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Adding some convenience methods.
 * @author mc_dev
 *
 */
public class SimpleCharPrefixTree extends SimplePrefixTree<Character>{
	
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
	public SimpleLookupEntry<Character> lookup(final char[] chars, boolean create){
		return lookup(toCharacterList(chars), create);
	}
	
	/**
	 * 
	 * @param chars
	 * @param create
	 * @return
	 */
	public SimpleLookupEntry<Character> lookup(final String input, boolean create){
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
	
	public void feedAll(final Collection<String> inputs, boolean trim, boolean lowerCase){
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
	
}
