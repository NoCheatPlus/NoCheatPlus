package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;


/**
 * Process words.
 * @author mc_dev
 *
 */
public class LetterEngine {
	
	protected final List<WordProcessor> processors = new ArrayList<WordProcessor>();
	
	public LetterEngine(ConfigFile config){
		// Add word processors.
		if (config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_ENGINE_GLWORDFREQ_CHECK, true)){
			// TODO: Make aspects configurable.
			processors.add(new FlatWordBuckets(1000, 4, 1500, 0.9f));
		}
		if (config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_ENGINE_GLCOMPRWORDS_CHECK, false)){
			// TODO: Make aspects configurable.
			processors.add(new CompressedWords(30000, 2000, false));
		}
	}
	
	public float process(final MessageLetterCount letterCount){
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
