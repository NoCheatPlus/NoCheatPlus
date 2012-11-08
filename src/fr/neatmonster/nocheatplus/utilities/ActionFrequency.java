package fr.neatmonster.nocheatplus.utilities;

/**
 * Keep track of frequency of some action, 
 * put weights into buckets, which represent intervals of time. 
 * @author mc_dev
 *
 */
public class ActionFrequency {
	
	/** Reference time for filling in. */
	private long time = 0;
	
	/**
	 * Buckets to fill weights in, each represents an interval of durBucket duration, 
	 * index 0 is the latest, highest index is the oldest. 
	 * Weights will get filled into the next buckets with time passed. 
	 */
	private final float[] buckets;

	/** Duration in milliseconds that oe bucket covers. */
	private final long durBucket;
	
 	public ActionFrequency(final int nBuckets, final long durBucket){
		this.buckets = new float[nBuckets];
		this.durBucket = durBucket;
	}
	
	/**
	 * Update and add.
	 * @param now
	 * @param amount
	 */
	public final void add(final long now, final float amount){
		update(now);
		buckets[0] += amount;
	}
	
	/**
	 * Unchecked addition of amount to the first bucket.
	 * @param amount
	 */
	public final void add(final float amount){
		buckets[0] += amount;
	}

	/**
	 * Update without adding, also updates time.
	 * @param now
	 */
	public final void update(final long now) {
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

	public final void clear(final long now) {
		for (int i = 0; i < buckets.length; i++){
			buckets[i] = 0f;
		}
		time = now;
	}
	
	/**
	 * @deprecated Use instead: score(float).
	 * @param factor
	 * @return
	 */
	public final float getScore(final float factor){
		return score(factor);
	}
	
	/**
	 * @deprecated Use instead: score(float).
	 * @param factor
	 * @return
	 */
	public final float getScore(final int bucket){
		return bucketScore(bucket);
	}
	
	/**
	 * Get a weighted sum score, weight for bucket i: w(i) = factor^i. 
	 * @param factor
	 * @return
	 */
	public final float score(final float factor){
		return sliceScore(0, buckets.length, factor);
	}
	
	/**
	 * Get score of a certain bucket. At own risk.
	 * @param bucket
	 * @return
	 */
	public final float bucketScore(final int bucket){
		return buckets[bucket];
	}
	
	/**
	 * Get score of first end buckets, with factor.
	 * @param end Number of buckets including start. This is not included.
	 * @param factor
	 * @return
	 */
	public final float leadingScore(final int end, float factor){
		return sliceScore(0, end, factor);
	}
	
	/**
	 * Get score from startBucket on, with factor.
	 * @param start This is included.
	 * @param factor
	 * @return
	 */
	public final float trailingScore(final int start, float factor){
		return sliceScore(start, buckets.length, factor);
	}
	
	/**
	 * Get score from startBucket on, until before maxBucket, with factor.
	 * @param start This is included.
	 * @param end This is not included.
	 * @param factor
	 * @return
	 */
	public final float sliceScore(final int start, final int end, float factor){
		float score = buckets[start];
		float cf = factor;
		for (int i = start; i < end; i++){
			score += buckets[i] * cf;
			cf *= factor;
		}
		return score;
	}
	
	/**
	 * Get reference time.
	 * @return
	 */
	public final long lastAccess(){
		return time;
	}
	
	/**
	 * Get the number of buckets.
	 * @return
	 */
	public final int numberOfBuckets(){
		return buckets.length;
	}
	
	/**
	 * Get the duration of a bucket in milliseconds.
	 * @return
	 */
	public final long bucketDuration(){
		return durBucket;
	}
	
}
