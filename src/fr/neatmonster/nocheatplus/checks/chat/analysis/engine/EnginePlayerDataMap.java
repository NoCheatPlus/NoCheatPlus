package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.ManagedMap;

/**
 * Store EnginePlayerData. Expire data on get(String, Chatonfig).
 * @author mc_dev
 *
 */
public class EnginePlayerDataMap extends ManagedMap<String, EnginePlayerData> {

	protected long durExpire;
	
	protected long lastAccess = System.currentTimeMillis();

	public EnginePlayerDataMap(long durExpire, int defaultCapacity, float loadFactor) {
		super(defaultCapacity, loadFactor);
		this.durExpire = durExpire;
	}

	/**
	 * 
	 * @param key
	 * @param cc
	 * @return
	 */
	public EnginePlayerData get(final String key, final ChatConfig cc) {
		EnginePlayerData data = super.get(key);
		if (data == null){
			data = new EnginePlayerData(cc);
			put(key, data);
		}
		final long ts = System.currentTimeMillis();
		if (ts - durExpire > lastAccess) expire(ts - durExpire);
		lastAccess = ts;
		return data;
	}
	
	public void clear(){
		for (ValueWrap wrap : map.values()){
			for (final WordProcessor processor : wrap.value.processors){
				processor.clear();
			}
		}
		super.clear();
	}

}
