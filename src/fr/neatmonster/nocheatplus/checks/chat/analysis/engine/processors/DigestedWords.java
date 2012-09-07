package fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.analysis.WordLetterCount;
import fr.neatmonster.nocheatplus.config.ConfigFile;

public abstract class DigestedWords extends AbstractWordProcessor{
	
	/**
	 * Doubling code a little for the sake of flexibility with config reading.
	 * @author mc_dev
	 *
	 */
	public static class DigestedWordsSettings{
		public boolean sort = false;
		public boolean compress = false;
		public boolean split = false;
		public float weight = 1f;
		
		public DigestedWordsSettings(){	
		}
		
		public DigestedWordsSettings(boolean sort, boolean compress, boolean split, float weight) {
			this.sort = sort;
			this.compress = compress;
			this.split = split;
			this.weight  = weight;
		}
		
		/**
		 * Returns this object.
		 * @param config
		 * @param prefix Prefix for direct addition of config path.
		 * @return This object.
		 */
		public DigestedWordsSettings applyConfig(ConfigFile config, String prefix){
			this.sort = config.getBoolean(prefix + "sort", this.sort);
			this.compress = config.getBoolean(prefix + "compress", this.compress);
			this.split = config.getBoolean(prefix + "split", this.split);
			this.weight = (float) config.getDouble(prefix + "weight", this.weight);
			return this;
		}
	}

	protected final boolean sort;
	protected final boolean compress;
	protected final boolean split;
	
	protected final List<Character> letters = new ArrayList<Character>(10);
	protected final List<Character> digits = new ArrayList<Character>(10);
	protected final List<Character> other = new ArrayList<Character>(10);
	
	/**
	 * Constructor for a given settings instance.
	 * @param name
	 * @param settings
	 */
	public DigestedWords(String name, DigestedWordsSettings settings){
		this(name, settings.sort, settings.compress, settings.split);
		this.weight = settings.weight;
	}
	
	/**
	 * 
	 * @param durExpire
	 * @param maxAdd
	 * @param sort Sort letters.
	 * @param compress Only use every letter once.
	 * @param split Check for letters, digits, other individually (!).
	 */
	public DigestedWords(String name, boolean sort, boolean compress, boolean split) {
		super(name);
		this.sort = sort;
		this.compress = compress;
		this.split = split;
	}

	@Override
	public float loop(final long ts, final int index, final String key, final WordLetterCount word) {
		letters.clear();
		digits.clear();
		other.clear();
		Collection<Character> chars;
		if (compress) chars = word.counts.keySet();
		else{
			// Add all.
			chars = new ArrayList<Character>(word.word.length());
			for (int i = 0; i < word.word.length(); i++){
				char c = word.word.charAt(i);
				if (Character.isUpperCase(c)) c = Character.toLowerCase(c);
				chars.add(c); 
				// hmm. Maybe provide all the lists in the WordLetterCount already.
			}
		}
		final int len = chars.size();
		for (Character c : chars){
			if (!split || Character.isLetter(c)) letters.add(c);
			else if (Character.isDigit(c)) digits.add(c);
			else other.add(c);
		}
		if (sort){
			Collections.sort(letters);
			Collections.sort(digits);
			Collections.sort(other);
		}
		float score = 0;
		if (!letters.isEmpty()){
			score += getScore(letters, ts) * (float) letters.size();
		}
		if (!digits.isEmpty()){
			score += getScore(digits, ts) * (float) digits.size();
		}
		if (!other.isEmpty()){
			score += getScore(other, ts) * (float) other.size();
		}
	return len == 0?0f:(score / (float) len);
	}
	
	@Override
	public void clear() {
		letters.clear();
		digits.clear();
		other.clear();
	}
	
	public static final char[] toArray(final Collection<Character> chars){
		final char[] a = new char[chars.size()];
		int i = 0;
		for (final Character c : chars){
			a[i] = c;
			i ++;
			// TODO: lol, horrible.
		}
		return a;
	}

	/**
	 * 
	 * @param chars List of characters, should be lower case, could be split / compressed.
	 * @param ts common timestamp for processing the whole message.
	 * @return
	 */
	protected abstract float getScore(final List<Character> chars, final long ts);

}
