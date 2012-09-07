package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.FlatWords.FlatWordsSettings;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.SimilarWordsBKL.SimilarWordsBKLSettings;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordPrefixes.WordPrefixesSettings;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;

/**
 * Per player config for the engine.
 * @author mc_dev
 *
 */
public class EnginePlayerConfig {
	
	public final boolean ppPrefixesCheck;
	public final WordPrefixesSettings ppPrefixesSettings;
	public final boolean ppWordsCheck;
	public final FlatWordsSettings ppWordsSettings;
	public final boolean ppSimilarityCheck;
	public final SimilarWordsBKLSettings ppSimilaritySettings;
	
	public EnginePlayerConfig(final ConfigFile config){
		// NOTE: These settings should be compared to the global settings done in the LetterEngine constructor.
		ppWordsCheck = config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_PP_WORDS_CHECK, false);
		if (ppWordsCheck){
			ppWordsSettings = new FlatWordsSettings();
			ppWordsSettings.maxSize = 150; // Adapt to smaller size.
			ppWordsSettings.applyConfig(config, ConfPaths.CHAT_GLOBALCHAT_PP_WORDS);
		}
		else ppWordsSettings = null; // spare some memory.
		ppPrefixesCheck = config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_PP_PREFIXES_CHECK, false);
		if (ppPrefixesCheck){
			ppPrefixesSettings = new WordPrefixesSettings();
			ppPrefixesSettings.maxAdd = 320; // Adapt to smaller size.
			ppPrefixesSettings.applyConfig(config, ConfPaths.CHAT_GLOBALCHAT_PP_PREFIXES);
		}
		else ppPrefixesSettings = null;
		ppSimilarityCheck = config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_PP_SIMILARITY_CHECK, false);
		if (ppSimilarityCheck){
			ppSimilaritySettings = new SimilarWordsBKLSettings();
			ppSimilaritySettings.maxSize = 100; // Adapt to smaller size;
			ppSimilaritySettings.applyConfig(config, ConfPaths.CHAT_GLOBALCHAT_PP_SIMILARITY);
		}
		else ppSimilaritySettings = null;
	}
}
