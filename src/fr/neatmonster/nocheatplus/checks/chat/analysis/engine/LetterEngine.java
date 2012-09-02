package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;


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
		processors.add(new CompressedChars(2000, false));
	}
	
	public float feed(final MessageLetterCount letterCount){
		float score = 0;
		// Run all processors.
		for (final WordProcessor processor : processors){
			final float refScore = processor.process(letterCount);
			
//			System.out.println(processor.getProcessorName() +": " + refScore);
			
			// TODO: max or sum or average or flexible (?)...
			score = Math.max(score, refScore);
		}
		return score;
	}
}
