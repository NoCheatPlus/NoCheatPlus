package fr.neatmonster.nocheatplus.checks.chat.analysis;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Letter count for a word.
 * @author mc_dev
 *
 */
public final class WordLetterCount{
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
