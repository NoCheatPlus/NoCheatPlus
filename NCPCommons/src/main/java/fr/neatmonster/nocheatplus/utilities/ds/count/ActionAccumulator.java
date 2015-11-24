package fr.neatmonster.nocheatplus.utilities.ds.count;

import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * This class is meant to accumulate actions similar to ActionFrequency. In contrast to ActionFrequency, this class will accumulate values grouped by number of events instead of real-time intervals.
 * @author mc_dev
 *
 */
public class ActionAccumulator {
	/** Event count. */
	private final int[] counts;
	/** Value accumulation. */
	private final float[] buckets;
	/** Capacity of each bucket. */
	private final int bucketCapacity;
	
	public ActionAccumulator(final int nBuckets, final int bucketCapacity){
		this.counts = new int[nBuckets];
		this.buckets = new float[nBuckets];
		this.bucketCapacity = bucketCapacity;
	}
	
	public void add(float value){
		if (counts[0] >= bucketCapacity){
			shift();
		}
		counts[0] ++;
		buckets[0] += value;
	}
	
	private void shift() {
		for (int i = buckets.length - 1; i > 0 ; i--){
			counts[i] = counts[i - 1];
			buckets[i] = buckets[i - 1];
		}
		counts[0] = 0;
		buckets[0] = 0;
	}

	/**
	 * Total score.
	 * @return
	 */
	public float score(){
		float score = 0;
		for (int i = 0; i < buckets.length; i++){
			score += buckets[i];
		}
		return score;
	}
	
	/**
	 * Total count.
	 * @return
	 */
	public int count(){
		int count = 0;
		for (int i = 0; i < counts.length; i++){
			count += counts[i];
		}
		return count;
	}
	
	public void clear(){
		for (int i = 0; i < buckets.length; i++){
			counts[i] = 0;
			buckets[i] = 0;
		}
	}
	
	public int bucketCount(final int bucket){
		return counts[bucket];
	}
	
	public float bucketScore(final int bucket){
		return buckets[bucket];
	}
	
	public int numberOfBuckets(){
		return buckets.length;
	}
	
	public int bucketCapacity(){
		return bucketCapacity;
	}
	
	/**
	 * Simple display of bucket contents, no class name.
	 * @return
	 */
	public String toInformalString(){
		StringBuilder b = new StringBuilder(buckets.length * 10);
		b.append("|");
		for (int i = 0; i < buckets.length; i++){
			b.append(StringUtil.fdec3.format(buckets[i]) + "/" + counts[i] + "|");
		}
		return b.toString();
	}
	
}
