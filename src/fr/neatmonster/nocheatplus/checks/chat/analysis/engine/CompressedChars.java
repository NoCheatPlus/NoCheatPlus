package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.WordLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.SimplePrefixTree;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.SimplePrefixTree.SimpleLookupEntry;

public class CompressedChars extends AbstractWordProcessor{

	protected final SimplePrefixTree<Character> tree = new SimplePrefixTree<Character>();
	
	protected final int maxAdd;
	
	protected int added = 0;

	private final boolean sort;
	
	public CompressedChars(int maxAdd, boolean sort) {
		super("CompressedChars");
		this.maxAdd = maxAdd;
		this.sort = sort;
	}
	
	@Override
	public void start(MessageLetterCount message) {
		// This allows adding up to maximum messge length more characters,
		//  	but also allows to set size of nodes exactly.
		// TODO: Some better method  than blunt clear (extra LinkedHashSet/LRU?).
		if (added > maxAdd) tree.clear();
		added = 0;
	}

	@Override
	public float loop(long ts, int index, String key, WordLetterCount word) {
		final List<Character> letters = new ArrayList<Character>(word.counts.size());
		final List<Character> numbers = new ArrayList<Character>(word.counts.size());
		final List<Character> other = new ArrayList<Character>(word.counts.size());
		for (Character c : word.counts.keySet()){
			if (Character.isLetter(c)) letters.add(c);
			else if (Character.isDigit(c)) numbers.add(c);
			else other.add(c);
		}
		if (sort){
			Collections.sort(letters);
			Collections.sort(numbers);
			Collections.sort(other);
		}
		float score = 0;
		float div = 0;
		if (!letters.isEmpty()){
			score += getScore(letters);
			div += 1;
		}
		if (!numbers.isEmpty()){
			score += getScore(numbers);
			div += 1;
		}
		if (!other.isEmpty()){
			score += getScore(other);
			div += 1;
		}
		return (div == 0) ? 0 : score;
	}

	private float getScore(List<Character> chars) {
		final int len = chars.size();
		SimpleLookupEntry<Character> entry = tree.lookup(chars, true);
		float score = (float) (entry.depth*entry.depth) / (float) (len*len) ;
		if (entry.depth == chars.size()){
			score += 0.2;
			if (entry.insertion.isEnd) score += 0.2;
		}
		if (len != entry.depth) added += len - entry.depth;
		return score;
	}

}
