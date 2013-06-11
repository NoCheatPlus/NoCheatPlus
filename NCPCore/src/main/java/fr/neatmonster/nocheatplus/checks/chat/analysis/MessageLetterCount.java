package fr.neatmonster.nocheatplus.checks.chat.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * Letter count for a message and sub words, including upper case count.<br>
 * NOTE: this is a pretty heavy implementation for testing purposes.
 * @author mc_dev
 *
 */
public class MessageLetterCount {
	
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
		final Map<String, WordLetterCount> done = new HashMap<String, WordLetterCount>(words.length);
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
