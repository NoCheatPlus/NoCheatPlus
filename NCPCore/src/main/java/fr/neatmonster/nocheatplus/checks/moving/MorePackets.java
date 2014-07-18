package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/**
 * The MorePackets check (previously called Speedhack check) will try to identify players that send more than the usual
 * amount of move-packets to the server to be able to move faster than normal, without getting caught by the other
 * checks (flying/running).
 * 
 * It monitors the number of packets sent to the server within 1 second and compares it to the "legal" number of packets
 * for that timeframe (22).
 */
public class MorePackets extends Check {

    /** The maximum number of packets per second that we accept. */
    private final static int maxPackets = 22;
    
    /** Assumed number of packets per second under ideal conditions. */
    private final static int idealPackets = 20;

    /**
     * Instantiates a new more packets check.
     */
    public MorePackets() {
        super(CheckType.MOVING_MOREPACKETS);
    }

    /**
     * Checks a player.
     * 
     * Players get assigned a certain amount of "free" packets as a limit initially. Every move packet reduces that
     * limit by 1. If more than 1 second of time passed, the limit gets increased by 22 * time in seconds, up to 50 and
     * they get a new "setback" location. If the player reaches limit = 0 -> teleport them back to "setback". If there was
     * a long pause (maybe lag), limit may be up to 100.
     * 
     * @param player
     *            the player
     * @param from
     *            the from
     * @param to
     *            the to
     * @return the location
     */
    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc) {
    	// Take time once, first:
    	final long time = System.currentTimeMillis();

        if (!data.hasMorePacketsSetBack()){
        	// TODO: Check if other set-back is appropriate or if to set on other events.
        	if (data.hasSetBack()) {
        		data.setMorePacketsSetBack(data.getSetBack(to));
        	}
        	else {
        		data.setMorePacketsSetBack(from);
        	}
        }
        
        // Add packet to frequency count.
        data.morePacketsFreq.add(time, 1f);
        
        // Fill up all "used" time windows (minimum we can do without other events).
        final float burnScore = (float) idealPackets * (float) data.morePacketsFreq.bucketDuration() / 1000f;
        // Find index.
        int i;
        int empty = 0;
        boolean used = false;
        for (i = 1; i < data.morePacketsFreq.numberOfBuckets(); i++) {
        	if (data.morePacketsFreq.bucketScore(i) > 0f) {
        		if (used) {
        			for (int j = i; j < data.morePacketsFreq.numberOfBuckets(); j ++) {
        				if (data.morePacketsFreq.bucketScore(j) == 0f) {
        					empty += 1;
        				}
        			}
        			break;
        		} else {
        			used = true;
        		}
        	}
        }
        final double fullCount;
        if (i < data.morePacketsFreq.numberOfBuckets()) {
        	// Assume all following time windows are burnt.
        	final float trailing = Math.max(data.morePacketsFreq.trailingScore(i, 1f), burnScore * (data.morePacketsFreq.numberOfBuckets() - i - empty));
        	final float leading = data.morePacketsFreq.leadingScore(i, 1f);
        	fullCount = leading + trailing;
        } else {
        	// All time windows are used.
        	fullCount = data.morePacketsFreq.score(1f);
        	
        }
        
        final double violation = (double) fullCount - (double) (burnScore * data.morePacketsFreq.numberOfBuckets());
        
        // TODO: Burn time windows based on other activity counting [e.g. same resolution ActinFrequency with keep-alive].

        // Player used up buffer, they fail the check.
        if (violation > 0.0) {
        	
            // Increment violation level.
            data.morePacketsVL = violation; // TODO: Accumulate somehow [e.g. always += 1, decrease with continuous moving without violation]?
            
            // Violation handling.
            final ViolationData vd = new ViolationData(this, player, data.morePacketsVL, violation, cc.morePacketsActions);
            if (cc.debug || vd.needsParameters()) {
            	vd.setParameter(ParameterName.PACKETS, Integer.toString(new Double(violation).intValue()));
            }
            if (executeActions(vd)){
            	return data.getMorePacketsSetBack(); 
            } 
        } 
        else {
        	// Set the new "setback" location. (CHANGED to only update, if not a violation.)
        	// (Might update whenever newTo == null)
        	data.setMorePacketsSetBack(from);
        }
        
        // No set-back.
        return null;
        
    }
    
}
