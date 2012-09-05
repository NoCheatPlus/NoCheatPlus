package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;

/**
 * Engine specific player data.
 * @author mc_dev
 *
 */
public class EnginePlayerData {
	
	public final List<WordProcessor> processors = new ArrayList<WordProcessor>(5);

	public EnginePlayerData(ChatConfig cc) {
		EnginePlayerConfig config = cc.globalChatEnginePlayerConfig;
		if (config.ppWordFrequencyCheck){
			// TODO: configure.
			processors.add(new FlatWordBuckets(50, 4, 1500, 0.9f));
		}
		if (config.ppComprWordsCheck){
			// TODO: configure.
			processors.add(new CompressedWords(30000, 320, false));
		}
		
	}

}
