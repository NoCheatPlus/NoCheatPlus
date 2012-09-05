package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.ChatData;
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
	
	protected final EnginePlayerDataMap dataMap;
	
	public LetterEngine(ConfigFile config){
		// Add word processors.
		if (config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_ENGINE_GLWORDFREQ_CHECK, false)){
			// TODO: Make aspects configurable.
			processors.add(new FlatWordBuckets(1000, 4, 1500, 0.9f));
		}
		if (config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_ENGINE_GLCOMPRWORDS_CHECK, false)){
			// TODO: Make aspects configurable.
			processors.add(new CompressedWords(30000, 2000, false));
		}
		
		// TODO: At least expiration duration configurable?
		dataMap = new EnginePlayerDataMap(600000L, 100, 0.75f);
	}
	
	public float process(final MessageLetterCount letterCount, final String playerName, final ChatConfig cc, final ChatData data){
		float score = 0;
		
		// Global processors.
		for (final WordProcessor processor : processors){
			final float refScore = processor.process(letterCount);
			
//			System.out.println("global:" + processor.getProcessorName() +": " + refScore);
			
			score = Math.max(score, refScore);
		}
		
		// Per player processors.
		final EnginePlayerData engineData = dataMap.get(playerName, cc); 
		for (final WordProcessor processor : engineData.processors){
			final float refScore = processor.process(letterCount);
			
//			System.out.println("player: " + processor.getProcessorName() +": " + refScore);
			
			score = Math.max(score, refScore);
		}
		
		// TODO: Is max the right method?
		
		return score;
	}

	public void clear() {
		for (WordProcessor processor : processors){
			processor.clear();
		}
		processors.clear();
		dataMap.clear();
	}
}
