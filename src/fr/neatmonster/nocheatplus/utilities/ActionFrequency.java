package fr.neatmonster.nocheatplus.utilities;

/**
 * Keep track of frequency of some action, 
 * put weights into buckets, which represent intervals of time. 
 * @author mc_dev
 *
 */
public class ActionFrequency {
	
	/** Reference time for filling in. */
	long time = 0;
	
	/**
	 * Buckets to fill weights in, each represents an interval of durBucket duration, 
	 * index 0 is the latest, highest index is the oldest. 
	 * Weights will get filled into the next buckets with time passed. 
	 */
	final float[] buckets;

	final long durBucket;
	
 	public ActionFrequency(final int nBuckets, final long durBucket){
		this.buckets = new float[nBuckets];
		this.durBucket = durBucket;
	}
	
	/**
	 * Update and add.
	 * @param ts
	 */
	public void add(final long now, final float amount){
		update(now);
		buckets[0] += amount;
	}

	/**
	 * Update without adding, also updates time.
	 * @param now
	 */
	public void update(final long now) {
		final long diff = now - time;
		final int shift = (int) ((float) diff / (float) durBucket);
		if (shift == 0){
			// No update.
			return; 
		}
		else if (shift >= buckets.length){
			// Clear (beyond range).
			clear(now);
			return;
		}
		// Update buckets.
		for (int i = 0; i < buckets.length - shift; i++){
			buckets[buckets.length - (i + 1)] = buckets[buckets.length - (i + 1 + shift)];
		}
		for (int i = 0; i < shift; i++){
			buckets[i] = 0;
		}
		// Set time according to bucket duration (!).
		time += durBucket * shift;
	}

	public void clear(final long now) {
		for (int i = 0; i < buckets.length; i++){
			buckets[i] = 0;
		}
		time = now;
	}
	
	
	/**
	 * Get a weighted sum score, weight for bucket i: w(i) = factor^i. 
	 * @param factor
	 * @return
	 */
	public float getScore(final float factor){
		float res = buckets[0];
		float cf = factor;
		for (int i = 1; i < buckets.length; i++){
			res += cf * buckets[i];
			cf *= factor;
		}
		return res;
	}
	
	/**
	 * Get score of a certain bucket. At own risk.
	 * @param bucket
	 * @return
	 */
	public float getScore(final int bucket){
		return buckets[bucket];
	}
	
	/**
	 * Get score of first maxBuckets buckets, with factor.
	 * @param maxBucket
	 * @param factor
	 * @return
	 */
	public final float getScore(final int maxBuckets, float factor){
		float score = buckets[0];
		float cf = factor;
		for (int i = 1; i < maxBuckets; i++){
			score += buckets[i] * cf;
			cf *= factor;
		}
		return score;
			
			
	}
	
}
