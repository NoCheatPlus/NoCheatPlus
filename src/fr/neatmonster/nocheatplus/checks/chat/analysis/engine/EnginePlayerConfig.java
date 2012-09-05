package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;

/**
 * Per player config for the engine.
 * @author mc_dev
 *
 */
public class EnginePlayerConfig {
	
	public final boolean ppComprWordsCheck;
	public final boolean ppWordFrequencyCheck;
	
	public EnginePlayerConfig(final ConfigFile config){
		ppWordFrequencyCheck = config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_ENGINE_PPWORDFREQ_CHECK, false);
		ppComprWordsCheck = config.getBoolean(ConfPaths.CHAT_GLOBALCHAT_ENGINE_PPCOMPRWORDS_CHECK, false);
	}
}
