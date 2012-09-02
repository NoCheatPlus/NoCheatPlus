package fr.neatmonster.nocheatplus.checks.chat.analysis;

import java.util.LinkedHashMap;

import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

/**
 * Words mapped to ActionFrequency queues. 
 * @author mc_dev
 *
 */
public class FlatWordBuckets extends AbstractWordProcessor{
	final int maxSize;
	final LinkedHashMap<String, ActionFrequency> entries;
	final long durBucket;
	final int nBuckets;
	final float factor;
	public FlatWordBuckets(int maxSize, int nBuckets, long durBucket, float factor){
		super("FlatWordBuckets");
		this.maxSize = maxSize;
		entries = new LinkedHashMap<String, ActionFrequency>(maxSize);
		this.nBuckets = nBuckets;
		this.durBucket = durBucket;
		this.factor = factor;
	}
	
	@Override
	public void start(MessageLetterCount message) {
		if (entries.size() + message.words.length > maxSize)
			releaseMap(entries, maxSize / 10);
	}
	
	@Override
	public float loop(long ts, int index, String key,
			WordLetterCount message) {
		ActionFrequency freq = entries.get(key);
		if (freq == null){
			freq = new ActionFrequency(nBuckets, durBucket);
			entries.put(key, freq);
			return 0.0f;
		}
		freq.update(ts);
		float score = Math.min(1.0f, freq.getScore(factor));
		freq.add(ts, 1.0f);
		return score;
	}
}
