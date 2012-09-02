package fr.neatmonster.nocheatplus.checks.chat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Letter count for a message and sub words, including upper case count.<br>
 * NOTE: this is a pretty heavy implementation for testing purposes.
 * @author mc_dev
 *
 */
public class MessageLetterCount {
	/**
	 * Letter count for a word.
	 * @author mc_dev
	 *
	 */
	public static final class WordLetterCount{
		public final String word;
		public final Map<Character, Integer> counts;
		public final int upperCase;
		public final int notLetter;
		public WordLetterCount(final String word){
			this.word = word;
			char[] a = word.toCharArray();
			// Preserve insertion order.
			counts = new LinkedHashMap<Character, Integer>(a.length);
			int upperCase = 0;
			int notLetter = 0;
			for (int i = 0; i < a.length; i++){
				final char c = a[i];
				final Character key;
				if (!Character.isLetter(c)) notLetter ++;
				if (Character.isUpperCase(c)){
					upperCase ++;
					key = Character.toLowerCase(c);
				}
				else key = c;
				final Integer count = counts.remove(key);
				if (count == null) counts.put(key,  1);
				else counts.put(key, count.intValue() + 1);
				
			}
			this.notLetter = notLetter;
			this.upperCase = upperCase;
		}
		
		public float getNotLetterRatio(){
			return (float) notLetter / (float) word.length();
		}
		
		public float getLetterCountRatio(){
			return (float) counts.size() / (float) word.length();
		}
		
		public float getUpperCaseRatio(){
			return (float) upperCase / (float) word.length();
		}
	}
	
	public final String message;
	
	public final String split;
	
	public final WordLetterCount[] words;
	
	public final WordLetterCount fullCount;
	
	/**
	 * Constructor for splitting by a space. 
	 * @param message
	 */
	public MessageLetterCount(final String message){
		this(message, " ");
	}
	
	/**
	 * 
	 * @param message
	 * @param split
	 */
	public MessageLetterCount(final String message, final String split){
		this.message = message;
		this.split = split;
		
		final String[] parts = message.split(split);
		words = new WordLetterCount[parts.length];
		
		fullCount = new WordLetterCount(message);
		// (Do not store 60 times "a".)
		final Map<String, WordLetterCount> done = new HashMap<String, MessageLetterCount.WordLetterCount>(words.length);
		for (int i = 0; i < parts.length; i++){
			final String word = parts[i];
			if (done.containsKey(word)){
				words[i] = done.get(word);
				continue;
			}
			done.put(word, words[i] = new WordLetterCount(word));
		}
		done.clear();
	}

}
