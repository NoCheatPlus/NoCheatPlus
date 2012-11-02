package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.FlatWords;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.SimilarWordsBKL;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordPrefixes;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordProcessor;
import fr.neatmonster.nocheatplus.components.IData;

/**
 * Engine specific player data.
 * @author mc_dev
 *
 */
public class EnginePlayerData implements IData{
	
	public final List<WordProcessor> processors = new ArrayList<WordProcessor>(5);

	public EnginePlayerData(ChatConfig cc) {
		EnginePlayerConfig config = cc.textEnginePlayerConfig;
		if (config.ppWordsCheck) 
			processors.add(new FlatWords("ppWords", config.ppWordsSettings));
		if (config.ppPrefixesCheck) 
			processors.add(new WordPrefixes("ppPrefixes", config.ppPrefixesSettings));
		if (config.ppSimilarityCheck)
			processors.add(new SimilarWordsBKL("ppSimilarity", config.ppSimilaritySettings));
	}
	
}
