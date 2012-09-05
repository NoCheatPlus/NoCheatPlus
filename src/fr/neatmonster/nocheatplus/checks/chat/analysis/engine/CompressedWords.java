package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.WordLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.prefixtree.SimpleTimedCharPrefixTree;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.prefixtree.SimpleTimedCharPrefixTree.SimpleTimedCharLookupEntry;

public class CompressedWords extends AbstractWordProcessor{

	protected final SimpleTimedCharPrefixTree tree = new SimpleTimedCharPrefixTree(true);
	
	protected final int maxAdd;
	
	protected int added = 0;

	protected final boolean sort;

	protected final long durExpire;
	
	protected final List<Character> letters = new ArrayList<Character>(10);
	protected final List<Character> digits = new ArrayList<Character>(10);
	protected final List<Character> other = new ArrayList<Character>(10);
	
	public CompressedWords(long durExpire, int maxAdd, boolean sort) {
		super("CompressedWords");
		this.durExpire = durExpire;
		this.maxAdd = maxAdd;
		this.sort = sort;
	}
	
	@Override
	public void start(final MessageLetterCount message) {
		// This allows adding up to maximum messge length more characters,
		//  	but also allows to set size of nodes exactly.
		// TODO: Some better method  than blunt clear (extra LinkedHashSet/LRU?).
		if (added > maxAdd) tree.clear();
		added = 0;
	}

	@Override
	public float loop(final long ts, final int index, final String key, final WordLetterCount word) {
		final int len = word.counts.size();
		letters.clear();
		digits.clear();
		other.clear();
		for (Character c : word.counts.keySet()){
			if (Character.isLetter(c)) letters.add(c);
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
			score += getScore(letters, ts);
		}
		if (!digits.isEmpty()){
			score += getScore(digits, ts);
		}
		if (!other.isEmpty()){
			score += getScore(other, ts);
		}
	return word.counts.isEmpty()?0f:(score / (float) len);
	}
	
	@Override
	public void clear() {
		tree.clear();
		letters.clear();
		digits.clear();
		other.clear();
	}

	protected float getScore(final List<Character> chars, final long ts) {
		final int len = chars.size();
		final SimpleTimedCharLookupEntry entry = tree.lookup(chars, true);
		final int depth = entry.depth;
		float score = 0f;
		for (int i = 0; i < depth ; i++){
			final long age = ts - entry.timeInsertion[i];
			if (age < durExpire)
				score += 1f / (float) (depth - i) * (float) (durExpire - age) / (float) durExpire;
		}
		if (depth == len){
			score += 0.2;
			if (entry.insertion.isEnd) score += 0.2;
		}
		if (len != depth) added += len - depth;
		return score;
	}

}
