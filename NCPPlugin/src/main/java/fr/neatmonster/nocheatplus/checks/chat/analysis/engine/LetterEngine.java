package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.ChatData;
import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.FlatWords;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.FlatWords.FlatWordsSettings;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.SimilarWordsBKL;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.SimilarWordsBKL.SimilarWordsBKLSettings;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordPrefixes;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordPrefixes.WordPrefixesSettings;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordProcessor;
import fr.neatmonster.nocheatplus.components.IData;
import fr.neatmonster.nocheatplus.components.IHaveCheckType;
import fr.neatmonster.nocheatplus.components.IRemoveData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.utilities.LogUtil;


/**
 * Process words.
 * @author mc_dev
 *
 */
public class LetterEngine implements IRemoveData, IHaveCheckType{
	
	/** Global processors */
	protected final List<WordProcessor> processors = new ArrayList<WordProcessor>();
	
	/**
	 * Mapping players to data.
	 */
	protected final EnginePlayerDataMap dataMap;
	
	public LetterEngine(ConfigFile config){
		// Add word processors.
		// NOTE: These settings should be compared to the per player settings done in the EnginePlayerConfig constructor.
		if (config.getBoolean(ConfPaths.CHAT_TEXT_GL_WORDS_CHECK, false)){
			FlatWordsSettings settings = new FlatWordsSettings();
			settings.maxSize = 1000;
			settings.applyConfig(config, ConfPaths.CHAT_TEXT_GL_WORDS);
			processors.add(new FlatWords("glWords",settings));
		}
		if (config.getBoolean(ConfPaths.CHAT_TEXT_GL_PREFIXES_CHECK , false)){
			WordPrefixesSettings settings = new WordPrefixesSettings();
			settings.maxAdd = 2000;
			settings.applyConfig(config, ConfPaths.CHAT_TEXT_GL_PREFIXES);
			processors.add(new WordPrefixes("glPrefixes", settings));
		}
		if (config.getBoolean(ConfPaths.CHAT_TEXT_GL_SIMILARITY_CHECK , false)){
			SimilarWordsBKLSettings settings = new SimilarWordsBKLSettings();
			settings.maxSize = 1000;
			settings.applyConfig(config, ConfPaths.CHAT_TEXT_GL_SIMILARITY);
			processors.add(new SimilarWordsBKL("glSimilarity", settings));
		}
		// TODO: At least expiration duration configurable? (Entries expire after 10 minutes.)
		dataMap = new EnginePlayerDataMap(600000L, 100, 0.75f);
	}
	
	public Map<String, Float> process(final MessageLetterCount letterCount, final String playerName, final ChatConfig cc, final ChatData data){
		
		final Map<String, Float> result = new HashMap<String, Float>();
		
		// Global processors.
		if (cc.textGlobalCheck){
			for (final WordProcessor processor : processors){
				try{
					result.put(processor.getProcessorName(), processor.process(letterCount) * cc.textGlobalWeight);
				}
				catch( final Exception e){
					LogUtil.logSevere("[NoCheatPlus] chat.text: processor("+processor.getProcessorName()+") generated an exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
					LogUtil.logSevere(e);
					continue;
				}
			}
		}
		
		// Per player processors.
		if (cc.textPlayerCheck){
			final EnginePlayerData engineData = dataMap.get(playerName, cc); 
			for (final WordProcessor processor : engineData.processors){
				try{
					result.put(processor.getProcessorName(), processor.process(letterCount) * cc.textPlayerWeight);
				}
				catch( final Exception e){
					LogUtil.logSevere("[NoCheatPlus] chat.text: processor("+processor.getProcessorName()+") generated an exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
					LogUtil.logSevere(e);
					continue;
				}
			}
		}
		
		return result;
	}

	public void clear() {
		for (WordProcessor processor : processors){
			processor.clear();
		}
		processors.clear();
		dataMap.clear();
	}

	@Override
	public IData removeData(final String playerName) {
		return dataMap.remove(playerName);
	}

	@Override
	public void removeAllData() {
		dataMap.clear();
	}

	@Override
	public final CheckType getCheckType() {
		return CheckType.CHAT_TEXT;
	}
}
