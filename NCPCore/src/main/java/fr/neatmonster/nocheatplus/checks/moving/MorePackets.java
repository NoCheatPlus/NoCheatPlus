package fr.neatmonster.nocheatplus.checks.moving;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.net.NetStatic;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * The MorePackets check will try to identify players that send more than the usual
 * amount of move-packets to the server to be able to move faster than normal, without getting caught by the other
 * checks (flying/running).
 */
public class MorePackets extends Check {
	
	private final List<String> tags = new ArrayList<String>();
	
    /**
     * Instantiates a new more packets check.
     */
    public MorePackets() {
        super(CheckType.MOVING_MOREPACKETS);
    }

	/**
	 * Check for speeding by sending too many packets. We assume 22 packets per
	 * second to be legitimate, while 20 would be ideal. See
	 * PlayerData.morePacketsFreq for the monitored amount of time and the
	 * resolution. See NetStatic for the actual check code.
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
    	
//    	if (from.isSamePos(to)) {
//    		// Ignore moves with "just look" for now.
//    		// TODO: Extra ActionFrequency for "just look" + use to burn, maybe also check individually.
//    		return null;
//    	}
    	
    	// Ensure we have a set-back location.
        if (!data.hasMorePacketsSetBack()){
        	// TODO: Check if other set-back is appropriate or if to set/reset on other events.
        	if (data.hasSetBack()) {
        		data.setMorePacketsSetBack(data.getSetBack(to));
        	}
        	else {
        		data.setMorePacketsSetBack(from);
        	}
        }
        
        // Check for a violation of the set limits.
        tags.clear();
        final double violation = NetStatic.morePacketsCheck(data.morePacketsFreq, time, 1f, cc.morePacketsEPSMax, cc.morePacketsEPSIdeal, data.morePacketsBurstFreq, cc.morePacketsBurstPackets, cc.morePacketsBurstDirect, cc.morePacketsBurstEPM, tags);
        
        // Process violation result.
        if (violation > 0.0) {
        	
            // Increment violation level.
            data.morePacketsVL = violation; // TODO: Accumulate somehow [e.g. always += 1, decrease with continuous moving without violation]?
            
            // Violation handling.
            final ViolationData vd = new ViolationData(this, player, data.morePacketsVL, violation, cc.morePacketsActions);
            if (data.debug || vd.needsParameters()) {
            	vd.setParameter(ParameterName.PACKETS, Integer.toString(new Double(violation).intValue()));
            	vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
            }
            if (executeActions(vd)) {
            	// Set to cancel the move.
            	return data.getMorePacketsSetBack(); 
            } 
        } 
        else {
        	// Update the set-back location. (CHANGED to only update, if not a violation.)
        	// (Might update whenever newTo == null)
        	data.setMorePacketsSetBack(from);
        }
        
        // No set-back.
        return null;
        
    }
    
}
