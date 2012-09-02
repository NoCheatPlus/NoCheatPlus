package fr.neatmonster.nocheatplus.checks.chat.analysis;

import java.util.ArrayList;
import java.util.List;


/**
 * Process words.
 * @author mc_dev
 *
 */
public class LetterEngine {
	
	protected final List<WordProcessor> processors = new ArrayList<WordProcessor>();
	
	public LetterEngine(){
		// Add word processors.
		processors.add(new FlatWordBuckets(1000, 4, 1500, 0.9f));
	}
	
	public float feed(final MessageLetterCount letterCount){
		float score = 0;
		// Run all processors.
		for (final WordProcessor processor : processors){
			final float refScore = processor.process(letterCount);
			// TODO: max or sum or average or flexible (?)...
			score = Math.max(score, refScore);
		}
		return score;
	}
}
