package fr.neatmonster.nocheatplus.net;

import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * Static method utility for networking related stuff.
 * <hr>
 * Not sure about final location and naming... and content :p.
 * @author dev1mc
 *
 */
public class NetStatic {
	
	/**
	 * Packet-cheating check, for catching clients that send more packets than
	 * allowed. Intention is to have a more accurate check than just preventing
	 * "extreme spamming".
	 * 
	 * @param packetFreq
	 *            Records the packets. This check will update packetFreq
	 *            according to the given time and packets.
	 * @param time
	 *            Milliseconds time to update the ActionFrequency instance with.
	 * @param packets
	 *            Amount to add to packetFreq with time.
	 * @param maxPackets
	 *            The amount of packets per second (!), that is considered
	 *            legitimate.
	 * @param idealPackets
	 *            The "ideal" amount of packets per second. Used for "burning"
	 *            time frames by setting them to this amount.
	 * @return The violation amount, i.e. "count above limit".
	 */
	public static double morePacketsCheck(final ActionFrequency packetFreq, final long time, final float packets, final float maxPackets, final float idealPackets) {
		// Pull down stuff.
		final long winDur = packetFreq.bucketDuration();
		final int winNum = packetFreq.numberOfBuckets();
		// Add packet to frequency count.
        packetFreq.add(time, packets);
        
        // Fill up all "used" time windows (minimum we can do without other events).
        final float burnScore = (float) idealPackets * (float) winDur / 1000f;
        // Find index.
        int burnStart;
        int empty = 0;
        boolean used = false;
        for (burnStart = 1; burnStart < winNum; burnStart ++) {
        	if (packetFreq.bucketScore(burnStart) > 0f) {
        		if (used) {
        			for (int j = burnStart; j < winNum; j ++) {
        				if (packetFreq.bucketScore(j) == 0f) {
        					empty += 1;
        				}
        			}
        			break;
        		} else {
        			used = true;
        		}
        	}
        }
        
        // TODO: Burn time windows based on other activity counting [e.g. same resolution ActinFrequency with keep-alive].
        
        // Adjust empty based on server side lag, this makes the check more strict.
        // TODO: Consider to add a config flag for skipping the lag adaption (e.g. strict).
    	final float lag = TickTask.getLag(winDur * winNum, true);
    	// TODO: Consider increasing the allowed maximum, for extreme server-side lag.
        empty = Math.min(empty, (int) Math.round((lag - 1f) * winNum));
        
        final double fullCount;
        if (burnStart < winNum) {
        	// Assume all following time windows are burnt.
        	final float trailing = Math.max(packetFreq.trailingScore(burnStart, 1f), burnScore * (winNum - burnStart - empty));
        	final float leading = packetFreq.leadingScore(burnStart, 1f);
        	fullCount = leading + trailing;
        } else {
        	// All time windows are used.
        	fullCount = packetFreq.score(1f);
        }
        
        return (double) fullCount - (double) (maxPackets * winNum * winDur / 1000f);
        
	}
	
}
