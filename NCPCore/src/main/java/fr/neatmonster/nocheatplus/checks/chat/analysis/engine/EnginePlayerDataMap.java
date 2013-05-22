package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import java.util.Collection;

import fr.neatmonster.nocheatplus.checks.chat.ChatConfig;
import fr.neatmonster.nocheatplus.checks.chat.analysis.engine.processors.WordProcessor;
import fr.neatmonster.nocheatplus.utilities.ds.ManagedMap;

/**
 * Store EnginePlayerData. Expire data on get(String, Chatonfig).
 * @author mc_dev
 *
 */
public class EnginePlayerDataMap extends ManagedMap<String, EnginePlayerData> {

	protected long durExpire;
	
	/** Timestamp of last time get was called.*/
	protected long lastAccess = System.currentTimeMillis();
	
	/** Timestamp of last time an entry was removed. */
	protected long lastExpired = lastAccess;

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
		if (ts < lastExpired){
			lastExpired = ts;
			// might clear map or update all entries.
		}
		else if (ts - lastExpired > durExpire) expire(ts - durExpire);
		lastAccess = ts;
		return data;
	}
	
	public void clear(){
		final long time = System.currentTimeMillis();
		for (final ValueWrap wrap : map.values()){
			for (final WordProcessor processor : wrap.value.processors){
				processor.clear();
			}
		}
		super.clear();
		lastAccess = lastExpired = time;
	}

	/* (non-Javadoc)
	 * @see fr.neatmonster.nocheatplus.utilities.ds.ManagedMap#expire(long)
	 */
	@Override
	public Collection<String> expire(long ts) {
		final Collection<String> rem = super.expire(ts);
		if (!rem.isEmpty()) lastExpired = System.currentTimeMillis();
		return rem;
	}
	
	

}
